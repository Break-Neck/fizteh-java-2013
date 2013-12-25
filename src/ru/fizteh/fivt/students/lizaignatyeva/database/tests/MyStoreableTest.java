package ru.fizteh.fivt.students.lizaignatyeva.database.tests;

import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.students.lizaignatyeva.database.MyStoreable;
import ru.fizteh.fivt.students.lizaignatyeva.database.StoreableSignature;

import java.text.ParseException;
import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

public class MyStoreableTest {
    private StoreableSignature storeableSignature;
    private ArrayList<Class<?>> classes;
    private ArrayList<Object> values;


    @Before
    public void init() {
        classes = new ArrayList<>();
        values = new ArrayList<>();
        classes.add(Integer.class);
        values.add(Integer.valueOf(1));
        classes.add(Long.class);
        values.add((long) 2);
        classes.add(Byte.class);
        values.add((byte) 3);
        classes.add(Float.class);
        values.add((float) 3.14);
        classes.add(Double.class);
        values.add(2.71);
        classes.add(Boolean.class);
        values.add(true);
        classes.add(String.class);
        values.add("test");
        storeableSignature = new StoreableSignature(classes);
    }

    @Test
    public void testNullStoreable() throws ParseException {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        String serialized = storeable.serialize();
        MyStoreable newStoreable = new MyStoreable(storeableSignature);
        newStoreable.deserialize(serialized);
        assertEquals(newStoreable, storeable);
    }

    @Test
    public void testBasics() throws ParseException {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        for (int i = 0; i < storeable.size(); i++) {
            storeable.setColumnAt(i, values.get(i));
        }
        String serialized = storeable.serialize();
        MyStoreable newStoreable = new MyStoreable(storeableSignature);
        newStoreable.deserialize(serialized);
        assertEquals(newStoreable, storeable);
        assertEquals(storeable.getIntAt(0), Integer.valueOf(1));
        assertEquals(storeable.getLongAt(1), Long.valueOf(2));
        assertEquals(storeable.getByteAt(2), Byte.valueOf((byte) 3));
        assertEquals(storeable.getFloatAt(3), Float.valueOf("3.14"));
        assertEquals(storeable.getDoubleAt(4), 2.71);
        assertEquals(storeable.getBooleanAt(5), Boolean.TRUE);
        assertEquals(storeable.getStringAt(6), "test");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testLesserBound() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.getColumnAt(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpperBound() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.getColumnAt(storeable.size());
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest01() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(0, values.get(1));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest02() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(0, values.get(2));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest03() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(0, values.get(3));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest04() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(0, values.get(4));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest05() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(0, values.get(5));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest06() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(0, values.get(6));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest10() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(1, values.get(0));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest12() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(1, values.get(2));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest13() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(1, values.get(3));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest14() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(1, values.get(4));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest15() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(1, values.get(5));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest16() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(1, values.get(6));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest20() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(2, values.get(0));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest21() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(2, values.get(1));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest23() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(2, values.get(3));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest24() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(2, values.get(4));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest25() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(2, values.get(5));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest26() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(2, values.get(6));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest30() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(3, values.get(0));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest31() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(3, values.get(1));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest32() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(3, values.get(2));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest34() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(3, values.get(4));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest35() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(3, values.get(5));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest36() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(3, values.get(6));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest40() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(4, values.get(0));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest41() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(4, values.get(1));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest42() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(4, values.get(2));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest43() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(4, values.get(3));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest45() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(4, values.get(5));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest46() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(4, values.get(6));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest50() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(5, values.get(0));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest51() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(5, values.get(1));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest52() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(5, values.get(2));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest53() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(5, values.get(3));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest54() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(5, values.get(4));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest56() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(5, values.get(6));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest60() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(6, values.get(0));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest61() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(6, values.get(1));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest62() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(6, values.get(2));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest63() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(6, values.get(3));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest64() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(6, values.get(4));
    }

    @Test(expected = ColumnFormatException.class)
    public void testIncorrectSetRequest65() {
        MyStoreable storeable = new MyStoreable(storeableSignature);
        storeable.setColumnAt(6, values.get(5));
    }

}
