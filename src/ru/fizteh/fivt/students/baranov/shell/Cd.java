package ru.fizteh.fivt.students.baranov.shell;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class Cd extends BasicCommand {
    public boolean doCommand(String args[], ShellState currentPath) {
        if (args.length != 2) {
            System.err.println("cd needs 1 argument");
            return true;
        }

        Path tempPath = Paths.get(args[1]).normalize();
        Path newPath = currentPath.getCurrentPath().resolve(tempPath);
        newPath = newPath.toAbsolutePath();

        if (Files.isDirectory(newPath)) {
            currentPath.changeCurrentPath(newPath);
            return true;
        } else {
            System.err.println(newPath.toString() + " - it isn't path");
            return true;
        }
    }
}
