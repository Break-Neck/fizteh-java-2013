package ru.fizteh.fivt.students.dobrinevski.binder;

import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.BinderFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class MyBinderFactory implements BinderFactory {

    public <T> Binder<T> create(Class<T> clazz) throws IllegalArgumentException {
        check(clazz);
        return new MyBinder<T>(clazz);
    }

    private void check(Class<?> clazz) throws IllegalArgumentException {
        if (clazz == null) {
            throw new IllegalArgumentException("input is null");
        }

        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException("incorrect class");
        }

        if (clazz.isArray()) {
            throw new IllegalArgumentException(clazz.getName() + " is array");
        }

        try {
            Constructor<?> cons = clazz.getConstructor();
            if (!Modifier.isPublic(cons.getModifiers())) {
                throw new IllegalArgumentException(clazz.getName() + " constructor isn't public");
            }
        }   catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("There is no declared constructor in " + clazz.getName());
        }   catch (SecurityException e) {
            throw new IllegalArgumentException("SecurityException in " + clazz.getName() + " : " + e.toString());
        }


}
}

