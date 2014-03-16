package ru.fizteh.fivt.students.kocurba.shell.command;

import ru.fizteh.fivt.students.kocurba.shell.StateWrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MvCommand implements Command<File> {
    @Override
    public int getArgCount() {
        return 2;
    }

    @Override
    public String getCommandName() {
        return "mv";
    }

    @Override
    public void executeCommand(StateWrap<File> state, String[] arguments) throws IOException {
        File source = new File(arguments[1]);
        if (!source.isAbsolute()) {
            source = new File(state.getState(), arguments[1]);
        }

        File destination = new File(arguments[2]);
        if (!destination.isAbsolute()) {
            destination = new File(state.getState(), arguments[2]);
        }

        if (!source.exists()) {
            throw new IOException("cp: source does not exist");
        }

        if (source.equals(destination)) {
            throw new IOException("cp: source and destination are equal");
        }

        if (source.isFile()) {
            if (!destination.exists()) {
                Files.copy(source.toPath(), destination.toPath());
            } else {
                if (destination.isDirectory()) {
                    File newDir = new File(destination, source.getName());
                    if (newDir.exists()) {
                        throw new IOException("cp: " + arguments[1] + " already exists in " + arguments[2]);
                    } else {
                        Files.move(source.toPath(), newDir.toPath());
                    }
                } else {
                    throw new IOException("cp: " + arguments[2] + " is not directory");
                }
            }
        }

        if (source.isDirectory()) {
            if (destination.isDirectory()) {
                if (!destination.exists()) {
                    destination.mkdir();
                }
                movingRec(source, destination);
            } else {
                throw new IOException("cp: " + arguments[2] + " is not directory");
            }
        }


    }

    private void movingRec(File source, File destination) throws IOException {
        File newDir = new File(destination, source.getName());
        if (newDir.exists()) {
            throw new IOException("cp: " + source.getName() + " already exists in" + destination.getName());
        } else {
            if (source.isDirectory()) {
                newDir.mkdir();
                for (File file : source.listFiles()) {
                    movingRec(file, newDir);
                }
            } else {
                Files.move(source.toPath(), newDir.toPath());
            }
        }
    }
}
