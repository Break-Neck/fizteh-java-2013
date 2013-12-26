package ru.fizteh.fivt.students.vishnevskiy.binder.unitTests;

import ru.fizteh.fivt.binder.*;
import ru.fizteh.fivt.students.vishnevskiy.binder.MyBinderFactory;
import org.junit.Before;
import org.junit.Test;

public class MyBinderFactoryTest {
    public static BinderFactory binderFactory;

    @Before
    public void init() {
        binderFactory = new MyBinderFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void classIsNull() {
        binderFactory.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void classIsArray() {
        binderFactory.create(byte[].class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void classHasNoDefaultConstructor() {
        binderFactory.create(NoDefaultConstructor.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void classHasInvalidField() {
        binderFactory.create(InvalidField.class);
    }

    @Test
    public void valid() {
        binderFactory.create(Valid.class);
    }

}

class NoDefaultConstructor {
    private int key;

    NoDefaultConstructor(int key) {
        this.key = key;
    }
}

class InvalidField {
    private int a;
    private int[] b;
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
    }
}

class SubValid {
    private int a;

    public SubValid() {
    }
}
