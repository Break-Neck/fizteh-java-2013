package ru.fizteh.fivt.students.dobrinevski.binder.tests;

import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.binder.BinderFactory;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;
import ru.fizteh.fivt.students.dobrinevski.binder.MyBinderFactory;

public class BinderFactoryTester {
    public static BinderFactory bf;

    @Before
    public void init()  {
        bf = new MyBinderFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyNullGiven() {
        bf.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void primitiveClassGiven() {
        bf.create(int.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyArrayGiven() {
        bf.create(Integer[].class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void classWithoutConstructorGiven() {
        bf.create(C.class);
    }

    @Test
    public void goodInput() {
        bf.create(A.class);
    }
}

class B {
    @DoNotBind
    int f;
    char g;
    double h;
    public B() {
        f = 1;
        g = 'A';
        h = 0.69;
    }
}
class A {
    int f;
    char g;
    @Name("ololo")
    String h;
    B add;
    public A() {
        f = 96;
        g = 'Z';
        h = "How to buy pig?";
        add = new B();
    }
}
class C {
    int f;
    char g;
}
