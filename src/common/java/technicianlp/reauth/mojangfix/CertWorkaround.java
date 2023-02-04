package technicianlp.reauth.mojangfix;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import technicianlp.reauth.ReAuth;

public final class CertWorkaround {

    private static final String MICROSOFT2017 = "microsoftrsarootcertificateauthority2017";
    private static final String AMAZON1 = "amazonrootca1";
    private static final String DIGICERT2 = "digicertglobalrootg2";

    private static SSLSocketFactory socketFactory = null;

    public static SSLSocketFactory getSocketFactory() {
        return socketFactory;
    }

    /**
     * The default truststore is missing some CA-Certificates required during authentication with Microsoft/XBox/Mojang
     * because Mojang for some insane reason ships the 7 years old Java 8 Update 51 (July 14, 2015).
     * <p>
     * The following Certificates are installed if they are missing:
     * - Microsoft RSA Root Certificate Authority 2017
     * - DigiCert Global Root G2
     * - Amazon Root CA 1
     */
    static void checkCertificates() {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate microsoft2017 = loadCertificate(cf, MICROSOFT2017);
            X509Certificate amazon1 = loadCertificate(cf, AMAZON1);
            X509Certificate digicert2 = loadCertificate(cf, DIGICERT2);

            TrustManagerFactory defaultTrust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            defaultTrust.init((KeyStore) null);

            List<X509Certificate> trustedCerts = getTrustedCerts(defaultTrust);
            Map<String, X509Certificate> missingCerts = new HashMap<>();
            if (!trustedCerts.contains(microsoft2017)) {
                missingCerts.put(MICROSOFT2017, microsoft2017);
            }
            if (!trustedCerts.contains(amazon1)) {
                missingCerts.put(AMAZON1, amazon1);
            }
            if (!trustedCerts.contains(digicert2)) {
                missingCerts.put(DIGICERT2, digicert2);
            }

            if (missingCerts.isEmpty()) {
                // no additional certificates required
                return;
            } else {
                ReAuth.log.warn("Some Certificates required for authentication are untrusted by default");
            }

            X509ExtendedTrustManager defaultTrustManager = findX509ExtendedTrustManager(defaultTrust);
            X509ExtendedTrustManager missingTrustManager = findX509ExtendedTrustManager(createTrustFactory(missingCerts));
            X509ExtendedTrustManager combinedTrustManager = new CombinedX509ExtendedTrustManager(defaultTrustManager, missingTrustManager);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509ExtendedTrustManager[]{combinedTrustManager}, null);

            CertWorkaround.socketFactory = context.getSocketFactory();
            ReAuth.log.info("Successfully built SSLSocketFactory with required Certificates");
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static X509Certificate loadCertificate(CertificateFactory certFactory, String name) throws CertificateException, IOException {
        try (InputStream is = CertWorkaround.class.getResourceAsStream("/resources/reauth/certs/" + name + ".pem")) {
            if (is != null) {
                return (X509Certificate) certFactory.generateCertificate(is);
            } else {
                throw new FileNotFoundException("Certificate " + name + " is unavailable");
            }
        }
    }

    private static List<X509Certificate> getTrustedCerts(TrustManagerFactory trustManagerFactory) {
        X509ExtendedTrustManager trustManager = findX509ExtendedTrustManager(trustManagerFactory);
        if (trustManager != null) {
            return new ArrayList<>(Arrays.asList(trustManager.getAcceptedIssuers()));
        } else {
            return new ArrayList<>();
        }
    }

    private static TrustManagerFactory createTrustFactory(Map<String, X509Certificate> certificates) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null);
        for (Map.Entry<String, X509Certificate> certificate : certificates.entrySet()) {
            ReAuth.log.info("Adding Certificate {} to trust", certificate.getKey());
            ks.setCertificateEntry(certificate.getKey(), certificate.getValue());
        }
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(ks);
        return trustFactory;
    }

    private static X509ExtendedTrustManager findX509ExtendedTrustManager(TrustManagerFactory trustManagerFactory) {
        return Arrays.stream(trustManagerFactory.getTrustManagers())
                .filter(X509ExtendedTrustManager.class::isInstance)
                .map(X509ExtendedTrustManager.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * required in order to combine multiple TrustManagers.
     * The default Implementation of {@link SSLContextSpi} only considers the first {@link X509TrustManager}
     */
    private static final class CombinedX509ExtendedTrustManager extends X509ExtendedTrustManager {

        private final List<X509ExtendedTrustManager> trustManagers;

        private CombinedX509ExtendedTrustManager(X509ExtendedTrustManager... trustManagers) {
            this.trustManagers = new ArrayList<>(Arrays.asList(trustManagers));
            if (this.trustManagers.isEmpty()) {
                throw new IllegalArgumentException("At least one X509ExtendedTrustManager is required");
            }
            if (this.trustManagers.contains(null)) {
                throw new IllegalArgumentException("X509ExtendedTrustManager cannot be null");
            }
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            this.check(tm -> tm.checkClientTrusted(chain, authType, socket));
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            this.check(tm -> tm.checkServerTrusted(chain, authType, socket));
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            this.check(tm -> tm.checkClientTrusted(chain, authType, engine));
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            this.check(tm -> tm.checkServerTrusted(chain, authType, engine));
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            this.check(tm -> tm.checkClientTrusted(chain, authType));
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            this.check(tm -> tm.checkServerTrusted(chain, authType));
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return this.trustManagers.stream().map(X509TrustManager::getAcceptedIssuers).flatMap(Arrays::stream).toArray(X509Certificate[]::new);
        }

        /**
         * checks if any of the trustManagers accepts the supplied operation.
         *
         * @throws CertificateException if all trustManagers refuse the operation
         */
        private void check(CertificateCheckConsumer checkFunction) throws CertificateException {
            Deque<CertificateException> exceptions = new LinkedList<>();
            for (X509ExtendedTrustManager trustManager : this.trustManagers) {
                try {
                    checkFunction.check(trustManager);
                    return; // accepted by TrustManager
                } catch (CertificateException e) {
                    exceptions.add(e);
                }
            }
            CertificateException last = exceptions.removeLast();
            exceptions.forEach(last::addSuppressed);
            throw last;
        }
    }

    @FunctionalInterface
    private interface CertificateCheckConsumer {
        void check(X509ExtendedTrustManager trustManager) throws CertificateException;
    }
}
