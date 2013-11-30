package ru.fizteh.fivt.students.yaninaAnastasia.filemap;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ru.fizteh.fivt.proxy.LoggingProxyFactory;
import ru.fizteh.fivt.storage.structured.*;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.*;

public class TestsLoggingProxyFactory {
    static JUnitTestInterface implementation;
    static List<Class<?>> columnTypes;
    static List<Class<?>> columnMultiTypes;
    Table table;
    Table multiColumnTable;
    static TableProviderFactory factory;
    TableProvider provider;
    private static final String SINGLE_COLUMN_TABLE_NAME = "testTable";
    private static final String MULTI_COLUMN_TABLE_NAME = "MultiColumnTable";
    static StringWriter stringWriter;
    static LoggingProxyFactory testLoggingFactory;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws IOException {
        columnTypes = new ArrayList<Class<?>>() {
            {
                add(Integer.class);
            }
        };
        columnMultiTypes = new ArrayList<Class<?>>() {
            {
                add(Integer.class);
                add(String.class);
                add(Double.class);
            }
        };
        factory = new DatabaseTableProviderFactory();
        testLoggingFactory = new DatabaseLoggingProxyFactory();

    }

    @Before
    public void beforeTest() throws IOException {
        stringWriter = new StringWriter();
        provider = factory.create(folder.getRoot().getPath());
        table = provider.createTable(SINGLE_COLUMN_TABLE_NAME, columnTypes);
        multiColumnTable = provider.createTable(MULTI_COLUMN_TABLE_NAME, columnMultiTypes);
    }

    @After
    public void afterTest() throws IOException {
        provider.removeTable(SINGLE_COLUMN_TABLE_NAME);
        provider.removeTable(MULTI_COLUMN_TABLE_NAME);
    }

    public Storeable makeStoreable(int value) {
        try {
            return provider.deserialize(table, String.format("<row><col>%d</col></row>", value));
        } catch (ParseException e) {
            return null;
        }
    }

    @Test
    public void interfaceExecLogTest() {
        implementation = new JUnitTestInterface() {
            @Override
            public void exec() {
            }

            @Override
            public void supportFunc() throws Exception {
            }

            @Override
            public int getAmount() {
                return 0;
            }
        };
        JUnitTestInterface proxy = (JUnitTestInterface)
                testLoggingFactory.wrap(stringWriter, implementation, JUnitTestInterface.class);

        long timestampBefore = System.currentTimeMillis();
        proxy.exec();
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), implementation.getClass().getName());
        Assert.assertEquals(parser.getString("method"), "exec");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertTrue(args.isNull(0));
        stringWriter.flush();
    }

    @Test
    public void interfaceSupportFuncLogTest() throws  Exception{
        implementation = new JUnitTestInterface() {
            @Override
            public void exec() {
            }

            @Override
            public void supportFunc() throws Exception {
            }

            @Override
            public int getAmount() {
                return 0;
            }
        };
        JUnitTestInterface proxy = (JUnitTestInterface)
                testLoggingFactory.wrap(stringWriter, implementation, JUnitTestInterface.class);

        long timestampBefore = System.currentTimeMillis();
        proxy.supportFunc();
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), implementation.getClass().getName());
        Assert.assertEquals(parser.getString("method"), "supportFunc");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertTrue(args.isNull(0));
        stringWriter.flush();
    }

    @Test
    public void interfaceGetAmountLogTest() {
        implementation = new JUnitTestInterface() {
            @Override
            public void exec() {
            }

            @Override
            public void supportFunc() throws Exception {
            }

            @Override
            public int getAmount() {
                table.put("key1", makeStoreable(1));
                table.put("key2", makeStoreable(1));
                table.put("key3", makeStoreable(1));
                return table.size();
            }
        };
        JUnitTestInterface proxy = (JUnitTestInterface)
                testLoggingFactory.wrap(stringWriter, implementation, JUnitTestInterface.class);

        long timestampBefore = System.currentTimeMillis();
        proxy.getAmount();
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), implementation.getClass().getName());
        Assert.assertEquals(parser.getString("method"), "getAmount");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertTrue(args.isNull(0));
        Assert.assertEquals(parser.getInt("returnValue"), 3);
        table.remove("key1");
        table.remove("key2");
        table.remove("key3");
        stringWriter.flush();
    }

    @Test
    public void interfaceGetAmountOneMoreLogTest() {
        implementation = new JUnitTestInterface() {
            @Override
            public void exec() {
            }

            @Override
            public void supportFunc() throws Exception {
            }

            @Override
            public int getAmount() {
                table.put("key1", makeStoreable(1));
                table.put("key2", makeStoreable(1));
                table.put("key3", makeStoreable(1));
                table.remove("key1");
                table.remove("key2");
                table.remove("key3");
                return table.rollback();
            }
        };
        JUnitTestInterface proxy = (JUnitTestInterface)
                testLoggingFactory.wrap(stringWriter, implementation, JUnitTestInterface.class);

        long timestampBefore = System.currentTimeMillis();
        proxy.getAmount();
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), implementation.getClass().getName());
        Assert.assertEquals(parser.getString("method"), "getAmount");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertTrue(args.isNull(0));
        Assert.assertEquals(parser.getInt("returnValue"), 0);
        stringWriter.flush();
    }

    @Test
    public void sizeLogTest() {
        Table wrappedTable = (Table) testLoggingFactory.wrap(stringWriter, table, Table.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedTable.size();
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTable");
        Assert.assertEquals(parser.getString("method"), "size");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertTrue(args.isNull(0));
        Assert.assertEquals(parser.getInt("returnValue"), 0);
        stringWriter.flush();
    }

    @Test
    public void getLogTest() {
        table.put("key", makeStoreable(5));
        Table wrappedTable = (Table) testLoggingFactory.wrap(stringWriter, table, Table.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedTable.get("key");
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTable");
        Assert.assertEquals(parser.getString("method"), "get");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertEquals(args.getString(0), "key");
        Assert.assertTrue(!parser.isNull("returnValue"));
        Assert.assertEquals(parser.getString("returnValue"), "DatabaseStoreable[5]");
        table.remove("key");
        stringWriter.flush();
    }

    @Test
    public void putLogTest() {
        Table wrappedTable = (Table) testLoggingFactory.wrap(stringWriter, table, Table.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedTable.put("key", makeStoreable(5));
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTable");
        Assert.assertEquals(parser.getString("method"), "put");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertEquals(args.getString(0), "key");
        Assert.assertEquals(args.getString(1), "DatabaseStoreable[5]");
        Assert.assertTrue(parser.isNull("returnValue"));
        stringWriter.flush();
    }

    @Test
    public void removeLogTest() {
        table.put("key", makeStoreable(5));
        Table wrappedTable = (Table) testLoggingFactory.wrap(stringWriter, table, Table.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedTable.remove("key");
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTable");
        Assert.assertEquals(parser.getString("method"), "remove");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertEquals(args.getString(0), "key");
        Assert.assertTrue(!parser.isNull("returnValue"));
        Assert.assertEquals(parser.get("returnValue"), "DatabaseStoreable[5]");
        stringWriter.flush();
    }

    @Test
    public void commitLogTest() {
        table.put("key1", makeStoreable(1));
        table.put("key2", makeStoreable(2));
        table.put("key3", makeStoreable(3));
        Table wrappedTable = (Table) testLoggingFactory.wrap(stringWriter, table, Table.class);
        long timestampBefore = System.currentTimeMillis();
        try {
            wrappedTable.commit();
        } catch (IOException e) {
            // nothing
        }
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTable");
        Assert.assertEquals(parser.getString("method"), "commit");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertTrue(args.isNull(0));
        Assert.assertTrue(!parser.isNull("returnValue"));
        Assert.assertEquals(parser.getInt("returnValue"), 3);
        table.remove("key1");
        table.remove("key2");
        table.remove("key3");
        stringWriter.flush();
    }

    @Test
    public void rollbackLogTest() {
        table.put("key1", makeStoreable(1));
        table.put("key2", makeStoreable(2));
        table.put("key3", makeStoreable(3));
        Table wrappedTable = (Table) testLoggingFactory.wrap(stringWriter, table, Table.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedTable.rollback();
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTable");
        Assert.assertEquals(parser.getString("method"), "rollback");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertTrue(args.isNull(0));
        Assert.assertTrue(!parser.isNull("returnValue"));
        Assert.assertEquals(parser.getInt("returnValue"), 3);
        table.remove("key1");
        table.remove("key2");
        table.remove("key3");
        stringWriter.flush();
    }

    @Test
    public void getNameLogTest() {
        Table wrappedTable = (Table) testLoggingFactory.wrap(stringWriter, table, Table.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedTable.getName();
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTable");
        Assert.assertEquals(parser.getString("method"), "getName");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertTrue(args.isNull(0));
        Assert.assertEquals(parser.getString("returnValue"), "testTable");
        stringWriter.flush();
    }

    @Test
    public void getColumnsCountLogTest() {
        Table wrappedTable = (Table) testLoggingFactory.wrap(stringWriter, table, Table.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedTable.getColumnsCount();
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTable");
        Assert.assertEquals(parser.getString("method"), "getColumnsCount");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertTrue(args.isNull(0));
        Assert.assertEquals(parser.getInt("returnValue"), 1);
        stringWriter.flush();
    }

    @Test
    public void getColumnTypeLogTest() {
        Table wrappedTable = (Table) testLoggingFactory.wrap(stringWriter, table, Table.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedTable.getColumnType(0);
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTable");
        Assert.assertEquals(parser.getString("method"), "getColumnType");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertEquals(args.getInt(0), 0);
        Assert.assertTrue(!parser.isNull("returnValue"));
        Assert.assertEquals(parser.get("returnValue").toString(), Integer.class.toString());
        stringWriter.flush();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getColumnTypeExceptionLogTest() {
        Table wrappedTable = (Table) testLoggingFactory.wrap(stringWriter, table, Table.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedTable.getColumnType(1);
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTable");
        Assert.assertEquals(parser.getString("method"), "getColumnType");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertEquals(args.getInt(0), 0);
        Assert.assertTrue(!parser.isNull("returnValue"));
        Assert.assertEquals(parser.get("returnValue").toString(), Integer.class.toString());
        stringWriter.flush();
    }

    @Test
    public void createTableLogTest() {
        TableProvider wrappedProvider = (TableProvider) testLoggingFactory.wrap(stringWriter,
                provider, TableProvider.class);
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Integer.class);
        long timestampBefore = System.currentTimeMillis();
        try {
            wrappedProvider.createTable("new", list);
        } catch (IOException e) {
            //
        }
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals("ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTableProvider",
                parser.getString("class"));
        Assert.assertEquals("createTable", parser.getString("method"));
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertEquals("new", args.getString(0));
        Assert.assertEquals("[\"class java.lang.Integer\"]", args.getJSONArray(1).toString());
        Assert.assertTrue(!parser.isNull("returnValue"));
        String fold = folder.getRoot().getPath();
        Assert.assertEquals(String.format("DatabaseTable[%s\\new]", fold), parser.get("returnValue").toString());
        try {
            provider.removeTable("new");
        } catch (IOException e) {
            //
        }
        stringWriter.flush();
    }

    @Test
    public void getTableLogTest() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Integer.class);
        try {
            provider.createTable("new", list);
        } catch (IOException e) {
            //
        }
        TableProvider wrappedProvider = (TableProvider) testLoggingFactory.wrap(stringWriter,
                provider, TableProvider.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedProvider.getTable("new");
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals("ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTableProvider",
                parser.getString("class"));
        Assert.assertEquals("getTable", parser.getString("method"));
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertEquals("new", args.getString(0));
        Assert.assertTrue(!parser.isNull("returnValue"));
        String fold = folder.getRoot().getPath();
        Assert.assertEquals(String.format("DatabaseTable[%s\\new]", fold), parser.get("returnValue").toString());
        try {
            provider.removeTable("new");
        } catch (IOException e) {
            //
        }
        stringWriter.flush();
    }

    @Test
    public void removeTableLogTest() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Integer.class);
        try {
            provider.createTable("new", list);
        } catch (IOException e) {
            //
        }
        TableProvider wrappedProvider = (TableProvider) testLoggingFactory.wrap(stringWriter,
                provider, TableProvider.class);
        long timestampBefore = System.currentTimeMillis();
        try {
            wrappedProvider.removeTable("new");
        } catch (IOException e) {
            //
        }
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals("ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTableProvider",
                parser.getString("class"));
        Assert.assertEquals("removeTable", parser.getString("method"));
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertEquals("new", args.getString(0));
        Assert.assertTrue(parser.isNull("returnValue"));
        stringWriter.flush();
    }

    @Test
    public void serializeTableLogTest() {
        TableProvider wrappedProvider = (TableProvider) testLoggingFactory.wrap(stringWriter,
                provider, TableProvider.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedProvider.serialize(table, makeStoreable(5));
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals("ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTableProvider",
                parser.getString("class"));
        Assert.assertEquals("serialize", parser.getString("method"));
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        String fold = folder.getRoot().getPath();
        Assert.assertEquals(String.format("DatabaseTable[%s\\testTable]", fold), args.getString(0));
        Assert.assertEquals(makeStoreable(5).toString(), args.getString(1));
        Assert.assertTrue(!parser.isNull("returnValue"));
        Assert.assertEquals("<row><col>5</col></row>", parser.getString("returnValue"));
        stringWriter.flush();
    }

    @Test
    public void deserializeTableLogTest() {
        TableProvider wrappedProvider = (TableProvider) testLoggingFactory.wrap(stringWriter,
                provider, TableProvider.class);
        long timestampBefore = System.currentTimeMillis();
        try {
            wrappedProvider.deserialize(table, "<row><col>5</col></row>");
        } catch (ParseException e) {
            //
        }
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals("ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTableProvider",
                parser.getString("class"));
        Assert.assertEquals("deserialize", parser.getString("method"));
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        String fold = folder.getRoot().getPath();
        Assert.assertEquals(String.format("DatabaseTable[%s\\testTable]", fold), args.getString(0));
        Assert.assertEquals("<row><col>5</col></row>", args.getString(1));
        Assert.assertTrue(!parser.isNull("returnValue"));
        Assert.assertEquals("DatabaseStoreable[5]", parser.getString("returnValue"));
        stringWriter.flush();
    }

    @Test
    public void createForLogTest() {
        TableProvider wrappedProvider = (TableProvider) testLoggingFactory.wrap(stringWriter,
                provider, TableProvider.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedProvider.createFor(table);
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals("ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTableProvider",
                parser.getString("class"));
        Assert.assertEquals("createFor", parser.getString("method"));
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        String fold = folder.getRoot().getPath();
        Assert.assertEquals(String.format("DatabaseTable[%s\\testTable]", fold), args.getString(0));
        List<Class<?>> list = new ArrayList<Class<?>>();
        Assert.assertTrue(!parser.isNull("returnValue"));
        Assert.assertEquals("DatabaseStoreable" + list.toString(), parser.get("returnValue"));
        stringWriter.flush();
    }

    @Test
    public void createForMultiLogTest() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Integer.class);
        TableProvider wrappedProvider = (TableProvider) testLoggingFactory.wrap(stringWriter,
                provider, TableProvider.class);
        long timestampBefore = System.currentTimeMillis();
        wrappedProvider.createFor(table, list);
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals("ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTableProvider",
                parser.getString("class"));
        Assert.assertEquals("createFor", parser.getString("method"));
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        String fold = folder.getRoot().getPath();
        Assert.assertEquals(String.format("DatabaseTable[%s\\testTable]", fold), args.getString(0));
        Assert.assertEquals(list.get(0).toString(), args.getJSONArray(1).get(0));
        Assert.assertTrue(!parser.isNull("returnValue"));
        Assert.assertEquals("DatabaseStoreable" + list.toString(), parser.get("returnValue"));
        stringWriter.flush();
    }

    @Test
    public void getExceptionLogTest() {
        TableProvider wrappedProveder = (TableProvider) testLoggingFactory.wrap(stringWriter,
                provider, TableProvider.class);
        long timestampBefore = System.currentTimeMillis();
        try {
            wrappedProveder.getTable(null);
        } catch (IllegalArgumentException e) {
            //
        }
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertEquals(parser.get("thrown"), "java.lang.IllegalArgumentException: table's name cannot be null");
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"),
                "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTableProvider");
        Assert.assertEquals(parser.getString("method"), "getTable");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertTrue(args.isNull(0));
        Assert.assertEquals(parser.get("thrown"), "java.lang.IllegalArgumentException: table's name cannot be null");
        stringWriter.flush();
    }

    @Test
    public void getColumnTypeExcpLogTest() {
        Table wrappedTable = (Table) testLoggingFactory.wrap(stringWriter, table, Table.class);
        long timestampBefore = System.currentTimeMillis();
        try {
        wrappedTable.getColumnType(1);
        } catch (IndexOutOfBoundsException e) {
            //
        }
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertEquals(parser.get("thrown"), "java.lang.IndexOutOfBoundsException: wrong index");
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals(parser.getString("class"), "ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTable");
        Assert.assertEquals(parser.getString("method"), "getColumnType");
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertEquals(args.getInt(0), 1);
        Assert.assertTrue(parser.isNull("returnValue"));
        stringWriter.flush();
    }

    @Test
    public void removeTableExceptionLogTest() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Integer.class);
        try {
            provider.createTable("new", list);
        } catch (IOException e) {
            //
        }
        TableProvider wrappedProvider = (TableProvider) testLoggingFactory.wrap(stringWriter,
                provider, TableProvider.class);
        long timestampBefore = System.currentTimeMillis();
        try {
            wrappedProvider.removeTable("newTable");
        } catch (IOException e) {
            //
        }  catch (IllegalStateException e) {
            //
        }
        long timestampAfter = System.currentTimeMillis();
        String result = stringWriter.toString();
        JSONObject parser = new JSONObject(result);
        Assert.assertEquals(parser.get("thrown"), "java.lang.IllegalStateException: newTable not exists");
        Assert.assertTrue(parser.getLong("timestamp") >= timestampBefore
                && parser.getLong("timestamp") <= timestampAfter);
        Assert.assertEquals("ru.fizteh.fivt.students.yaninaAnastasia.filemap.DatabaseTableProvider",
                parser.getString("class"));
        Assert.assertEquals("removeTable", parser.getString("method"));
        JSONArray args = parser.getJSONArray("arguments");
        Assert.assertTrue(args instanceof JSONArray);
        Assert.assertEquals("newTable", args.getString(0));
        Assert.assertTrue(parser.isNull("returnValue"));
        stringWriter.flush();
    }

}
