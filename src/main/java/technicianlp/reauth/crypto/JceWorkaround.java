package technicianlp.reauth.crypto;

import technicianlp.reauth.ReAuth;
import technicianlp.reauth.util.ReflectionHelper;

import javax.crypto.Cipher;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.security.PermissionCollection;
import java.util.Map;

public final class JceWorkaround {

    /**
     * check if CryptoAllPermission is in effect and try to remove Jce restrictions otherwise
     */
    public static void ensureUnlimitedCryptography() {
        try {
            if (Cipher.getMaxAllowedKeyLength("AES") != Integer.MAX_VALUE) {
                ReAuth.log.warn("Cryptography is restricted in this Java installation");
                ReAuth.log.warn("Please complain to Mojang for shipping a 5 year old Java version");
                removeCryptographyRestrictions();
                if (Cipher.getMaxAllowedKeyLength("AES") != Integer.MAX_VALUE) {
                    ReAuth.log.error("Failed to remove cryptography restriction");
                } else {
                    ReAuth.log.info("Cryptography restriction removed successfully");
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("AES unavailable", e);
        }
    }

    /**
     * Java had for legal reasons limited the allowed strength of cryptographic algorithms.
     * Historically to disable this restriction the so called "Java Cryptography Extension (JCE) Unlimited Strength
     * Jurisdiction Policy Files" has to be installed within the JRE directory.
     * Since update 151 (October 17, 2017) this restrictions can be disabled programmatically
     * and has since been disabled by default in update 161 (January 16, 2018).
     * <p>
     * Since Mojang for some insane reason ships the 5 year old update 51 (July 14, 2015), installation of the policy files
     * would be necessary. Since installation of those files cannot be required of the user, a workaround has been found in
     * https://stackoverflow.com/questions/1179672 and is used to disable this restriction at runtime:
     * <p>
     * JceSecurity.isRestricted = false;
     * JceSecurity.defaultPolicy.perms.clear();
     * JceSecurity.defaultPolicy.add(CryptoAllPermission.INSTANCE);
     * <p>
     * The alternative to this workaround would have been to drop the AES key-length from 256 bits to 128 bits.
     */
    private static void removeCryptographyRestrictions() {
        try {
            final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
            final Field isRestricted = ReflectionHelper.findField(jceSecurity, "isRestricted");
            ReflectionHelper.unlockFinalField(isRestricted);
            final Field defaultPolicy = ReflectionHelper.findField(jceSecurity, "defaultPolicy");

            final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
            final Field perms = ReflectionHelper.findField(cryptoPermissions, "perms");

            final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");
            final Field instance = ReflectionHelper.findField(cryptoAllPermission, "INSTANCE");


            ReflectionHelper.setField(isRestricted, null, false);

            final PermissionCollection permissionCollection = ReflectionHelper.getField(defaultPolicy, null);
            ((Map<?, ?>) ReflectionHelper.getField(perms, permissionCollection)).clear();

            permissionCollection.add(ReflectionHelper.getField(instance, null));
        } catch (final Exception e) {
            ReAuth.log.error("Exception removing cryptography restrictions", e);
        }
    }
}
