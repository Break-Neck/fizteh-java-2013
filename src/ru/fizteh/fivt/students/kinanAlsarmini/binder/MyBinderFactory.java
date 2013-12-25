package ru.fizteh.fivt.students.kinanAlsarmini.binder;

import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.BinderFactory;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.MyBinder;

import ru.fizteh.fivt.binder.Name;
import ru.fizteh.fivt.binder.DoNotBind;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

public class MyBinderFactory implements BinderFactory {
    private HashMap<Class<?>,Boolean> isSerializable;
    private HashMap<Class<?>,MyBinder> cache;

    public MyBinderFactory() {
        cache = new HashMap<Class<?>,MyBinder>();
    }

    private boolean checkDefaultConstructor(Class<?> clazz) {
        for (Constructor constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length == 0) {
                return true;
            }
        }

        return false;
    }

    private boolean checkSerializability(Class<?> clazz) {
        if (isSerializable.containsKey(clazz)) {
            return isSerializable.get(clazz);
        }
        
        if (MyBinder.isWrapperType(clazz)) {
            isSerializable.put(clazz, false);
            return false;
        }
        
        if (clazz.isEnum() || clazz.isPrimitive() || MyBinder.isWrapperType(clazz) || clazz.equals(String.class)) {
            isSerializable.put(clazz, true);
            return true;
        }

        if (clazz.isArray()) {
            isSerializable.put(clazz, false);
            throw new IllegalArgumentException("Not serializable: contains array");
        }

        Field[] fields = clazz.getDeclaredFields();

        if (!checkDefaultConstructor(clazz)) {
            isSerializable.put(clazz, false);
            throw new IllegalArgumentException("Not serializable: no default constructor");
        }

        if (fields.length == 0) {
            isSerializable.put(clazz, true);
            return true;
        }

        isSerializable.put(clazz, true);

        Set<String> fieldNames = new HashSet<String>();

        for (Field field : fields) {
            if (field.getAnnotation(DoNotBind.class) != null) {
                continue;
            }

            String fieldName = field.getName();
            Name name = field.getAnnotation(Name.class);
            if (name != null) {
                fieldName = name.value();
            }

            if (fieldNames.contains(fieldName)) {
                throw new IllegalArgumentException("Not serializable: Duplicate field name");
            }

            fieldNames.add(fieldName);

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

        isSerializable = new HashMap<Class<?>,Boolean>();
        if (!checkSerializability(clazz)) {
            throw new IllegalArgumentException("Invalid class: not serializable");
        }

        cache.put(clazz, new MyBinder<T>(clazz));
        return cache.get(clazz);
    }
}
