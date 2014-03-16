package ru.fizteh.fivt.students.kocurba.storage.strings.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.students.kocurba.storage.strings.FileTable;

/**
 * Task 05 - JUnit
 *
 * JUnit tests for {@link FileTable} class
 *
 * @author Alina Kocurba
 *
 */
public class FileTableTest {

    private Path tempFileName;
    private Table table;

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIfFileNameIsNull() {
        new FileTable(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIfFileDoesntExist() {
        new FileTable(null, "/non-existing");
    }

    @Before
    public void initTempFile() throws IOException {
        this.tempFileName = Files.createTempFile(null, null);
        this.table = new FileTable(null, this.tempFileName.toString());
    }

    @After
    public void removeTempFile() throws IOException {
        Files.delete(this.tempFileName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithNullKey() {
        this.table.put(null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithNullValue() {
        this.table.put("", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithNullKey() {
        this.table.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveWithNullKey() {
        this.table.remove(null);
    }

    @Test
    public void testIfPutReturnsPreviousValue() {
        this.table.put("a", "b");
        Assert.assertEquals(this.table.put("a", "c"), "b");
    }

}
