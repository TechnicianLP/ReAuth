package technicianlp.reauth.util;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ReflectionUtils {

    private static final Field fieldModifiers = findField(Field.class, "modifiers");

    public static Method findMethod(Class<?> clz, String name, Class<?>... parameterTypes) {
        try {
            Method method = clz.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException exception) {
            throw new UncheckedReflectiveOperationException("Unable to find Method: " + name, exception);
        }
    }

    public static Method findMcpMethod(Class<?> clz, String obfName, String name, Class<?>... parameterTypes) {
        try {
            return net.minecraftforge.fml.relauncher.ReflectionHelper.findMethod(clz, name, obfName, parameterTypes);
        } catch (UnableToFindMethodException ignored) {
            throw new UncheckedReflectiveOperationException("Unable to find MCP Method: " + name);
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

    public static <T> Constructor<T> findConstructor(Class<T> clz, Class<?>... parameterTypes) {
        try {
            Constructor<T> constructor = clz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Unable to find Constructor", exception);
        }
    }

    public static <T> T callConstructor(Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Failed reflective Constructor call", exception);
        }
    }

    public static Field findField(Class<?> clz, String name) {
        try {
            Field field = clz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new UncheckedReflectiveOperationException("Unable to find Field: " + name, exception);
        }
    }

    public static <T> Field findMcpField(Class<? super T> clz, String obfName, String name) {
        try {
            return ReflectionHelper.findField(clz, obfName, name);
        } catch (UnableToFindFieldException ignored) {
            throw new UncheckedReflectiveOperationException("Unable to find MCP Field: " + name);
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

        public UncheckedReflectiveOperationException(String message) {
            super(message);
        }
    }
}
