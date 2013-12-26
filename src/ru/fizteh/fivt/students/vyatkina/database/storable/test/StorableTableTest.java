package ru.fizteh.fivt.students.vyatkina.database.storable.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import ru.fizteh.fivt.students.vyatkina.database.StorableTable;
import ru.fizteh.fivt.students.vyatkina.database.storable.StorableRow;
import ru.fizteh.fivt.students.vyatkina.database.storable.StorableRowShape;
import ru.fizteh.fivt.students.vyatkina.database.storable.StorableTableImp2;
import ru.fizteh.fivt.students.vyatkina.database.storable.StorableTableProviderFactory;
import ru.fizteh.fivt.students.vyatkina.database.storable.StorableTableProviderImp;
import ru.fizteh.fivt.students.vyatkina.database.superior.TableChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StorableTableTest {

    private StorableTable table;
    private static StorableTableProviderImp tableProvider;
    private static StorableTableProviderFactory factory;
    private final String tableName = "LittleTestTable";
    private static final List<Class<?>> CLASS_LIST;
    private static final StorableRow SAMPLE_VALUE1;
    private static final StorableRow SAMPLE_VALUE2;
    private static final String SAMPLE_KEY1 = "ohMyGod";
    private static final String SAMPLE_KEY2 = "dearLeasy";
    private static final String SAMPLE_KEY3 = "hahahahas";
    private static final String NOT_EXISTING_KEY = "VeryLonelyKey";

    static {
        CLASS_LIST = new ArrayList<>();
        CLASS_LIST.add(String.class);
        CLASS_LIST.add(Integer.class);
        StorableRowShape shape = new StorableRowShape(CLASS_LIST);
        SAMPLE_VALUE1 = new StorableRow(shape);
        ArrayList<Object> valueList = new ArrayList<>();
        valueList.add(new String("Bla"));
        valueList.add(new Integer(2));
        SAMPLE_VALUE2 = new StorableRow(shape, valueList);
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() throws IOException {
        factory = new StorableTableProviderFactory();
        tableProvider = StorableTableProviderImp.class.cast(factory.create(folder.getRoot().toString()));
        table = StorableTableImp2.class.cast(tableProvider.createTable(tableName, CLASS_LIST));
    }

    @After
    public void deleteTable() throws IOException {
        table = null;
        tableProvider.removeTable(tableName);
    }

    @Test
    public void putNullKeyShouldFail() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(TableChecker.KEY_SHOULD_NOT_BE_NULL);
        table.put(null, SAMPLE_VALUE1);
    }

    @Test
    public void putEmptyKeyShouldFail() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(TableChecker.KEY_SHOULD_NOT_BE_EMPTY);
        table.put("   ", SAMPLE_VALUE1);

    }


    @Test
    public void putNullValueShouldFail() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(TableChecker.VALUE_SHOULD_NOT_BE_NULL);
        table.put(SAMPLE_KEY1, null);

    }

    @Test
    public void getEmptyKeyShouldFail() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(TableChecker.KEY_SHOULD_NOT_BE_EMPTY);
        table.get("   ");

    }

    @Test
    public void getNullKeyShouldFail() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(TableChecker.KEY_SHOULD_NOT_BE_NULL);
        table.get(null);

    }

    @Test
    public void putAndGetValueShouldNotFail() {
        table.put(SAMPLE_KEY1, SAMPLE_VALUE1);
        Assert.assertEquals("Get value should not fail", SAMPLE_VALUE1, table.get(SAMPLE_KEY1));
    }

    @Test
    public void emptyTableShouldHaveNullSize() {
        Assert.assertEquals("Empty table should have null size", 0, table.size());
    }

    @Test
    public void putValueShouldIncreaseSize() {
        table.put(SAMPLE_KEY1, SAMPLE_VALUE1);
        Assert.assertEquals("Put value should increase size", 1, table.size());
    }


    @Test
    public void overrideValueShouldNotFail() {
        table.put(SAMPLE_KEY1, SAMPLE_VALUE1);
        Assert.assertEquals("Override value should return null", null, table.put(SAMPLE_KEY2, SAMPLE_VALUE1));
    }

    @Test
    public void overrideValueShouldNotIncreaseSize() {
        table.put(SAMPLE_KEY1, SAMPLE_VALUE1);
        table.put(SAMPLE_KEY1, SAMPLE_VALUE1);
        Assert.assertEquals("Put value should increase size", 1, table.size());
    }

    @Test
    public void removeValueShouldDecreaseSize() {
        table.put(SAMPLE_KEY1, SAMPLE_VALUE1);
        table.remove(SAMPLE_KEY1);
        Assert.assertEquals("Remove value should decrease size", 0, table.size());
    }

    @Test
    public void removeKeyThatDoesNotExistShouldReturnNull() {
        Assert.assertEquals("Remove table that does not exist should return null", null, table.remove(
                NOT_EXISTING_KEY));
    }

    @Test
    public void getShouldChangeWithinRollback() {
        table.put(SAMPLE_KEY3, SAMPLE_VALUE1);
        Assert.assertEquals("Putten value should be reachable", SAMPLE_VALUE1, table.get(SAMPLE_KEY3));
        table.rollback();
        Assert.assertEquals("Value should not be found", null, table.get(SAMPLE_KEY3));
    }

    @Test
    public void rollbackPutShouldBeOne() {
        table.put(SAMPLE_KEY1, SAMPLE_VALUE1);
        Assert.assertEquals("Rollback one put should be one", 1, table.rollback());
    }

    @Test
    public void rollbackPutRemoveShouldBeNull() {
        table.put(SAMPLE_KEY1, SAMPLE_VALUE1);
        table.remove(SAMPLE_KEY1);
        Assert.assertEquals("Rollback put,remove should be 0", 0, table.rollback());
    }
}
