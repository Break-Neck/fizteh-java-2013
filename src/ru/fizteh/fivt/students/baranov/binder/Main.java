package ru.fizteh.fivt.students.baranov.binder;

import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {


        String before = "<ru.fizteh.fivt.students.baranov.binder.User>" +
                "<name>Vasya</name>" +
                "<age>1</age>" +
                "<parent>" +
                "<ru.fizteh.fivt.students.baranov.binder.User>" +
                "<name>mamka</name>" +
                "<age>0</age>" +
                "</ru.fizteh.fivt.students.baranov.binder.User>" +
                "</parent>" +
                "</ru.fizteh.fivt.students.baranov.binder.User>";
        ByteArrayInputStream input = new ByteArrayInputStream(before.getBytes());

        MyBinderFactory factory = new MyBinderFactory();
        try {
            MyBinder binder = factory.create(User.class);
            User user = (User)binder.deserialize(input);
            System.err.println(user.name);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
    }
}

