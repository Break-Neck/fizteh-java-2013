package ru.fizteh.fivt.students.zhidkovanton.storable;

import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.zhidkovanton.shell.BaseShellCommand;
import ru.fizteh.fivt.students.zhidkovanton.shell.Command;
import ru.fizteh.fivt.students.zhidkovanton.shell.InvalidCommandException;

import java.text.ParseException;

public final class ShellPut extends BaseShellCommand {
    private DataBaseFactory dataBaseFactory;

    public ShellPut(DataBaseFactory dataBaseFactory) {
        setName("put");
        setNumberOfArgs(3);
        setHint("usage: put <key> <value>");
        this.dataBaseFactory = dataBaseFactory;
    }

    @Override
    public void execute() {
        if (dataBaseFactory.dataBase == null) {
            System.out.println("no table");
            return;
        }
        try {
            Storeable oldValue = ((DataBase) dataBaseFactory.dataBase).putStoreable(getArg(1), getSpacedArg(2));
            if (oldValue == null) {
                System.out.println("new");
            } else {
                System.out.println("overwrite");
                System.out.println(dataBaseFactory.dataFactory.serialize(dataBaseFactory.dataBase, oldValue));
            }
        } catch (ParseException e) {
            System.out.println("wrong type (" + e.getMessage() + ")");
        }
    }

    @Override
    public boolean isAvaliableCommand(final Command command) {
        if (name.equals(command.getArg(0))) {
            if (command.length() < numberOfArgs) {
                throw new InvalidCommandException(name + " " + hint);
            }
            args = command;
            return true;
        }
        return false;
    }
}
