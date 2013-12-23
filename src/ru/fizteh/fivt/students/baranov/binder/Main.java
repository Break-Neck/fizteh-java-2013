package ru.fizteh.fivt.students.baranov.binder;

import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        class User {
            User () {}
            private int id;
            public String name;
        }

        //MyClass c = new MyClass();
        //c.ololo = new MyClass2();
        //c.first = new int[]{1, 2};
        //c.second = "anton";
        //c.fourth = 42;
        //c.xyu = null;
        //c.ololo.age = 19;
        //User user = new User();
        //user.name = "Anton";
        //user.permission.role = Role.USER;
        //byte[] buf = user.toString().getBytes();
        String task = "<ru.fizteh.fivt.students.baranov.binder.MyClass><first>1</first><second>anton</second></ru.fizteh.fivt.students.baranov.binder.MyClass>";
        byte[] buf = task.getBytes();
        ByteArrayInputStream iStream = new ByteArrayInputStream(buf);

        MyBinderFactory factory = new MyBinderFactory();
        try {
            MyBinder binder = factory.create(MyClass.class, 0);
            //binder.serialize(c, System.out);
            MyClass d = new MyClass();
            d = (MyClass)binder.deserialize(iStream);
            System.err.println(d.first);
            System.err.println(d.second);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println(e.getMessage());
            return;
        }
        System.err.println("OK");
    }
}

