package ru.fizteh.fivt.students.baranov.binder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BinderTests {
    private ByteArrayInputStream input;
    private ByteArrayOutputStream output;
    private MyBinderFactory factory;

    @Before
    public void init() {
        factory = new MyBinderFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBinderWithNullClass() {
        factory.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createAllFieldsDoNotBind() {
        factory.create(UselessClass.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNoConstructor() {
        factory.create(MyClassWithoutConstructor.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void serializeValueNull() throws IOException {
        MyBinder binder = factory.create(MyUser.class);
        MyUser object = null;
        output = new ByteArrayOutputStream();
        binder.serialize(object, output);
    }

    @Test(expected = IllegalArgumentException.class)
    public void serializeOutputNull() throws IOException {
        MyBinder binder = factory.create(MyUser.class);
        MyUser object = new MyUser();
        object.name = "anton";
        object.id = 42;
        object.howOldAreYou = 19;
        output = null;
        binder.serialize(object, output);
    }

    @Test(expected = IllegalStateException.class)
    public void serializeCircularLink() throws IOException {
        MyBinder binder = factory.create(MyUser.class);
        MyUser object = new MyUser();
        object.name = "Vasya";
        object.parent = object;
        output = new ByteArrayOutputStream();
        binder.serialize(object, output);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deserializeInputNull() throws IOException {
        MyBinder binder = factory.create(MyUser.class);
        input = null;
        binder.deserialize(input);
    }

    @Test
    public void serializeSimpleTest() throws IOException {
        MyBinder binder = factory.create(MyUser.class);
        MyUser user = new MyUser();
        user.name = "Anton";
        user.id = 1;
        user.howOldAreYou = 19;
        output = new ByteArrayOutputStream();
        binder.serialize(user, output);
        String expectedResult = "<MyUser><name>Anton</name><age>19</age></MyUser>";
        assertEquals("serialize: ", expectedResult, output.toString());
    }

    @Test
    public void serializeTest() throws IOException {
        MyBinder binder = factory.create(MyUser.class);
        MyUser user = new MyUser();
        user.name = "Vasya";
        user.id = 1;
        user.howOldAreYou = 1;
        user.parent = new MyUser();
        user.parent.name = "mamka";
        output = new ByteArrayOutputStream();
        binder.serialize(user, output);
        String expectedResult = "<MyUser><name>Vasya</name><age>1</age><parent><MyUser>"
                + "<name>mamka</name><age>0</age></MyUser></parent></MyUser>";
        assertEquals("serialize: ", expectedResult, output.toString());
    }

    @Test
    public void deserializeEqualsSerialize() throws IOException {
        MyBinder binder = factory.create(MyUser.class);
        MyUser expectedUser = new MyUser();
        expectedUser.name = "Vasya";
        expectedUser.id = 1;
        expectedUser.howOldAreYou = 1;
        expectedUser.parent = new MyUser();
        expectedUser.parent.name = "mamka";
        String before = "<MyUser><name>Vasya</name><age>1</age><parent><MyUser>"
                + "<name>mamka</name><age>0</age></MyUser></parent></MyUser>";

        input = new ByteArrayInputStream(before.getBytes());
        output = new ByteArrayOutputStream();
        MyUser user = (MyUser) binder.deserialize(input);
        binder.serialize(user, output);

        assertEquals("results: ", before, output.toString());
    }

    @Test(expected = IOException.class)
    public void parsingXMLError() throws IOException {
        try {
            MyBinder binder = factory.create(MyUser.class);
            String before = "<MyUser><name>Vasya</name><age>1</age><parent>";
            input = new ByteArrayInputStream(before.getBytes());
            binder.deserialize(input);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBinderForClassContainsArray() throws IOException {
        factory.create(WithArray.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBinderForClassContainsInterface() throws IOException {
        factory.create(WithInterface.class);
    }
}

class MyClassWithoutConstructor {
    int number;
}

class UselessClass {
    public UselessClass() {
    }

    @DoNotBind
    boolean foo;
    @DoNotBind
    int something;
}

class MyUser {
    public MyUser() {
    }

    String name;
    @DoNotBind
    int id;
    @Name("age")
    int howOldAreYou;
    MyUser parent;
}

class WithArray {
    public WithArray() {
    }

    int[] lol;
}

class WithInterface {
    public WithInterface() {
    }

    InterFace lol;
}

interface InterFace {

}
