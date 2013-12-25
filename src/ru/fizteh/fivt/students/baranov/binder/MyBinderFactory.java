package ru.fizteh.fivt.students.baranov.binder;

import ru.fizteh.fivt.binder.BinderFactory;

import ru.fizteh.fivt.binder.DoNotBind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;


public class MyBinderFactory implements BinderFactory {
    private HashSet<String> setOfClasses = new HashSet<>();

    public MyBinderFactory() {
        HashSet<String> set = new HashSet<>();
        set.add("#");
        this.setOfClasses = set;
    }

    public <T> MyBinder<T> create(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("class is null");
        }
        if (clazz.isPrimitive() || clazz.equals(String.class) || clazz.isEnum() || isWrapperType(clazz)) {
            throw new IllegalArgumentException("primitive and wrapper types not supported");
        }
        if (clazz.isArray()) {
            throw new IllegalArgumentException("arrays not supported");
        }
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("interfaces not supported");
        }

        setOfClasses.add(clazz.getSimpleName());

        Field[] fields = clazz.getDeclaredFields();
        int countOfUselessFields = 0;
        for (Field field : fields) {
            //if (field == null) {
            //    throw new IllegalArgumentException("field is null");
            //}
            if (field.getType().isArray()) {
                throw new IllegalArgumentException("arrays not supported");
            }
            if (field.getType().isInterface()) {
                throw new IllegalArgumentException("interfaces not supported");
            }
            Annotation[] annotationsOfField = field.getAnnotations();
            for (Annotation a : annotationsOfField) {
                if (a.annotationType().equals(DoNotBind.class)) {
                    countOfUselessFields++;
                    break;
                }
            }
            if (field.getType().isPrimitive() || field.getType().equals(String.class) || field.getType().isEnum()) {
                continue;
            } else {
                goThroughField(field);
            }
        }
        if (countOfUselessFields == fields.length) {
            throw new IllegalArgumentException("in class " + clazz.getSimpleName() + " nothing to serialize");
        }
        try {
            clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("class " + clazz.getSimpleName() + " don't have constructor");
        }

        MyBinder binder = new MyBinder(clazz);
        return binder;
    }

    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    private static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }

    private void goThroughField(Field field) {
        Class fieldType = field.getType();
        if (!setOfClasses.contains(fieldType.getSimpleName())) {
            setOfClasses.add(fieldType.getSimpleName());
            Field[] fields = fieldType.getDeclaredFields();
            for (Field f : fields) {
                if (f.getType().isArray()) {
                    throw new IllegalArgumentException("arrays not supported");
                }
                if (f.getType().isInterface()) {
                    throw new IllegalArgumentException("interfaces not supported");
                }
                if (f.getType().isPrimitive() || f.getType().equals(String.class) || f.getType().isEnum()) {
                    continue;
                } else {
                    goThroughField(field);
                }
            }
            try {
                fieldType.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("class " + fieldType.getSimpleName() + " don't have constructor");
            }
        }
    }
}
