package ru.fizteh.fivt.students.lizaignatyeva.database.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.lizaignatyeva.database.*;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

public class MyTableProviderTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private MyTableProvider myTableProvider;

    private static final List<Class<?>> COLUMN_TYPES = new ArrayList<>();

    static {
        COLUMN_TYPES.add(String.class);
        COLUMN_TYPES.add(Integer.class);
    }

    @Before
    public void createTableProvider() {
        myTableProvider = new MyTableProvider(temporaryFolder.getRoot().toPath());
    }

    @After
    public void cleanup() {
        for (File file : temporaryFolder.getRoot().listFiles()) {
            FileUtils.remove(file);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullGetTable() {
        myTableProvider.getTable(null);
    }

    @Test
    public void getTableTest() throws IOException {
        myTableProvider.createTable("default", COLUMN_TYPES);
        MyTable table1 = myTableProvider.getTable("default");
        MyTable table2 = myTableProvider.getTable("default");
        assertNotNull(table1);
        assertTrue(table1 == table2);
    }

    @Test
    public void createTableTest() throws IOException {
        MyTable table = myTableProvider.createTable("default", COLUMN_TYPES);
        assertNotNull(table);
        assertNull(myTableProvider.createTable("default", COLUMN_TYPES));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNullName() throws IOException {
        myTableProvider.createTable(null, COLUMN_TYPES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNullColumns() throws IOException {
        myTableProvider.createTable("default", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEmptyColumns() throws IOException {
        myTableProvider.createTable("default", new ArrayList<Class<?>>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNullColumn() throws IOException {
        List<Class<?>> columnTypes = new ArrayList<Class<?>>();
        columnTypes.add(null);
        myTableProvider.createTable("default", columnTypes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIllegalType() throws IOException {
        List<Class<?>> columnTypes = new ArrayList<Class<?>>();
        columnTypes.add(BigInteger.class);
        myTableProvider.createTable("default", columnTypes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullName() {
        myTableProvider.removeTable(null);
    }

    @Test
    public void testReadWrite() throws IOException {
        final String tableName = "test";
        Storeable value1 = new MyStoreable(new StoreableSignature(COLUMN_TYPES));
        value1.setColumnAt(0, "testValue");
        value1.setColumnAt(1, 1);
        Storeable value2 = new MyStoreable(new StoreableSignature(COLUMN_TYPES));

        {
            MyTableProvider tableProvider = new MyTableProvider(temporaryFolder.getRoot().toPath());
            MyTable table = tableProvider.createTable(tableName, COLUMN_TYPES);
            table.put("key1", value1);
            table.put("key2", value2);
            table.commit();
        }

        {
            MyTableProvider tableProvider = new MyTableProvider(temporaryFolder.getRoot().toPath());
            MyTable table = tableProvider.getTable(tableName);
            assertEquals(table.get("key1"), value1);
            assertEquals(table.get("key2"), value2);
        }
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException, ParseException {
        MyTable table = myTableProvider.createTable("default", COLUMN_TYPES);
        Storeable value1 = myTableProvider.createFor(table);
        value1.setColumnAt(0, "foo");
        value1.setColumnAt(1, 1);
        String serialized1 = myTableProvider.serialize(table, value1);
        assertEquals(serialized1, "[\"foo\",1]");
        Storeable deserialized1 = myTableProvider.deserialize(table, serialized1);
        assertEquals(deserialized1, value1);
        Storeable value2 = myTableProvider.createFor(table);
        String serialized2 = myTableProvider.serialize(table, value2);
        assertEquals(serialized2, "[null,null]");
        Storeable deserialized2 = myTableProvider.deserialize(table, serialized2);
        assertEquals(deserialized2, value2);
    }

    @Test(expected = ParseException.class)
    public void testDeserializeTrash() throws IOException, ParseException {
        String trash = "ThisIsNotAJson";
        MyTable table = myTableProvider.createTable("default", COLUMN_TYPES);
        myTableProvider.deserialize(table, trash);
    }

    @Test(expected = ParseException.class)
    public void testDeserializeIncorrectTypes() throws IOException, ParseException {
        String deserialized = "[false, 0]";
        MyTable table = myTableProvider.createTable("default", COLUMN_TYPES);
        myTableProvider.deserialize(table, deserialized);
    }
}
