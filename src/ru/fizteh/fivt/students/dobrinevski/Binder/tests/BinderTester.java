package ru.fizteh.fivt.students.dobrinevski.Binder.tests;

import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.BinderFactory;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;
import ru.fizteh.fivt.students.dobrinevski.Binder.MyBinderFactory;

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

    @Test
    public void mainTestSerDeser() throws IOException {
        bind = bf.create(D.class);
        bind.serialize(new D(), outStream);
        assertEquals(outStream.toString() , "{\"f\":\"96\",\"g\":\"Z\",\"ololo\":\"How to buy pig?\",\"add\":{\"g\":\"A\",\"h\":\"0.69\"}}");
        buf = "{\"f\":\"96\",\"g\":\"Z\",\"ololo\":\"How to buy pig?\",\"add\":{\"g\":\"A\",\"h\":\"0.69\"}}".getBytes();
    }
}
class IntAndDouble {
    int a;
    double b;
    public IntAndDouble() {
        a = 5;
        b = 25;
    }
}
class C {
    @DoNotBind
    int f;
    char g;
    double h;
    public C() {
        f = 1;
        g = 'A';
        h = 0.69;
    }
}
class D {
    int f;
    char g;
    @Name("ololo")
    String h;
    C add;
    public D() {
        f = 96;
        g = 'Z';
        h = "How to buy pig?";
        add = new C();
    }
}
