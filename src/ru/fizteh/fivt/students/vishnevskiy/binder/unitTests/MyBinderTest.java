package ru.fizteh.fivt.students.vishnevskiy.binder.unitTests;

import ru.fizteh.fivt.binder.*;
import ru.fizteh.fivt.students.vishnevskiy.binder.*;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MyBinderTest {
    public static MyBinderFactory binderFactory;
    public static MyBinder binder;
    public static ByteArrayOutputStream output;
    public static ByteArrayInputStream input;

    @Before
    public void init() {
        binderFactory = new MyBinderFactory();
        binder = binderFactory.create(Valid.class);
        byte[] buffer = new byte[2048];
        input = new ByteArrayInputStream(buffer);
        output = new ByteArrayOutputStream();
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueIsNull() throws IOException {
        binder.serialize(null, output);
    }

    @Test(expected = IllegalArgumentException.class)
    public void outputIsNull() throws IOException {
        binder.serialize(new Valid(), null);
    }

    @Test(expected = IllegalStateException.class)
    public void circularReference() throws IOException {
        binder.serialize(new Circular(), output);
    }

    @Test
    public void mainTestSerDeser() throws IOException {
        binder.serialize(new Valid(), output);
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
