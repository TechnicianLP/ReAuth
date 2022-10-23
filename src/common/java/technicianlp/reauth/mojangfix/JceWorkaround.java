package technicianlp.reauth.mojangfix;

import technicianlp.reauth.ReAuth;
import technicianlp.reauth.crypto.CryptoException;
import technicianlp.reauth.util.ReflectionUtils;

import javax.crypto.Cipher;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.security.PermissionCollection;
import java.util.Map;

enum JceWorkaround {
    ;

    /**
     * check if CryptoAllPermission is in effect and try to remove Jce restrictions otherwise
     */
    static void ensureUnlimitedCryptography() {
        try {
            if (Cipher.getMaxAllowedKeyLength("AES") != Integer.MAX_VALUE) {
                ReAuth.log.warn("Cryptography is restricted in this Java installation");
                if (!MojangJavaFix.java8) {
                    ReAuth.log.warn("Cryptography is likely deliberately restricted!");
                }
                removeCryptographyRestrictions();
                if (Cipher.getMaxAllowedKeyLength("AES") == Integer.MAX_VALUE) {
                    ReAuth.log.info("Cryptography restriction removed successfully");
                } else {
                    ReAuth.log.error("Failed to remove cryptography restriction");
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("AES unavailable", e);
        }
    }

    /**
     * Java had for legal reasons limited the allowed strength of cryptographic algorithms. Historically to disable this
     * restriction the so-called "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files" have
     * to be installed within the JRE directory. Since update 151 (October 17, 2017) these restrictions can be disabled
     * programmatically and have since been disabled by default in update 161 (January 16, 2018).
     * <p>
     * Since Mojang for some insane reason ships the 7 years old update 51 (July 14, 2015), installation of the policy
     * files would be necessary. Since installation of those files cannot be required of the user, a workaround has been
     * found in <a href="https://stackoverflow.com/questions/1179672">https://stackoverflow.com/questions/1179672</a>
     * and is used to disable this restriction at runtime:
     * <p>
     * JceSecurity.isRestricted = false; JceSecurity.defaultPolicy.perms.clear();
     * JceSecurity.defaultPolicy.add(CryptoAllPermission.INSTANCE);
     * <p>
     * The alternative to this workaround would have been to drop the AES key-length from 256 bits to 128 bits.
     */
    private static void removeCryptographyRestrictions() {
        try {
            Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
            Field isRestricted = ReflectionUtils.findField(jceSecurity, "isRestricted");
            ReflectionUtils.unlockFinalField(isRestricted);
            Field defaultPolicy = ReflectionUtils.findField(jceSecurity, "defaultPolicy");

            Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
            Field perms = ReflectionUtils.findField(cryptoPermissions, "perms");

            Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");
            Field instance = ReflectionUtils.findField(cryptoAllPermission, "INSTANCE");


            ReflectionUtils.setField(isRestricted, null, false);

            PermissionCollection permissionCollection = ReflectionUtils.getField(defaultPolicy, null);
            ((Map<?, ?>) ReflectionUtils.getField(perms, permissionCollection)).clear();

            permissionCollection.add(ReflectionUtils.getField(instance, null));
        } catch (Exception e) {
            ReAuth.log.error("Exception removing cryptography restrictions", e);
        }
    }
}
