package ru.fizteh.fivt.students.vishnevskiy.binder;

import ru.fizteh.fivt.binder.*;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

public class MyBinderFactory implements BinderFactory {
    private Map<Class<?>, MyBinder> binders;

    public MyBinderFactory() {
        binders = new HashMap<Class<?>, MyBinder>();
    }

    private void checkDefaultConstructor(Class<?> clazz) {
        for (Constructor constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterTypes().length == 0) {
                return;
            }
        }
        throw new IllegalArgumentException("The class can't be serialized");
    }

    private void checkSerializable(Class<?> clazz) {
        if (clazz.isPrimitive() || clazz.equals(String.class) || clazz.isEnum()) {
            return;
        }
        if (clazz.isArray() || MyBinder.isWrapperType(clazz)) {
            throw new IllegalArgumentException("The class can't be serialized");
        }
        checkDefaultConstructor(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(DoNotBind.class) != null) {
                continue;
            }
            checkSerializable(field.getType());
        }
    }

    public <T> MyBinder<T> create(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Invalid class");
        }
        if (clazz.isPrimitive() || MyBinder.isWrapperType(clazz)) {
            throw new IllegalArgumentException("Invalid class");
        }
        checkSerializable(clazz);

        if (binders.containsKey(clazz)) {
            return binders.get(clazz);
        }

        MyBinder<T> binder = new MyBinder<T>(clazz);
        binders.put(clazz, binder);
        return binder;
    }
}