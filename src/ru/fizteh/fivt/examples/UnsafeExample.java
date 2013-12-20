package ru.fizteh.fivt.examples;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * @author Dmitriy Komanov (dkomanov@ya.ru)
 */
public class UnsafeExample {

    private static final Unsafe UNSAFE_INSTANCE;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE_INSTANCE = Unsafe.class.cast(field.get(null));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to get Unsafe instance", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to obtain theUnsafe field", e);
        }
    }

    private static class NoDefaultConstructorClass {
        public final int n;
        public final String s;

        private NoDefaultConstructorClass(int n, String s) {
            this.n = n;
            this.s = s;
        }
    }

    public static void main(String[] args) throws Exception {
        Object o = UNSAFE_INSTANCE.allocateInstance(NoDefaultConstructorClass.class);
        assert o != null;
        NoDefaultConstructorClass c = (NoDefaultConstructorClass) o;
        assert c.n == 0;
        assert c.s == null;
    }
}
