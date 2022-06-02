package technicianlp.reauth.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ReflectionUtils {

    private static final Field fieldModifiers = findField(Field.class, "modifiers");

    private static Method findMethodInternal(Class<?> clz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = clz.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    public static Method findMethod(Class<?> clz, String name, Class<?>... parameterTypes) {
        try {
            return findMethodInternal(clz, name, parameterTypes);
        } catch (ReflectiveOperationException exception) {
            throw new UncheckedReflectiveOperationException("Unable to find Method: " + name, exception);
        }
    }

    public static Method findMcpMethod(Class<?> clz, String obfName, String name, Class<?>... parameterTypes) {
        try {
            return findMethodInternal(clz, obfName, parameterTypes);
        } catch (NoSuchMethodException suppressed) {
            try {
                return findMethodInternal(clz, name, parameterTypes);
            } catch (NoSuchMethodException exception) {
                exception.addSuppressed(suppressed);
                throw new UncheckedReflectiveOperationException("Unable to find MCP Method: " + name, exception);
            }
        }
    }

    public static <T> T callMethod(Method method, Object target, Object... args) {
        try {
            //noinspection unchecked
            return (T) method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new UncheckedReflectiveOperationException("Failed reflective Method call", exception);
        }
    }

    private static Field findFieldInternal(Class<?> clz, String name) throws NoSuchFieldException {
        Field field = clz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    public static Field findField(Class<?> clz, String name) {
        try {
            return findFieldInternal(clz, name);
        } catch (ReflectiveOperationException exception) {
            throw new UncheckedReflectiveOperationException("Unable to find Field: " + name, exception);
        }
    }

    public static Field findMcpField(Class<?> clz, String obfName, String name) {
        try {
            return findFieldInternal(clz, obfName);
        } catch (NoSuchFieldException suppressed) {
            try {
                return findFieldInternal(clz, name);
            } catch (NoSuchFieldException exception) {
                exception.addSuppressed(suppressed);
                throw new UncheckedReflectiveOperationException("Unable to find MCP Field: " + name, exception);
            }
        }
    }

    public static void unlockFinalField(Field field) {
        try {
            fieldModifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (ReflectiveOperationException exception) {
            throw new UncheckedReflectiveOperationException("Unable to unlock final field", exception);
        }
    }

    public static void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new UncheckedReflectiveOperationException("Failed Reflective set", exception);
        }
    }

    public static <T> T getField(Field field, Object target) {
        try {
            //noinspection unchecked
            return (T) field.get(target);
        } catch (ReflectiveOperationException exception) {
            throw new UncheckedReflectiveOperationException("Failed Reflective get", exception);
        }
    }

    public static class UncheckedReflectiveOperationException extends RuntimeException {

        public UncheckedReflectiveOperationException(String message, ReflectiveOperationException cause) {
            super(message, cause);
        }
    }
}
