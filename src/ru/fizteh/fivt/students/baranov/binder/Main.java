package ru.fizteh.fivt.students.baranov.binder;

import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        class User {
            User() {
            }

            private int id;
            public String name;
        }

        MyClass c = new MyClass();
        c.ololo = new MyClass2();
        c.first = 1;
        c.second = "anton";
        c.fourth = 42;
        c.xyu = null;
        c.ololo.age = 19;
        String task = "<ru.fizteh.fivt.students.baranov.binder.MyClass><first>1</first><second>anton</second><ololo>" +
                "<ru.fizteh.fivt.students.baranov.binder.MyClass2>" +
                "<age>19</age>" +
                "</ru.fizteh.fivt.students.baranov.binder.MyClass2>" +
                "</ololo></ru.fizteh.fivt.students.baranov.binder.MyClass>";
        byte[] buf = task.getBytes();
        ByteArrayInputStream iStream = new ByteArrayInputStream(buf);

        MyBinderFactory factory = new MyBinderFactory();
        try {
            MyBinder binder = factory.create(MyClass.class);
            binder.serialize(c, System.out);
            System.out.println("------------");
            MyClass d = (MyClass) binder.deserialize(iStream);
            System.out.println(d.first);
            System.out.println(d.second);
            System.out.println(d.ololo.age);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println(e.getMessage());
            return;
        }
        System.out.println("------------");
        System.err.println("OK");
    }
}

