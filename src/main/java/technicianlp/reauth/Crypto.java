package technicianlp.reauth;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Basic encryption for credentials via PBKDF2 and AES-CBC<br>
 * Key and IV for AES are derived from the PBKDF2-Hash<br>
 */
final class Crypto {

    private static final int iterations = 100_000;

    private final Cipher aes;
    private final SecretKeyFactory pbkdf;

    private String key;
    private byte[] salt;
    private byte[] hash;

    public Crypto() throws GeneralSecurityException {
        if (Cipher.getMaxAllowedKeyLength("AES") < 256) {
            removeJceRestriction();
        }
        aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbkdf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
    }

    private void removeJceRestriction() throws NoSuchAlgorithmException {
        ReAuth.log.warn("Cryptography is restricted in this Java installation");
        ReAuth.log.warn("Please complain to Mojang for shipping a 5 year old Java version");
        new JceWorkaround().removeCryptographyRestrictions();
        if (Cipher.getMaxAllowedKeyLength("AES") < 256) {
            ReAuth.log.error("Failed to remove cryptography restriction - saving credentials will not be available");
            throw new NoSuchAlgorithmException("AES 256 unsupported by JVM");
        } else {
            ReAuth.log.info("Cryptography restriction removed successfully");
        }
    }

    private byte[] crypt(int mode, byte[] secret) throws GeneralSecurityException {
        SecretKeySpec secretKey = new SecretKeySpec(getHash(), 0, 32, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(getHash(), 32, 16);

        aes.init(mode, secretKey, ivParameterSpec);
        return aes.doFinal(secret);
    }

    public String encryptString(String string) {
        try {
            byte[] raw = string.getBytes(StandardCharsets.UTF_8);
            byte[] enc = crypt(Cipher.ENCRYPT_MODE, raw);
            return Base64.getEncoder().encodeToString(enc);
        } catch (GeneralSecurityException e) {
            ReAuth.log.error("Unexpected Crypto Exception", e);
        }
        return "";
    }

    public String decryptString(String string) {
        try {
            byte[] raw = Base64.getDecoder().decode(string);
            byte[] dec = crypt(Cipher.DECRYPT_MODE, raw);
            return new String(dec, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            ReAuth.log.error("Unexpected Crypto Exception", e);
        }
        return "";
    }

    /**
     * set Parameters, invalidate hash if parameters changed
     */
    public void setup(String key, byte[] salt) {
        if (key.equals(this.key) && Arrays.equals(salt, this.salt))
            return;
        this.key = key;
        this.salt = salt;
        this.hash = null;
    }

    /**
     * Lazy generate hash
     */
    private byte[] getHash() throws InvalidKeySpecException {
        if (hash == null) {
            hash = pbkdf.generateSecret(new PBEKeySpec(key.toCharArray(), salt, iterations, 512)).getEncoded();
        }
        return hash;
    }
}
