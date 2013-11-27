package ru.fizteh.fivt.students.vlmazlov.storeable;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.vlmazlov.generics.GenericTableProvider;
import ru.fizteh.fivt.students.vlmazlov.utils.*;

import javax.activation.UnsupportedDataTypeException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class StoreableTableProvider extends GenericTableProvider<Storeable, StoreableTable>
        implements TableProvider, AutoCloseable {

    private boolean isClosed;

    public StoreableTableProvider(String name, boolean autoCommit) throws ValidityCheckFailedException {
        super(name, autoCommit);
        isClosed = false;
    }

    @Override
    protected StoreableTable instantiateTable(String name, Object[] args) {
        checkClosed();
        return new StoreableTable(this, name, autoCommit, (List) args[0]);
    }

    public StoreableTable getTable(String name) {
        checkClosed();

        return super.getTable(name);
    }

    public synchronized void removeTable(String name) {
        checkClosed();
        super.removeTable(name);
    }

    public String getRoot() {
        checkClosed();
        return super.getRoot();
    }

    public synchronized StoreableTable createTable(String name, List<Class<?>> columnTypes) throws IOException {
        checkClosed();

        if ((columnTypes == null) || (columnTypes.isEmpty())) {
            throw new IllegalArgumentException("wrong type (column types not specified)");
        }

        try {
            for (Class<?> type : columnTypes) {
                ValidityChecker.checkColumnType(type);
            }
        } catch (ValidityCheckFailedException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }

        StoreableTable table = super.createTable(name, new Object[]{columnTypes});

        File tableDir = new File(getRoot(), name);
        File signatureFile = new File(tableDir, "signature.tsv");

        signatureFile.createNewFile();

        PrintWriter writer = new PrintWriter(signatureFile);

        try {
            for (Class<?> clazz : columnTypes) {
                writer.print(TypeName.getNameByClass(clazz) + " ");
            }
        } finally {
            QuietCloser.closeQuietly(writer);
        }

        return table;
    }

    @Override
    public Storeable deserialize(StoreableTable table, String value) throws ParseException {
        checkClosed();
        return this.deserialize((Table) table, value);
    }

    @Override
    public String serialize(StoreableTable table, Storeable value) throws ColumnFormatException {
        checkClosed();
        return this.serialize((Table) table, value);
    }

    @Override
    public Storeable deserialize(Table table, String value) throws ParseException {
        checkClosed();

        List<Object> values = new ArrayList<Object>();

        try {
            XMLStoreableReader reader = new XMLStoreableReader(value);
            for (int i = 0; i < table.getColumnsCount(); ++i) {
                values.add(reader.readColumn(table.getColumnType(i)));
            }


        } catch (XMLStreamException ex) {
            throw new ParseException(ex.getMessage(), 0);
        }

        return createFor(table, values);
    }

    @Override
    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        checkClosed();

        try {
            ValidityChecker.checkValueFormat(table, value);
        } catch (ValidityCheckFailedException ex) {
            throw new ColumnFormatException(ex.getMessage());
        }

        try {
            return XMLStoreableWriter.serialize(value);
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Unable to write XML: " + ex.getMessage());
        }
    }

    @Override
    public Storeable createFor(Table table) {
        checkClosed();

        if (table == null) {
            throw new IllegalArgumentException("table not specified");
        }

        List<Class<?>> valueTypes = new ArrayList<Class<?>>(table.getColumnsCount());

        for (int i = 0; i < table.getColumnsCount(); ++i) {
            valueTypes.add(table.getColumnType(i));
        }

        return new TableRow(valueTypes);
    }

    @Override
    public Storeable createFor(Table table, List<?> values) throws ColumnFormatException, IndexOutOfBoundsException {
        checkClosed();

        if (table == null) {
            throw new IllegalArgumentException("table not specified");
        }

        if (values == null) {
            throw new IllegalArgumentException("values not specified");
        }

        if (values.size() > table.getColumnsCount()) {
            throw new IndexOutOfBoundsException("Too many columns passed");
        }

        if (values.size() < table.getColumnsCount()) {
            throw new IndexOutOfBoundsException("Too few columns passed");
        }

        Storeable result = createFor(table);

        for (int i = 0; i < values.size(); ++i) {
            result.setColumnAt(i, values.get(i));
        }

        return result;
    }

    private List<Class<?>> getTableSignature(File tableDir)
            throws ValidityCheckFailedException, IOException {
        checkClosed();

        ValidityChecker.checkMultiStoreableTableRoot(tableDir);

        File signatureFile = new File(tableDir, "signature.tsv");
        List<Class<?>> signature = new ArrayList<Class<?>>();
        Scanner scanner = new Scanner(signatureFile);

        try {
            while (scanner.hasNext()) {
                String type = scanner.next();
                Class<?> columnType = TypeName.getClassByName(type.trim());

                if (columnType == null) {
                    throw new UnsupportedDataTypeException("Unsupported column type: " + type);
                }

                signature.add(columnType);
            }

            ValidityChecker.checkStoreableTableSignature(signature);

            return signature;
        } finally {
            scanner.close();
        }
    }

    @Override
    public void read() throws IOException, ValidityCheckFailedException {
        checkClosed();

        for (File file : ProviderReader.getTableDirList(this)) {
            StoreableTable table = createTable(file.getName(), getTableSignature(file));
            ProviderReader.readMultiTable(file, table, this);
            //read data has to be preserved
            table.commit();
        }
    }

    @Override
    public void write() throws IOException, ValidityCheckFailedException {
        checkClosed();
        ProviderWriter.writeProvider(this);
    }

    public void closeTable(String name) {
        checkClosed();
        tables.remove(name);
    }

    public void close() {
        //necessary for factory.close() to work
        if (isClosed) {
            return;
        }

        for (Map.Entry<String, StoreableTable> entry : tables.entrySet()) {
            entry.getValue().close();
        }

        isClosed = true;

    }

    public void checkClosed() {
        if (isClosed) {
            throw new IllegalStateException("trying to operate on a closed table provider");
        }
    }

    public String toString() {
        checkClosed();
        StringBuilder builder = new StringBuilder();

        builder.append(getClass().getSimpleName());
        builder.append("[");
        builder.append(getRoot());
        builder.append("]");

        return builder.toString();
    }
}
