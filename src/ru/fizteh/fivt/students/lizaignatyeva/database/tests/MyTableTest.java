package ru.fizteh.fivt.students.lizaignatyeva.database.tests;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.lizaignatyeva.database.MyStoreable;
import ru.fizteh.fivt.students.lizaignatyeva.database.MyTable;
import ru.fizteh.fivt.students.lizaignatyeva.database.MyTableProvider;
import ru.fizteh.fivt.students.lizaignatyeva.database.StoreableSignature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class MyTableTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private MyTableProvider tableProvider;
    private MyTable table;
    private final String tableName = "testTable";
    private static final List<Class<?>> COLUMN_TYPES = new ArrayList<>();
    static {
        COLUMN_TYPES.add(Integer.class);
        COLUMN_TYPES.add(String.class);
    }

    private static final MyStoreable EMPTY_INCORRECT_COLUMNS;
    private static final MyStoreable NON_EMPTY_INCORRECT_COLUMNS;
    static {
        List<Class<?>> incorrectColumnTypes = new ArrayList<>();
        incorrectColumnTypes.add(String.class);
        incorrectColumnTypes.add(Integer.class);
        EMPTY_INCORRECT_COLUMNS = new MyStoreable(new StoreableSignature(incorrectColumnTypes));
        NON_EMPTY_INCORRECT_COLUMNS = new MyStoreable(new StoreableSignature(incorrectColumnTypes));
        NON_EMPTY_INCORRECT_COLUMNS.setColumnAt(0, "testValue");
        NON_EMPTY_INCORRECT_COLUMNS.setColumnAt(1, 123456789);
    }

    private static final MyStoreable SMALL_COLUMNS_COUNT;
    static {
        List<Class<?>> smallColumnTypes = new ArrayList<>();
        smallColumnTypes.add(Integer.class);
        SMALL_COLUMNS_COUNT = new MyStoreable(new StoreableSignature(smallColumnTypes));
    }

    private static final MyStoreable LARGE_COLUMNS_COUNT;
    static {
        List<Class<?>> largeColumnTypes = new ArrayList<>();
        largeColumnTypes.add(Integer.class);
        largeColumnTypes.add(String.class);
        largeColumnTypes.add(Boolean.class);
        LARGE_COLUMNS_COUNT = new MyStoreable(new StoreableSignature(largeColumnTypes));
    }

    private Storeable value1;
    private Storeable value2;
    private Storeable value3;


    @Before
    public void createTable() throws IOException {
        tableProvider = new MyTableProvider(folder.getRoot().toPath());
        table = tableProvider.createTable(tableName, COLUMN_TYPES);
        assertNotNull(table);
        value1 = tableProvider.createFor(table);
        value1.setColumnAt(0, 1);
        value1.setColumnAt(1, "value1");
        value2 = tableProvider.createFor(table);
        value2.setColumnAt(0, 2);
        value2.setColumnAt(1, "value2");
        value3 = tableProvider.createFor(table);
        value3.setColumnAt(0, 3);
        value3.setColumnAt(1, "value3");
    }

    @Test
    public void testGetName() {
        assertEquals(tableName, table.getName());
    }

    @Test
    public void testGetColumnsCount() {
        assertEquals(COLUMN_TYPES.size(), table.getColumnsCount());
    }

    @Test
    public void testGetColumnClass() {
        for (int index = 0; index < COLUMN_TYPES.size(); index ++) {
            assertEquals(table.getColumnType(index), COLUMN_TYPES.get(index));
        }
    }

    @Test
    public void testBasics() {
        final String key = "testKey";

        assertEquals(table.get(key), null);
        assertEquals(table.put(key, value1), null);
        assertEquals(table.get(key), value1);
        assertEquals(table.put(key, value2), value1);
        assertEquals(table.remove(key), value2);
        assertEquals(table.get(key), null);
        assertEquals(table.remove(key), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullArgument() {
        table.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutFirstNullArgument() {
        table.put(null, value1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutSecondNullArgument() {
        table.put("key", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullArgument() {
        table.remove(null);
    }

    @Test
    public void testSize() {
        assertEquals(table.size(), 0);
        table.put("key1", value1);
        assertEquals(table.size(), 1);
        table.put("key2", value2);
        assertEquals(table.size(), 2);
        table.remove("key2");
        assertEquals(table.size(), 1);
        table.remove("key1");
        assertEquals(table.size(), 0);
    }

    @Test
    public void testCommit() {
        table.put("key1", value1);
        table.put("key2", value2);
        assertEquals(table.commit(), 2);
        assertEquals(table.size(), 2);
        assertEquals(table.get("key1"), value1);
        assertEquals(table.get("key2"), value2);
        table.put("key2", value3);
        table.remove("key2");
        assertEquals(table.commit(), 1);
        assertEquals(table.size(), 1);
        assertEquals(table.get("key1"), value1);
        assertEquals(table.get("key2"), null);
    }

    @Test
    public void testRollback() {
        table.put("key1", value1);
        table.put("key2", value2);
        assertEquals(table.rollback(), 2);
        table.put("key1", value1);
        table.put("key2", value2);
        assertEquals(table.commit(), 2);
        table.remove("key2");
        table.put("key1", value3);
        assertEquals(table.rollback(), 2);
        assertEquals(table.get("key1"), value1);
        assertEquals(table.get("key2"), value2);
    }

    @Test (expected = ColumnFormatException.class)
    public void testSmallColumns() {
        table.put("key", SMALL_COLUMNS_COUNT);
    }

    @Test (expected = ColumnFormatException.class)
    public void testLargeColumns() {
        table.put("key", LARGE_COLUMNS_COUNT);
    }

    @Test
    public void testEmptyIncorrectTypeStoreable() {
        table.put("key", EMPTY_INCORRECT_COLUMNS);
    }

    @Test (expected = ColumnFormatException.class)
    public void testNonEmptyIncorrectTypeStoreable() {
        table.put("key", NON_EMPTY_INCORRECT_COLUMNS);
    }
}
