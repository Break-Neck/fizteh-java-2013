package ru.fizteh.fivt.students.mazanovArtem.homeworkforlosers.binder.tests;

import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.binder.BinderFactory;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;
import ru.fizteh.fivt.students.mazanovArtem.homeworkforlosers.binder.MyBinderFactory;

public class BinderFactoryTests {

    public static BinderFactory binderFactory;

    @Before
    public void init() {
        binderFactory = new MyBinderFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyNullInput() {
        binderFactory.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyIsPrimitive() {
        binderFactory.create(char.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyIsArray() {
        binderFactory.create(int[].class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void classHavenotConstructor() {
        binderFactory.create(A.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void classHavenotpublicConstructor() {
        binderFactory.create(B.class);
    }

    @Test
    public void goodInput() {
        binderFactory.create(C.class);
    }

    public static class A {
        private A() {
            a = 2;
            b = 123;
        }
        private int a;
        public int b;
    }

    public static class B {
        private int a;
        public int b;
        B() {
            a = 1;
            b = 2;
        }
    }

    public static class C {
        @DoNotBind
        private int r;
        @Name("ololo")
        public double o;
        public C() {
            r = 0;
            o = 42.5;
        }
    }
}
