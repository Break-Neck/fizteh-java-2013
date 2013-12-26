package ru.fizteh.fivt.students.dobrinevski.binder;

import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.BinderFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;

public class MyBinderFactory implements BinderFactory {

    public <T> Binder<T> create(Class<T> clazz) throws IllegalArgumentException {
        if (clazz == null) {
            throw new IllegalArgumentException("input is null");
        }

        if (clazz.isPrimitive() || clazz.isEnum()) {
            throw new IllegalArgumentException("incorrect class");
        }


        check(clazz, new IdentityHashMap<Class<?>, Boolean>());
        return new MyBinder<T>(clazz);
    }

    private void check(Class<?> clazz, IdentityHashMap<Class<?>, Boolean> idHashMap) throws IllegalArgumentException {
        if (clazz == null) {
            throw new IllegalArgumentException("input is null");
        }

        if (clazz.isPrimitive() || clazz.isEnum() || clazz == String.class) {
            return;
        }

        if (clazz.isArray() || clazz.isInterface()) {
            throw new IllegalArgumentException(clazz.getName() + " is incorrect");
        }

        try {
            Constructor<?> cons = clazz.getDeclaredConstructor();
            if (!Modifier.isPublic(cons.getModifiers())) {
                throw new IllegalArgumentException(clazz.getName() + " constructor isn't public");
            }
        }   catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("There is no declared constructor in " + clazz.getName());
        }   catch (SecurityException e) {
            throw new IllegalArgumentException("SecurityException in " + clazz.getName() + " : " + e.toString());
        }
        Field[] flds = clazz.getDeclaredFields();
        for (Field fld : flds) {
            Class<?> cls = fld.getType();
            if (idHashMap.get(cls) != null) {
                continue;
            }
            idHashMap.put(cls, true);
            check(fld.getType(), idHashMap);
        }
    }
}

