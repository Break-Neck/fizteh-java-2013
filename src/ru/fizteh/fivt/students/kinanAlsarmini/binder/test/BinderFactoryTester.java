package ru.fizteh.fivt.students.kinanAlsarmini.binder.test;

import ru.fizteh.fivt.students.kinanAlsarmini.binder.MyBinder;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.MyBinderFactory;

import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.NestedArray;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.NoConstructor;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.NestedNoConstructor;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.AllPrimitivesOuter;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.test.hclasses.AllPrimitivesInner;

import org.junit.*;
import static org.junit.Assert.*;

enum Day {
    MONDAY;
}

public class BinderFactoryTester {
    private MyBinderFactory factory;

    @Before
    public void before() {
        factory = new MyBinderFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullTest() {
        factory.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void arrayTest() {
        factory.create(int[].class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void arrayInsideTest() {
        factory.create(NestedArray.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noDefaultConstructor() {
        factory.create(NoConstructor.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noDefaultConstructSubclass() {
        factory.create(NestedNoConstructor.class);
    }

    @Test
    public void successfulTest() {
        factory.create(AllPrimitivesOuter.class);
    }

    @Test
    public void checkCache() {
        MyBinder<AllPrimitivesOuter> a = factory.create(AllPrimitivesOuter.class);
        MyBinder<AllPrimitivesOuter> b = factory.create(AllPrimitivesOuter.class);

        Assert.assertEquals(a, b);
    }
}
