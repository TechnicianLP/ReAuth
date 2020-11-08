package technicianlp.reauth;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ReflectionHelper {

    public static Method findMethod(Class<?> clz, String name, Class<?>... parameterTypes) {
        try {
            Method method = clz.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public static <T> T callMethod(Method method, Object target, Object... args) throws UncheckedInvocationTargetException {
        try {
            //noinspection unchecked
            return (T) method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw new UncheckedInvocationTargetException(e);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed reflective Method call", e);
        }
    }

    public static <T> Constructor<T> findConstructor(Class<T> clz, Class<?>... parameterTypes) {
        try {
            Constructor<T> constructor = clz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public static <T> T callConstructor(Constructor<T> constructor, Object... args) throws UncheckedInvocationTargetException {
        try {
            return constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            throw new UncheckedInvocationTargetException(e);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed reflective Constructor call", e);
        }
    }

    public static Field findField(Class<?> clz, String name) {
        try {
            Field field = clz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    public static void setField(Field field, Object target, Object value){
        try {
            field.set(target, value);
        } catch (ReflectiveOperationException throwable) {
            throw new RuntimeException("Failed Reflective set", throwable);
        }
    }

    public static <T> T getField(Field field, Object target){
        try {
            //noinspection unchecked
            return (T) field.get(target);
        } catch (ReflectiveOperationException throwable) {
            throw new RuntimeException("Failed Reflective get", throwable);
        }
    }

    public static class UncheckedInvocationTargetException extends RuntimeException {
        public UncheckedInvocationTargetException(InvocationTargetException e) {
            super(e.getCause());
        }
    }
}
