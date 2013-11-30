package ru.fizteh.fivt.students.musin.filemap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.junit.rules.TemporaryFolder;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class FileMapUnitTest {

    private ArrayList<Class<?>> getColumnTypeList() {
        ArrayList<Class<?>> columnTypes = new ArrayList<>();
        columnTypes.add(Integer.class);
        columnTypes.add(String.class);
        columnTypes.add(Byte.class);
        return columnTypes;
    }

    private Storeable getSampleStoreable() {
        Storeable sample = new FixedList(getColumnTypeList());
        sample.setColumnAt(0, 1);
        sample.setColumnAt(1, "hello");
        sample.setColumnAt(2, Byte.valueOf((byte) 2));
        return sample;
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void providerFromNullShouldFail() throws IOException {
        try {
            FileMapProviderFactory factory = new FileMapProviderFactory();
            factory.create(null);
            factory.close();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Null location");
        }
    }

    @Test(expected = RuntimeException.class)
    public void notAFolderCheckTest() throws IOException {
        File file = new File(folder.getRoot(), "test");
        file.createNewFile();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        factory.create(file.getCanonicalPath());
        factory.close();
    }

    @Test(expected = RuntimeException.class)
    public void filesInsideDirectoryShouldFail() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        File newFile = new File(testFolder, "file");
        newFile.createNewFile();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        factory.create(testFolder.getCanonicalPath());
        factory.close();
    }

    @Test
    public void newlyCreatedTableIsEmptyWithNoUncommittedChanges() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Assert.assertTrue(table.size() == 0);
        Assert.assertTrue(table.uncommittedChanges() == 0);
        factory.close();
    }

    @Test
    public void createTableCreatesFolder() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        File tableFolder = new File(testFolder, "new");
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Assert.assertTrue(tableFolder.exists() && tableFolder.isDirectory());
        factory.close();
    }

    @Test
    public void tableNullStringsCheckTest() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Storeable sample = getSampleStoreable();
        try {
            table.put(null, sample);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Null pointer instead of string");
        }
        try {
            table.put("test", null);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Null value");
        }
        try {
            table.get(null);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Null pointer instead of string");
        }
        try {
            table.remove(null);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Null pointer instead of string");
        }
        factory.close();
    }

    @Test
    public void getTableReturnsSameInstanceEveryTime() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Assert.assertTrue(table == provider.getTable("new"));
        Assert.assertTrue(provider.getTable("new") == provider.getTable("new"));
        factory.close();
    }

    @Test
    public void createTableReturnsNullIfTableExists() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        table = provider.createTable("new", getColumnTypeList());
        Assert.assertTrue(table == null);
        factory.close();
    }

    @Test
    public void getTableReturnsNullIfTableDoesntExist() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.getTable("new");
        Assert.assertTrue(table == null);
        factory.close();
    }

    @Test
    public void providerNullArgumentCheckTest() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        try {
            provider.createTable(null, getColumnTypeList());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Null name");
        }
        try {
            provider.createTable("new", null);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Null columnTypes");
        }
        try {
            provider.getTable(null);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Null name");
        }
        try {
            provider.removeTable(null);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Null name");
        }
        factory.close();
    }

    @Test
    public void removeTableRemovesDirectory() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        provider.createTable("new", getColumnTypeList());
        provider.removeTable("new");
        File tableFolder = new File(testFolder, "new");
        Assert.assertFalse(tableFolder.exists());
        factory.close();
    }

    @Test
    public void getTableForRemovedTableReturnsNull() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        provider.createTable("new", getColumnTypeList());
        provider.removeTable("new");
        Assert.assertTrue(provider.getTable("new") == null);
        factory.close();
    }

    @Test(expected = IllegalStateException.class)
    public void removingNonExistingTableShouldFail() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        provider.removeTable("new");
        factory.close();
    }

    @Test
    public void getNameReturnCorrectTableName() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Assert.assertEquals(table.getName(), "new");
        factory.close();
    }

    @Test
    public void ifPutValueGetReturnsSameValue() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        table.put("test1", getSampleStoreable());
        Assert.assertEquals(table.get("test1"), getSampleStoreable());
        factory.close();
    }

    @Test
    public void putReturnsPreviouslyStoredValue() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        table.put("test1", getSampleStoreable());
        Storeable sample2 = getSampleStoreable();
        sample2.setColumnAt(2, null);
        Assert.assertEquals(table.put("test1", sample2), getSampleStoreable());
        factory.close();
    }

    @Test
    public void getAndRemoveReturnNullIfKeyDoesntExist() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Assert.assertTrue(table.get("test1") == null);
        Assert.assertTrue(table.remove("test1") == null);
        factory.close();
    }

    @Test
    public void numberOfCommittedChangesShouldBe3then1() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        Storeable sample1 = getSampleStoreable();
        Storeable sample2 = getSampleStoreable();
        sample2.setColumnAt(1, null);
        Storeable sample3 = getSampleStoreable();
        sample3.setColumnAt(0, null);
        Storeable sample4 = getSampleStoreable();
        sample4.setColumnAt(2, null);
        Storeable sample5 = getSampleStoreable();
        sample5.setColumnAt(0, 10000);
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        table.put("test1", sample1);
        table.put("test1", sample1);
        table.put("test2", sample2);
        table.put("test3", sample3);
        table.put("test3", sample4);
        Assert.assertTrue(table.commit() == 3);
        table.put("test1", sample5);
        table.remove("test1");
        Assert.assertTrue(table.commit() == 1);
        factory.close();
    }

    @Test
    public void rollbackSetsTableBackToLastCommit() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Storeable sample1 = getSampleStoreable();
        Storeable sample2 = getSampleStoreable();
        sample2.setColumnAt(1, null);
        table.put("a", sample1);
        table.rollback();
        Assert.assertTrue(table.get("a") == null);
        table.put("a", sample1);
        table.commit();
        table.put("a", sample2);
        table.rollback();
        Assert.assertEquals(table.get("a"), sample1);
        factory.close();
    }

    @Test
    public void after3NewKeysAddedSizeShouldBe3() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Storeable sample1 = getSampleStoreable();
        Storeable sample2 = getSampleStoreable();
        sample2.setColumnAt(1, null);
        Storeable sample3 = getSampleStoreable();
        sample3.setColumnAt(0, null);
        Storeable sample4 = getSampleStoreable();
        sample4.setColumnAt(2, null);
        table.put("a", sample1);
        table.put("a", sample2);
        table.put("b", sample1);
        table.put("c", sample3);
        table.remove("b");
        table.put("d", sample4);
        Assert.assertTrue(table.size() == 3);
        factory.close();
    }

    @Test
    public void removeDeletesKeyFromTable() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Storeable sample1 = getSampleStoreable();
        table.put("test1", sample1);
        table.remove("test1");
        Assert.assertTrue(table.get("test1") == null);
        factory.close();
    }

    @Test
    public void twoTypesOfStoreable() throws  IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Storeable sample1 = getSampleStoreable();
        Storeable sample = new FixedListTwo(getColumnTypeList());
        sample.setColumnAt(0, 1);
        sample.setColumnAt(1, "hello");
        sample.setColumnAt(2, Byte.valueOf((byte) 3));
        table.put("a", sample1);
        table.put("b", sample);
        Assert.assertTrue(table.commit() == 2);
        table.put("a", sample);
        table.put("b", sample1);
        Assert.assertTrue(table.commit() == 2);
        table.remove("b");
        Assert.assertEquals(table.get("a"), sample);
        table.remove("a");
        Assert.assertTrue(table.commit() == 2);
        table.put("a", sample);
        table.commit();
        table.put("a", sample);
        Assert.assertTrue(table.commit() == 0);
        factory.close();
    }

    @Test
    public void storeablePuttingWrongTypesShouldFail() {
        Storeable sample = new FixedList(getColumnTypeList());
        sample.setColumnAt(0, new Integer(3));
        sample.setColumnAt(1, "test");
        sample.setColumnAt(2, new Byte((byte) 4));
        sample.setColumnAt(2, null);
        boolean ok = false;
        try {
            sample.setColumnAt(0, new Float(1.2));
        } catch (ColumnFormatException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail("Exception expected none found");
        }
        ok = false;
        try {
            sample.setColumnAt(1, new HashMap<String, String>());
        } catch (ColumnFormatException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail("Exception expected none found");
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void tooBigIndexesInStoreableShouldFail() {
        Storeable sample = getSampleStoreable();
        sample.getColumnAt(10);
    }

    @Test(expected = ColumnFormatException.class)
    public void putWrongColumnsStoreableShouldFail() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        ArrayList<Class<?>> columnTypes = getColumnTypeList();
        columnTypes.set(0, Float.class);
        Storeable sample = new FixedList(columnTypes);
        sample.setColumnAt(0, new Float(1.1));
        table.put("a", sample);
    }

    @Test
    public void serializeDeserializeReturnsSameObject() throws IOException, ParseException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Storeable sample = getSampleStoreable();
        Assert.assertEquals(provider.deserialize(table, provider.serialize(table, sample)), sample);
        factory.close();
    }

    @Test(expected = ParseException.class)
    public void deserializeInvalidJSONShouldFail() throws IOException, ParseException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Storeable sample = provider.deserialize(table, "abracadabra");
        factory.close();
    }

    @Test(expected = ParseException.class)
    public void deserializeWrongTypesShouldFail() throws IOException, ParseException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Storeable sample = provider.deserialize(table, "[1.1, 1, 3]");
        factory.close();
    }

    @Test
    public void serializeReturnsJSON() throws IOException, ParseException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Storeable sample = getSampleStoreable();
        Assert.assertEquals(provider.serialize(table, sample), "[1,\"hello\",2]");
        factory.close();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void createForArrayMisMatchShouldFail() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        provider.createFor(table, new ArrayList<Object>(4));
        factory.close();
    }

    @Test(expected = ColumnFormatException.class)
    public void tooManyColumnsPutShouldFail() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        ArrayList<Class<?>> columnTypes = getColumnTypeList();
        columnTypes.add(Integer.class);
        table.put("a", new FixedList(columnTypes));
        factory.close();
    }

    @Test
    public void getClassAtReturnsSpecifiedClass() {
        ArrayList<Class<?>> columnTypes = new ArrayList<>();
        columnTypes.add(Integer.class);
        columnTypes.add(Byte.class);
        columnTypes.add(Long.class);
        columnTypes.add(Boolean.class);
        columnTypes.add(Float.class);
        columnTypes.add(Double.class);
        columnTypes.add(String.class);
        Storeable sample = new FixedList(columnTypes);
        sample.setColumnAt(0, new Integer(3));
        sample.setColumnAt(1, new Byte((byte) 4));
        sample.setColumnAt(2, new Long((long) 123));
        sample.setColumnAt(3, new Boolean(true));
        sample.setColumnAt(4, new Float((float) 1.2));
        sample.setColumnAt(5, new Double(3.4));
        sample.setColumnAt(6, "sample");
        Assert.assertTrue(sample.getIntAt(0).getClass() == Integer.class);
        Assert.assertTrue(sample.getByteAt(1).getClass() == Byte.class);
        Assert.assertTrue(sample.getLongAt(2).getClass() == Long.class);
        Assert.assertTrue(sample.getBooleanAt(3).getClass() == Boolean.class);
        Assert.assertTrue(sample.getFloatAt(4).getClass() == Float.class);
        Assert.assertTrue(sample.getDoubleAt(5).getClass() == Double.class);
        Assert.assertTrue(sample.getStringAt(6).getClass() == String.class);
    }

    @Test
    public void getClassAtFromWrongColumnShouldFail() {
        ArrayList<Class<?>> columnTypes = new ArrayList<>();
        columnTypes.add(Integer.class);
        columnTypes.add(Byte.class);
        columnTypes.add(Long.class);
        columnTypes.add(Boolean.class);
        columnTypes.add(Float.class);
        columnTypes.add(Double.class);
        columnTypes.add(String.class);
        Storeable sample = new FixedList(columnTypes);
        boolean ok = false;
        try {
            sample.getIntAt(1);
        } catch (ColumnFormatException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getByteAt(2);
        } catch (ColumnFormatException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getLongAt(3);
        } catch (ColumnFormatException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getBooleanAt(4);
        } catch (ColumnFormatException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getFloatAt(5);
        } catch (ColumnFormatException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getDoubleAt(6);
        } catch (ColumnFormatException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getStringAt(0);
        } catch (ColumnFormatException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
    }

    @Test
    public void getClassAtFromBigIndexShouldFail() {
        ArrayList<Class<?>> columnTypes = new ArrayList<>();
        columnTypes.add(Integer.class);
        columnTypes.add(Byte.class);
        columnTypes.add(Long.class);
        columnTypes.add(Boolean.class);
        columnTypes.add(Float.class);
        columnTypes.add(Double.class);
        columnTypes.add(String.class);
        Storeable sample = new FixedList(columnTypes);
        boolean ok = false;
        try {
            sample.getIntAt(10);
        } catch (IndexOutOfBoundsException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getByteAt(10);
        } catch (IndexOutOfBoundsException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getLongAt(10);
        } catch (IndexOutOfBoundsException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getBooleanAt(10);
        } catch (IndexOutOfBoundsException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getFloatAt(10);
        } catch (IndexOutOfBoundsException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getDoubleAt(10);
        } catch (IndexOutOfBoundsException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
        ok = false;
        try {
            sample.getStringAt(10);
        } catch (IndexOutOfBoundsException e) {
            ok = true;
        }
        if (!ok) {
            Assert.fail();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getColumnTypeFromBigIndexShouldFail() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        table.getColumnType(10);
        factory.close();
    }

    @Test
    public void getColumnTypeFromIntegerColumnReturnsInteger() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Assert.assertTrue(table.getColumnType(0) == Integer.class);
        factory.close();
    }

    @Test
    public void getColumnCountReturnsColumnCount() throws IOException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        MultiFileMap table = provider.createTable("new", getColumnTypeList());
        Assert.assertEquals(table.getColumnsCount(), 3);
        factory.close();
    }

    @Test
    public void parallelCountsSizeSeparately() throws IOException, InterruptedException, ExecutionException {
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        final FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        final MultiFileMap table = provider.createTable("new", getColumnTypeList());

        ExecutorService thread1 = Executors.newSingleThreadExecutor();
        ExecutorService thread2 = Executors.newSingleThreadExecutor();

        thread1.submit(new Runnable() {
            @Override
            public void run() {
                table.put("a", getSampleStoreable());
            }
        }).get();

        Storeable sample = getSampleStoreable();
        sample.setColumnAt(0, 3);

        thread2.submit(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(table.size() == 0);
                table.put("b", getSampleStoreable());
            }
        }).get();

        thread1.submit(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(table.size() == 1);
            }
        }).get();

        thread2.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    table.commit();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).get();

        thread1.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Assert.assertTrue(table.size() == 2);
                    table.commit();
                    Assert.assertTrue(table.size() == 2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).get();

        thread1.shutdown();
        if (!thread1.awaitTermination(1, TimeUnit.SECONDS)) {
            Assert.fail("Thread haven't terminated");
        }
        thread2.shutdown();
        if (!thread2.awaitTermination(1, TimeUnit.SECONDS)) {
            Assert.fail("Thread haven't terminated");
        }
        factory.close();
    }

    @Test
    public void parallelPutAndGet() throws IOException, InterruptedException, ExecutionException {
        final Object lock = new Object();
        File testFolder = new File(folder.getRoot(), "test");
        testFolder.mkdir();
        FileMapProviderFactory factory = new FileMapProviderFactory();
        final FileMapProvider provider = factory.create(testFolder.getCanonicalPath());
        final MultiFileMap table = provider.createTable("new", getColumnTypeList());

        ExecutorService thread1 = Executors.newSingleThreadExecutor();
        ExecutorService thread2 = Executors.newSingleThreadExecutor();

        thread1.submit(new Runnable() {
            @Override
            public void run() {
                table.put("a", getSampleStoreable());
            }
        }).get();

        final Storeable sample = getSampleStoreable();
        sample.setColumnAt(0, 3);

        thread2.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Assert.assertTrue(table.size() == 0);
                    table.put("b", getSampleStoreable());
                    table.commit();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).get();

        thread1.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Assert.assertTrue(table.storeableEqual(table.get("b"), getSampleStoreable()));
                    table.commit();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).get();

        thread2.submit(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(table.storeableEqual(getSampleStoreable(), table.get("a")));
                table.put("a", sample);
            }
        }).get();

        thread1.submit(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(table.storeableEqual(table.get("a"), getSampleStoreable()));
            }
        }).get();

        thread2.submit(new Runnable() {
            @Override
            public void run() {
                Assert.assertFalse(table.storeableEqual(getSampleStoreable(), table.get("a")));
            }
        }).get();

        thread1.shutdown();
        if (!thread1.awaitTermination(1, TimeUnit.SECONDS)) {
            Assert.fail("Thread haven't terminated");
        }
        thread2.shutdown();
        if (!thread2.awaitTermination(1, TimeUnit.SECONDS)) {
            Assert.fail("Thread haven't terminated");
        }
        factory.close();
    }

    @Test
    public void proxyVoidMethodDoesntPrintReturn() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                object,
                InterfaceToProxy.class
        );
        proxy.voidResultMethod(null);
        String result = writer.toString();
        Assert.assertFalse(result.contains("return"));
        Assert.assertTrue(result.contains("<null/>"));
    }

    @Test
    public void cyclicListPassing() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                object,
                InterfaceToProxy.class
        );
        ArrayList a = new ArrayList();
        ArrayList b = new ArrayList();
        b.add(a);
        a.add(new Integer(3));
        a.add(b);
        a.add(new Integer(3));
        proxy.valueReturn(a);
        String result = writer.toString();
        Assert.assertTrue(result.contains("<value>cyclic</value>"));
    }

    @Test(expected = IOException.class)
    public void thrownExceptionShouldBeOfOriginalType() throws XMLStreamException, IOException {
        StringWriter writer = new StringWriter();
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                object,
                InterfaceToProxy.class
        );
        proxy.exceptionThrower(new Double(3.1));
    }

    @Test
    public void exceptionLogging() throws XMLStreamException, IOException {
        StringWriter writer = new StringWriter();
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                object,
                InterfaceToProxy.class
        );
        boolean exception = false;
        try {
            proxy.exceptionThrower(new Double(3.1));
        } catch (IOException e) {
            Assert.assertTrue(writer.toString().contains("<thrown>java.io.IOException: hello</thrown>"));
            Assert.assertFalse(writer.toString().contains("<return>"));
            exception = true;
        }
        if (!exception) {
            Assert.fail();
        }
    }

    @Test
    public void primitiveTypesLogging() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                object,
                InterfaceToProxy.class
        );
        proxy.argumentReceiver(3, 2.5, true);
        Assert.assertTrue(writer.toString().contains("<argument>3</argument><argument>2.5</argument>"
                + "<argument>true</argument>"));
        proxy.argumentReceiver("hello", 1000000000000L, (float) 123.4);
        Assert.assertTrue(writer.toString().contains("<argument>hello</argument><argument>1000000000000</argument>"
                + "<argument>123.4</argument>"));
    }

    @Test
    public void newlineSeparatesInvocations() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                object,
                InterfaceToProxy.class
        );
        proxy.voidResultMethod("hello");
        proxy.voidResultMethod("hi");
        Assert.assertTrue(writer.toString().contains("</invoke>" + System.lineSeparator() + "<invoke"));
    }

    @Test
    public void loggingIterable() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                object,
                InterfaceToProxy.class
        );
        ArrayList<Integer> a = new ArrayList<>();
        a.add(1);
        a.add(2);
        a.add(3);
        proxy.voidResultMethod(a);
        Assert.assertTrue(writer.toString().contains("<argument><list><value>1</value><value>2</value>"
                + "<value>3</value></list></argument>"));
    }

    @Test
    public void nestedCyclicLists() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                object,
                InterfaceToProxy.class
        );
        ArrayList a = new ArrayList();
        ArrayList b = new ArrayList();
        a.add("hello");
        a.add(b);
        b.add("goodbye");
        b.add(a);
        proxy.voidResultMethod(a);
        Assert.assertTrue(writer.toString().contains("<list><value>hello</value><value><list><value>goodbye</value>"
                + "<value>cyclic</value></list></value></list>"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void proxyFactoryNullWriterShouldFail() throws XMLStreamException {
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                null,
                object,
                InterfaceToProxy.class
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void proxyFactoryNullImplementationShouldFail() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                null,
                InterfaceToProxy.class
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void proxyFactoryNullInterfaceShouldFail() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                object,
                null
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void proxyFactoryNotAnInterfaceShouldFail() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                new ArrayList(),
                ArrayList.class
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void proxyFactoryImplementationOfWrongInterfaceShouldFail() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        ImplementToProxy object = new ImplementToProxy();
        InterfaceToProxy proxy = (InterfaceToProxy) (new XMLLoggingProxyFactory()).wrap(
                writer,
                new ArrayList(),
                InterfaceToProxy.class
        );
    }
}

interface InterfaceToProxy {

    void voidResultMethod(Object argument);
    void argumentReceiver(Object arg1, Object arg2, Object arg3);
    Object valueReturn(Object arg1);
    Object exceptionThrower(Object arg1) throws IOException;
}

class ImplementToProxy implements InterfaceToProxy{
    public void voidResultMethod(Object argument) {}
    public void argumentReceiver(Object arg1, Object arg2, Object arg3) {}
    public Object valueReturn(Object arg1) {
        return arg1;
    }
    public Object exceptionThrower(Object arg1) throws IOException {
        throw new IOException("hello");
    }
}


class FixedListTwo implements Storeable {
    public static final Class<?>[] CLASSES = new Class<?>[] {
            Integer.class,
            Long.class,
            Byte.class,
            Float.class,
            Double.class,
            Boolean.class,
            String.class
    };

    final ArrayList<Class<?>> columnTypes;
    Object[] objects;

    public FixedListTwo(List<Class<?>> columnTypes) {
        this.columnTypes = new ArrayList<Class<?>>(columnTypes);
        objects = new Object[columnTypes.size()];
    }

    public void setColumnAt(int columnIndex, Object value) throws ColumnFormatException, IndexOutOfBoundsException {
        if (columnIndex >= objects.length || columnIndex < 0) {
            throw new IndexOutOfBoundsException(String.format("Index out of bounds: array size %d, found %d",
                    objects.length, columnIndex));
        }
        if (value != null && value.getClass() != columnTypes.get(columnIndex)) {
            throw new ColumnFormatException(String.format("Wrong format: %s expected, %s found",
                    columnTypes.get(columnIndex).toString(), value.getClass().toString()));
        }
        objects[columnIndex] = value;
    }

    public ArrayList<Class<?>> getColumnTypes() {
        return new ArrayList<>(columnTypes);
    }

    public int size() {
        return objects.length;
    }

    public Object getColumnAt(int columnIndex) throws IndexOutOfBoundsException {
        if (columnIndex >= objects.length || columnIndex < 0) {
            throw new IndexOutOfBoundsException(String.format("Index out of bounds: array size %d, found %d",
                    objects.length, columnIndex));
        }
        return objects[columnIndex];
    }

    public Integer getIntAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        if (columnIndex >= objects.length || columnIndex < 0) {
            throw new IndexOutOfBoundsException(String.format("Index out of bounds: array size %d, found %d",
                    objects.length, columnIndex));
        }
        if (Integer.class != columnTypes.get(columnIndex)) {
            throw new ColumnFormatException(String.format("Wrong format: %s expected, %s found",
                    columnTypes.get(columnIndex).toString(), Integer.class.toString()));
        }
        return (Integer) objects[columnIndex];
    }

    public Long getLongAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        if (columnIndex >= objects.length || columnIndex < 0) {
            throw new IndexOutOfBoundsException(String.format("Index out of bounds: array size %d, found %d",
                    objects.length, columnIndex));
        }
        if (Long.class != columnTypes.get(columnIndex)) {
            throw new ColumnFormatException(String.format("Wrong format: %s expected, %s found",
                    columnTypes.get(columnIndex).toString(), Long.class.toString()));
        }
        return (Long) objects[columnIndex];
    }

    public Byte getByteAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        if (columnIndex >= objects.length || columnIndex < 0) {
            throw new IndexOutOfBoundsException(String.format("Index out of bounds: array size %d, found %d",
                    objects.length, columnIndex));
        }
        if (Byte.class != columnTypes.get(columnIndex)) {
            throw new ColumnFormatException(String.format("Wrong format: %s expected, %s found",
                    columnTypes.get(columnIndex).toString(), Byte.class.toString()));
        }
        return (Byte) objects[columnIndex];
    }

    public Float getFloatAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        if (columnIndex >= objects.length || columnIndex < 0) {
            throw new IndexOutOfBoundsException(String.format("Index out of bounds: array size %d, found %d",
                    objects.length, columnIndex));
        }
        if (Float.class != columnTypes.get(columnIndex)) {
            throw new ColumnFormatException(String.format("Wrong format: %s expected, %s found",
                    columnTypes.get(columnIndex).toString(), Float.class.toString()));
        }
        return (Float) objects[columnIndex];
    }

    public Double getDoubleAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        if (columnIndex >= objects.length || columnIndex < 0) {
            throw new IndexOutOfBoundsException(String.format("Index out of bounds: array size %d, found %d",
                    objects.length, columnIndex));
        }
        if (Double.class != columnTypes.get(columnIndex)) {
            throw new ColumnFormatException(String.format("Wrong format: %s expected, %s found",
                    columnTypes.get(columnIndex).toString(), Double.class.toString()));
        }
        return (Double) objects[columnIndex];
    }

    public Boolean getBooleanAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        if (columnIndex >= objects.length || columnIndex < 0) {
            throw new IndexOutOfBoundsException(String.format("Index out of bounds: array size %d, found %d",
                    objects.length, columnIndex));
        }
        if (Boolean.class != columnTypes.get(columnIndex)) {
            throw new ColumnFormatException(String.format("Wrong format: %s expected, %s found",
                    columnTypes.get(columnIndex).toString(), Boolean.class.toString()));
        }
        return (Boolean) objects[columnIndex];
    }

    public String getStringAt(int columnIndex) throws ColumnFormatException, IndexOutOfBoundsException {
        if (columnIndex >= objects.length || columnIndex < 0) {
            throw new IndexOutOfBoundsException(String.format("Index out of bounds: array size %d, found %d",
                    objects.length, columnIndex));
        }
        if (String.class != columnTypes.get(columnIndex)) {
            throw new ColumnFormatException(String.format("Wrong format: %s expected, %s found",
                    columnTypes.get(columnIndex).toString(), String.class.toString()));
        }
        return (String) objects[columnIndex];
    }
}

