package technicianlp.reauth.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public enum ReflectionUtils {
    ;

    private static Field findFieldInternal(Class<?> clz, String name) throws NoSuchFieldException {
        Field field = clz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    public static Field findField(Class<?> clz, String name) {
        try {
            return findFieldInternal(clz, name);
        } catch (NoSuchFieldException exception) {
            throw new UncheckedReflectiveOperationException("Unable to find Field: " + name, exception);
        }
    }

    // TODO: use Mutator mixins?

    public static Field findObfuscatedField(Class<?> clz, String obfName, String name) {
        try {
            return findFieldInternal(clz, obfName);
        } catch (NoSuchFieldException suppressed) {
            try {
                return findFieldInternal(clz, name);
            } catch (NoSuchFieldException exception) {
                exception.addSuppressed(suppressed);
                throw new UncheckedReflectiveOperationException("Unable to find Obfuscated Field: " + name, exception);
            }
        }
    }

    public static void unlockFinalField(Field field) {
        try {
            Field fieldModifiers = findField(Field.class, "modifiers");
            fieldModifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException exception) {
            throw new UncheckedReflectiveOperationException("Unable to unlock final field", exception);
        }
    }

    public static void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException exception) {
            throw new UncheckedReflectiveOperationException("Failed Reflective set", exception);
        }
    }

    public static <T> T getField(Field field, Object target) {
        try {
            //noinspection unchecked
            return (T) field.get(target);
        } catch (IllegalAccessException exception) {
            throw new UncheckedReflectiveOperationException("Failed Reflective get", exception);
        }
    }

    public static class UncheckedReflectiveOperationException extends RuntimeException {

        public UncheckedReflectiveOperationException(String message, ReflectiveOperationException cause) {
            super(message, cause);
        }
    }
}
