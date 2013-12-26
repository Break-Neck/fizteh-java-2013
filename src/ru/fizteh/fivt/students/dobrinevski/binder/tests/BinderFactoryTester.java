package ru.fizteh.fivt.students.dobrinevski.binder.tests;

import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.binder.BinderFactory;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;
import ru.fizteh.fivt.students.dobrinevski.binder.MyBinderFactory;

import java.util.List;

public class BinderFactoryTester {
    public static BinderFactory bf;

    public static class B {
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

    public static class A {
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

    public static class WithoutConstructor {
        int f;
        char g;
        A h;
        public WithoutConstructor(String a) {

        }
    }

    public static class WithPrivateConstructor {
        int a;
        double b;
        private WithPrivateConstructor() {
            a = 0;
            b = 0;
        }
    }

    public interface Inter {
    }

    @Before
    public void init()  {
        bf = new MyBinderFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyInterfaceGiven() {
        bf.create(Inter.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyListGiven() {
        bf.create(List.class);
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
    public void keyEnumGiven() {
        bf.create(Enum.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void classWithoutConstructorGiven() {
        bf.create(WithoutConstructor.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void classWithPrivateConstructorGiven() {
        bf.create(WithPrivateConstructor.class);
    }

    @Test
    public void goodInput() {
        bf.create(A.class);
    }
}

