package ru.fizteh.fivt.students.kinanAlsarmini.binder;

import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.BinderFactory;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.MyBinder;

import java.util.HashMap;
import java.lang.reflect.Field;

public class MyBinderFactory implements BinderFactory {
    private HashMap<Class<?>,Boolean> isSerializable;
    private HashMap<Class<?>,MyBinder> cache;

    MyBinderFactory() {
        isSerializable = new HashMap<Class<?>,Boolean>();
        cache = new HashMap<Class<?>,MyBinder>();
    }

    private boolean checkDefaultConstructor(Class<?> clazz) {
        try {
            clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException | ExceptionInInitializerError e) {
            return false;
        }

        return true;
    }

    private boolean checkSerializability(Class<?> clazz) {
        if (isSerializable.containsKey(clazz)) {
            return isSerializable.get(clazz);
        }

        if (clazz.isEnum() || clazz.isPrimitive() || MyBinder.isWrapperType(clazz) || clazz.equals(String.class)) {
            isSerializable.put(clazz, true);
            return true;
        }

        if (clazz.isArray()) {
            isSerializable.put(clazz, false);
            return false;
        }

        Field[] fields = clazz.getDeclaredFields();
        if (fields.length == 0) {
            isSerializable.put(clazz, true);
            return true;
        }

        if (!checkDefaultConstructor(clazz)) {
            isSerializable.put(clazz, false);
            return false;
        }

        isSerializable.put(clazz, true);
        for (Field field : fields) {
            if (!checkSerializability(field.getType())) {
                isSerializable.put(clazz, false);
            }
        }

        return isSerializable.get(clazz);
    }

    @Override
    public <T> MyBinder<T> create(Class<T> clazz) {
        if (cache.containsKey(clazz)) {
            return cache.get(clazz);
        }

        if (clazz == null) {
            throw new IllegalArgumentException("Invalid class: null");
        }

        if (!checkSerializability(clazz)) {
            throw new IllegalArgumentException("Invalid class: not serializable");
        }

        cache.put(clazz, new MyBinder<T>(clazz));
        return cache.get(clazz);
    }
}
