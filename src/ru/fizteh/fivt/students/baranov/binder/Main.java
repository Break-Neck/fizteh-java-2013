package ru.fizteh.fivt.students.baranov.binder;

import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        MyBinderFactory factory = new MyBinderFactory();
        try {
            MyBinder binder = factory.create(User.class);
            User user = new User();
            user.name = "Vasya";
            user.id = 1;
            user.howOldAreYou = 1;
            user.parent = new User();
            user.parent.name = "mamka";
            //output = null;
            binder.serialize(user, System.out);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
    }
}

