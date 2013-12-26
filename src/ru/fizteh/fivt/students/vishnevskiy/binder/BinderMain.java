package ru.fizteh.fivt.students.vishnevskiy.binder;

import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BinderMain {

    public static void main(String[] args) {
        MyBinderFactory factory = new MyBinderFactory();
        MyBinder binder = factory.create(Valid.class);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            binder.serialize(new Circular(), output);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
class Circular {
    private int a;
    SubCircular sub;
}

class SubCircular {
    public float a;
    Circular circ;
}

class Valid {
    private int b;

    public SubValid sub;
    public SubValid sub2;
    public Valid() {
        b = 7;
        sub = new SubValid();
        sub2 = new SubValid();
    }
}

class SubValid {
    private int a;

    public SubValid() {
    }
}