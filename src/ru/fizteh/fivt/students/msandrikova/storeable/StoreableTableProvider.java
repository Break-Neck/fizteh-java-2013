package ru.fizteh.fivt.students.msandrikova.storeable;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.msandrikova.shell.Utils;

public class StoreableTableProvider implements ChangesCountingTableProvider {
    private File currentDirectory;
    private Map<String, ChangesCountingTable> mapOfTables = new HashMap<String, ChangesCountingTable>(); 
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    

    public StoreableTableProvider(File dir) throws IllegalArgumentException, IOException {
        this.currentDirectory = dir;
        if (!this.currentDirectory.exists()) {
            if (!dir.mkdir()) {
                throw new IOException("Table provider: Can not create working directory.");
            }
        } else if (!this.currentDirectory.isDirectory()) {
            throw new IllegalArgumentException("Given directory name does not correspond to directory.");
        } else {
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    ChangesCountingTable newTable = null;
                    List<Class<?>> columnTypes = null;
                    try {
                        columnTypes = Utils.getClassTypes(f);
                        newTable = new StoreableTable(this.currentDirectory, 
                                f.getName(), columnTypes, this);
                    } catch (IOException e) {
                        throw e;
                    }
                    this.mapOfTables.put(f.getName(), newTable);
                }
            }
        }
    }

    @Override
    public void removeTable(String name) throws IOException, IllegalStateException, IllegalArgumentException {
        if (Utils.isEmpty(name) || !Utils.testBadSymbols(name)) {
            throw new IllegalArgumentException("Table name can not be null or empty");
        }
        File tablePath = new File(this.currentDirectory, name);
        if (tablePath.exists() && !tablePath.isDirectory()) {
            throw new IllegalArgumentException("File with name '" + name + "' should be directory.");
        }
        
        lock.writeLock().lock();
        if (this.mapOfTables.get(name) == null) {
            lock.writeLock().unlock();
            throw new IllegalStateException();
        }
        
        try {
            Utils.remover(tablePath, "drop", false);
            this.mapOfTables.remove(name);
        } catch (IOException e) {
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Storeable deserialize(Table table, String value) throws ParseException {
        Storeable row = this.createFor(table);
        JSONArray valueJSON = null;
        try {
            valueJSON = new JSONArray(value);
        } catch (JSONException e) {
            throw new ParseException(e.getMessage(), 0);
        }
        if (valueJSON.length() != table.getColumnsCount()) {
            throw new ParseException("Incorrect column count, expected " 
                    + table.getColumnsCount() + ".", 0);
        }
        Object o = null;
        for (int i = 0; i < table.getColumnsCount(); ++i) {
            o = valueJSON.get(i);
            if (o.equals(JSONObject.NULL)) {
                o = null;
            }
            if (o != null && table.getColumnType(i).equals(Long.class) 
                    && o.getClass().equals(Integer.class)) {
                o = Long.parseLong(o.toString());
            }
            if (o != null && table.getColumnType(i).equals(Byte.class) 
                    && o.getClass().equals(Integer.class)) {
                o = Byte.parseByte(o.toString());
            }
            if (o != null && table.getColumnType(i).equals(Float.class) 
                    && o.getClass().equals(Double.class)) {
                o = Float.parseFloat(o.toString());
            }
            try {
                row.setColumnAt(i, o);
            } catch (ColumnFormatException e) {
                throw new ParseException(e.getMessage(), 0);
            }
        }
        return row;
    }


    @Override
    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        if (value == null) {
            return null;
        }
        JSONArray valueJSON = new JSONArray();
        Object o = null;
        for (int i = 0; i < table.getColumnsCount(); ++i) {
            o = value.getColumnAt(i);
            if (o == null) {
                valueJSON.put(JSONObject.NULL);
                continue;
            }
            if (!o.getClass().equals(table.getColumnType(i))) {
                throw new ColumnFormatException("Incorrect column type.");
            }
            if (table.getColumnType(i).equals(Byte.class)) {
                o = Integer.parseInt(o.toString());
            }
            if (table.getColumnType(i).equals(Float.class)) {
                o = Double.parseDouble(o.toString());
            }
            valueJSON.put(o);
        }
        return valueJSON.toString();
    }

    @Override
    public Storeable createFor(Table table) {
        List<Class<?>> columnTypes = new ArrayList<Class<?>>();
        for (int i = 0; i < table.getColumnsCount(); ++i) {
            columnTypes.add(table.getColumnType(i));
        }
        return new TableRow(columnTypes);
    }

    @Override
    public Storeable createFor(Table table, List<?> values) throws IndexOutOfBoundsException {
        Storeable row = this.createFor(table);
        for (int i = 0; i < table.getColumnsCount(); ++i) {
            row.setColumnAt(i, values.get(i));
        }
        return row;
    }

    @Override
    public ChangesCountingTable getTable(String name) throws IllegalArgumentException {
        if (Utils.isEmpty(name) || !Utils.testBadSymbols(name)) {
            throw new IllegalArgumentException("Table name can not be null "
                    + "or empty or contain bad symbols");
        }
        File tablePath = new File(this.currentDirectory, name);
        if (tablePath.exists() && !tablePath.isDirectory()) {
            throw new IllegalArgumentException("File with name '" + name + "' should be directory.");
        }
        lock.readLock().lock();
        ChangesCountingTable answer = this.mapOfTables.get(name);
        lock.readLock().unlock();
        return answer;
    }

    @Override
    public ChangesCountingTable createTable(String name, List<Class<?>> columnTypes) throws IOException {
        if (Utils.isEmpty(name) || !Utils.testBadSymbols(name)) {
            throw new IllegalArgumentException("Table name can not be null "
                    + "or empty or contain bad symbols");
        }
        if (columnTypes == null) {
            throw new IllegalArgumentException("Null column types");
        }
        if (!Utils.testColumnTypes(columnTypes)) {
            throw new IllegalArgumentException("Bad column types");
        }
        
        lock.writeLock().lock();
        if (this.mapOfTables.get(name) != null) {
            lock.writeLock().unlock();
            return null;
        }
        
        ChangesCountingTable newTable = null;
        try {
            newTable = new StoreableTable(this.currentDirectory, name, columnTypes, this);
            this.mapOfTables.put(name, newTable);
        } catch (IOException e) {
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
        
        return newTable;
    }

}
