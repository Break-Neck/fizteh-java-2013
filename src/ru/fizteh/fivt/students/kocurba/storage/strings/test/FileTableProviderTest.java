package ru.fizteh.fivt.students.kocurba.storage.strings.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.students.kocurba.storage.strings.FileTableProvider;

/**
 * Task 05 - JUnit
 * 
 * JUnit tests for {@link FileTableProvider} class
 * 
 * @author Alina Kocurba
 * 
 */
public class FileTableProviderTest {

    private Path tempDir;
    private TableProvider provider;

    @Test(expected = IllegalArgumentException.class)
    public void testNullDirectoryName() {
        new FileTableProvider(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistingDirectoryName() {
        new FileTableProvider("/non-existing");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectSymbolsInDirectoryName() {
        new FileTableProvider("/\\");
    }

    @Before
    public void initTempDirectory() throws IOException {
        this.tempDir = Files.createTempDirectory(null);
        provider = new FileTableProvider(this.tempDir.toString());
    }

    private void removeDirectory(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                removeDirectory(f);
            }
        }
        file.delete();
    }

    @After
    public void removeTempDirectory() {
        removeDirectory(this.tempDir.toFile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTableIfGeneratedFileNameIsDirectory()
            throws IOException {
        for (int i = 0; i < 16; ++i) {
            Files.createDirectory(Paths.get(this.tempDir.toString() + "/" + i
                    + ".dir"));
            for (int j = 0; j < 16; ++j) {
                Files.createDirectory(Paths.get(this.tempDir.toString() + "/"
                        + i + ".dir/" + j + ".dat"));
            }
        }
        this.provider.createTable("a");
    }

    @Test
    public void testCreateTableIfGeneratedFileNameExists() throws IOException {
        for (int i = 0; i < 16; ++i) {
            Files.createDirectory(Paths.get(this.tempDir.toString() + "/" + i
                    + ".dir"));
            for (int j = 0; j < 16; ++j) {
                Files.createFile(Paths.get(this.tempDir.toString() + "/" + i
                        + ".dir/" + j + ".dat"));
            }
        }
        Assert.assertNull(this.provider.createTable("a"));
    }


    @Test
    public void testGetTableIfFileDoesntExist() {
        Assert.assertNull(this.provider.getTable("a"));
    }


}
