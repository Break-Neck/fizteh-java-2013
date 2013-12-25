package ru.fizteh.fivt.students.baranov.binder;

import ru.fizteh.fivt.binder.BinderFactory;

import ru.fizteh.fivt.binder.DoNotBind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;


public class MyBinderFactory implements BinderFactory {
    public HashSet<String> setOfClasses = new HashSet<>();

    public MyBinderFactory() {
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

        if (!setOfClasses.contains(clazz.getName())) {
            setOfClasses.add(clazz.getSimpleName());

            Field[] fields = clazz.getDeclaredFields();
            int countOfUselessFields = 0;
            for (Field field : fields) {
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
                } else if (!field.getType().equals(clazz)) {
                    MyBinderFactory f = new MyBinderFactory();
                    f.setOfClasses = setOfClasses;
                    f.create(field.getType());
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
        }
        MyBinder binder = new MyBinder(clazz);
        binder.setOfClasses = setOfClasses;
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
}
