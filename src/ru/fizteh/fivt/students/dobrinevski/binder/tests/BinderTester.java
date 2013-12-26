package ru.fizteh.fivt.students.dobrinevski.binder.tests;

import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.BinderFactory;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;
import ru.fizteh.fivt.students.dobrinevski.binder.MyBinderFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class BinderTester {
    byte[] buf;
    public static BinderFactory bf;
    public static Binder bind;
    public static ByteArrayOutputStream outStream;
    public static ByteArrayInputStream inStream;

    public static class IntAndDouble {
        int a;
        double b;
        public IntAndDouble() {
            a = 5;
            b = 25;
        }
    }
    public static class C {
        @DoNotBind
        public int f;
        public char g;
        public double h;
        public C() {
            f = 1;
            g = 'A';
            h = 0.69;
        }
        @Override
        public boolean equals(Object a) {
            if (f == ((C) a).f && g == ((C) a).g && h == ((C) a).h) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return f + (int) g + (int) h;
        }
    }
    public static class D {
        int f;
        char g;
        @Name("ololo")
        String h;
        C add;
        public D() {
            f = 96;
            g = 'Z';
            h = "How to buy pig?";
           // add = new C();
        }
        @Override
        public boolean equals(Object a) {
            if (f == ((D) a).f && g == ((D) a).g && h.equals(((D) a).h) && add.equals(((D) a).add)) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return f + (int) g + hashCode() + add.hashCode();
        }
    }
    public static class Inner {
        public Inner() {

        }
    }
    public static class Outer {
        Inner i = new Inner();
        Inner i1 = i;
        Inner i2 = i;
        public Outer() {

        }
    }
    public static class EqualName {
        int a;
        @Name("a")
        int b;
    }


    @Before
    public void init()  {
        bf = new MyBinderFactory();
        outStream = new ByteArrayOutputStream();
        buf = new byte[1024];
        inStream = new ByteArrayInputStream(buf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyNullGivenSerialisation() throws IOException {
        bind = bf.create(IntAndDouble.class);
        bind.serialize(null, outStream);
    }

    @Test (expected = IllegalArgumentException.class)
    public void streamNullSerialisation() throws IOException {
        bind = bf.create(IntAndDouble.class);
        bind.serialize(new IntAndDouble(), null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void classWithEqualName() throws IOException {
        bind = bf.create(EqualName.class);
        bind.serialize(new EqualName(), new ByteArrayOutputStream());
    }

    @Test
    public void mainTestSerDeser() throws IOException {
        bind = bf.create(D.class);
        D begin = new D();
       /* bind.serialize(begin, outStream);
        assertEquals(outStream.toString(),
                "{\"f\":\"96\",\"g\":\"Z\",\"ololo\":\"How to buy pig?\",\"add\":{\"g\":\"A\",\"h\":\"0.69\"}}"); */
        buf = "{\"f\":\"96\",\"g\":\"Z\",\"ololo\":\"How to buy pig?\",\"add\":{\"g\":\"A\",\"h\":\"0.69\"}}"
                .getBytes();
        inStream = new ByteArrayInputStream(buf);
        D test = (D) bind.deserialize(inStream);
        assertEquals(begin, test);
    }

    @Test
    public void testKomanov() throws IOException {
        bind = bf.create(Outer.class);
        bind.serialize(new Outer(), outStream);
        assertEquals("{\"i\":{},\"i1\":{},\"i2\":{}}", outStream.toString());
    }
}

