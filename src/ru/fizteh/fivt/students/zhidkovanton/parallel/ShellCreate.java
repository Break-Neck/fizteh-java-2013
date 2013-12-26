package ru.fizteh.fivt.students.zhidkovanton.parallel;

import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;
import ru.fizteh.fivt.students.zhidkovanton.shell.Command;
import ru.fizteh.fivt.students.zhidkovanton.shell.InvalidCommandException;

import java.io.IOException;

public final class ShellCreate extends BaseShellCommand {
    private DataBaseFactory dataBaseFactory;

    public ShellCreate(final DataBaseFactory dataBaseFactory) {
        setName("create");
        setNumberOfArgs(2);
        setHint("usage: create <table name>");
        this.dataBaseFactory = dataBaseFactory;
    }

    @Override
    public void execute() {
        try {
            DataBase oldValue = (DataBase) dataBaseFactory.dataFactory.createTable(getArg(1),
                    MySignature.getTypes(getSpacedArg(2)));
            if (oldValue != null) {
                System.out.println("created");
            } else {
                System.out.println(getArg(1) + " exists");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean isAvaliableCommand(final Command command) {
        if (name.equals(command.getArg(0))) {
            if (command.length() < numberOfArgs) {
                throw new InvalidCommandException("wrong type (" + name + " " + hint + ")");
            }
            args = command;
            return true;
        }
        return false;
    }
}
