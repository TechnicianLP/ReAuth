package technicianlp.reauth;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.PermissionCollection;
import java.util.Map;

final class JceWorkaround {

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
    public void removeCryptographyRestrictions() {
        try {
            final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
            final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
            final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");

            setFinalField(jceSecurity, "isRestricted", null, true);

            final PermissionCollection defaultPolicy = getFieldValue(jceSecurity, "defaultPolicy", null);
            ((Map<?, ?>) getFieldValue(cryptoPermissions, "perms", defaultPolicy)).clear();
            defaultPolicy.add(getFieldValue(cryptoAllPermission, "INSTANCE", null));
        } catch (final Exception e) {
            ReAuth.log.error("Exception removing cryptography restrictions", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <E> E getFieldValue(Class<?> clz, String name, Object target) throws ReflectiveOperationException {
        Field field = clz.getDeclaredField(name);
        field.setAccessible(true);
        return (E) field.get(target);
    }

    @SuppressWarnings("SameParameterValue")
    private void setFinalField(Class<?> clz, String name, Object target, Object value) throws ReflectiveOperationException {
        Field field = clz.getDeclaredField(name);
        field.setAccessible(true);
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(target, value);
    }
}
