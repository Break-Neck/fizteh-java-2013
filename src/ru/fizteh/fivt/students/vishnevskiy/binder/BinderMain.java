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
            binder.serialize(new Valid(), output);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

class Valid {
    private byte a;
    private int b;
    private long c;
    private float d;
    public double f;
    public boolean g;
    public char h;

    @Name("str")
    public String i;

    @DoNotBind
    private int[] j;

    public SubValid sub;

    public Valid() {
        b = 7;
        f = 8.56;
        h = 'h';
        i = "qwerty";
        sub = new SubValid();
    }
}

class SubValid {
    private int a;

    public SubValid() {
    }
}