package ru.fizteh.fivt.students.baranov.binder;

import ru.fizteh.fivt.binder.BinderFactory;

import ru.fizteh.fivt.binder.DoNotBind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;


public class MyBinderFactory implements BinderFactory {
    public MyBinderFactory() {
    }

    public <T> MyBinder<T> create(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("class " + clazz.getName() + " is null");
        }
        if (clazz.isPrimitive() || clazz.equals(String.class)) {
            return new MyBinder(clazz);
        }
        if (clazz.isArray()) {
            throw new IllegalArgumentException("arrays not supported");
        }
        Field[] fields = clazz.getDeclaredFields();
        int countOfUselessFields = 0;
        for (Field field : fields) {
            if (field.getType().isArray()) {
                throw new IllegalArgumentException("arrays not supported");
            }
            Annotation[] annotationsOfField = field.getAnnotations();
            for (Annotation a : annotationsOfField) {
                if (a.equals(DoNotBind.class)) {
                    countOfUselessFields++;
                    break;
                }
            }
        }
        if (countOfUselessFields == fields.length) {
            throw new IllegalArgumentException("in class " + clazz.getName() + " nothing to serialize");
        }
        try {
            clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("class " + clazz.getName() + " don't have constructor");
        }

        return new MyBinder(clazz);
    }
}
