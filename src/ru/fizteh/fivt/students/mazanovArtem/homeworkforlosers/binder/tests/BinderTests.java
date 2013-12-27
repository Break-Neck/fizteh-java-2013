package ru.fizteh.fivt.students.mazanovArtem.homeworkforlosers.binder.tests;

import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.binder.BinderFactory;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;
import ru.fizteh.fivt.binder.Binder;

import java.io.*;

import ru.fizteh.fivt.students.mazanovArtem.homeworkforlosers.binder.MyBinderFactory;

public class BinderTests {

    byte[] buf;
    public static BinderFactory binderFactory;
    public static Binder binder;
    public static ByteArrayOutputStream output;
    public static ByteArrayInputStream input;
    public static ByteArrayOutputStream output1;

    @Before
    public void init() throws FileNotFoundException {
        binderFactory = new MyBinderFactory();
        output = new ByteArrayOutputStream(1024);
        output1 = new ByteArrayOutputStream(1024);
        buf = new byte[1024];
        input = new ByteArrayInputStream(buf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyNullClass() throws IOException {
        binder = binderFactory.create(A.class);
        binder.serialize(null, output);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyNullOutput() throws IOException {
        binder = binderFactory.create(A.class);
        binder.serialize(new A(), null);
    }

    @Test
    public void goodTest() throws IOException {
        binder = binderFactory.create(A.class);
        binder.serialize(new A(), output);
        input = new ByteArrayInputStream(output.toByteArray());
        Object c = binder.deserialize(input);
        binder.serialize(c, output1);
    }

    public static class A {
        public int a;
        private double b;
        @Name("lort")
        public B trol;
        public  A() {
            a = 5;
            b = 12.5;
            trol = null;
        }
    }

    public static class B {
        public String str;
        @DoNotBind
        public String strr;
        //private Character s;
        public B() {
            str = "Vasya";
            strr = "Petya";
        }
    }

    public static class C {
        public C jlj;
    }
}
