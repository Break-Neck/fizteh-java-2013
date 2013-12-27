package ru.fizteh.fivt.students.mazanovArtem.homeworkforlosers.binder;

import ru.fizteh.fivt.binder.BinderFactory;

public class MyBinderFactory implements BinderFactory{

    public <T> MyBinder<T> create(Class<T> clazz) {
        return new MyBinder<>(clazz);
    }

}
