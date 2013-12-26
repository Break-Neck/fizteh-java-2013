package ru.fizteh.fivt.students.dmitryKonturov.dataBase.Servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServletMain {
    public static void main(String[] args) {
        String dbDir = System.getProperty("fizteh.db.dir");
        //String dbDir = "/home/kontr/testDir/myTest";
        if (dbDir == null) {
            System.err.println("Empty property");
            System.exit(1);
        }

        ServletShell shell = null;
        try {
            Path dbDirPath = Paths.get(dbDir);
            if (Files.notExists(dbDirPath)) {
                Path parentPath = dbDirPath.getParent();
                if (Files.isDirectory(parentPath)) {
                    Files.createDirectory(dbDirPath);
                }
            }
            shell = new ServletShell(dbDirPath);
        } catch (IOException |IllegalArgumentException e) {
            System.err.println("Unable to launch shell:  " + e.toString());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Wrong property: " + e.toString());
            System.exit(1);
        }

        if (args.length > 0) {
            System.err.println("Package mode ignored, interactive launched");
            shell.interactiveMode();
        } else {
            shell.interactiveMode();
        }
    }
}
