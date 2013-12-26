package ru.fizteh.fivt.students.kinanAlsarmini.binder.test;

import ru.fizteh.fivt.students.kinanAlsarmini.binder.MyBinder;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.MyBinderFactory;

import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.CyclicA;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.CyclicB;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.SerializableInner;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.SerializableOuter;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.Day;

import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.*;
import static org.junit.Assert.*;


public class BinderTester {
    MyBinderFactory factory;
    ByteArrayInputStream input;
    ByteArrayOutputStream output;

    @Before
    public void before() {
        factory = new MyBinderFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void serializeNullTest() throws IOException {
        MyBinder<String> binder = factory.create(String.class);
        binder.serialize(null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void circularReferenceTest() throws IOException {
        CyclicA test = new CyclicA();
        test.b = new CyclicB();
        test.b.a = test;

        output = new ByteArrayOutputStream();

        MyBinder<CyclicA> binder = factory.create(CyclicA.class);
        binder.serialize(test, output);
    }

    @Test
    public void successfulSerializationTest() throws IOException {
        output = new ByteArrayOutputStream();

        SerializableOuter test = new SerializableOuter(Day.SATURDAY, (long)5, "abc", true, 5);

        MyBinder<SerializableOuter> binder = factory.create(SerializableOuter.class);

        binder.serialize(test, output);
        Assert.assertEquals("<SerializableOuter>"
                + "<day>SATURDAY</day><a>5</a><b>abc</b><c>true</c><empty></empty>"
                + "<New>ABC</New>"
                + "</SerializableOuter>", output.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deserializeNullTest() throws IOException {
        MyBinder<String> binder = factory.create(String.class);
        binder.deserialize(null);
    }

    @Test
    public void successfulDeserializationTest() throws IOException {
        SerializableOuter test = new SerializableOuter(Day.SATURDAY, (long)5, "abc", true, 0);

        MyBinder<SerializableOuter> binder = factory.create(SerializableOuter.class);

        String serialization = "<SerializableOuter>"
                + "<day>SATURDAY</day><a>5</a><b>abc</b><c>true</c><empty></empty>"
                + "<New>ABC"
                + "</New></SerializableOuter>";

        input = new ByteArrayInputStream(serialization.getBytes());
        SerializableOuter get = binder.deserialize(input);
        Assert.assertEquals(test, test);
    }
}
