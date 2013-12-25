package ru.fizteh.fivt.students.lizaignatyeva.database.commands;

import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.lizaignatyeva.database.Database;
import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;

public class RemoveCommand extends Command{
    private Database database;

    public RemoveCommand(Database database) {
        name = "remove";
        argumentsAmount = 1;
        this.database = database;
    }

    @Override
    public void run(String[] args) throws Exception {
        if (!database.checkActive()) {
            return;
        }
        String key = args[0];
        Storeable oldValue = database.currentTable.remove(key);
        if (oldValue == null) {
            System.out.println("not found");
        } else {
            System.out.println("removed");
        }
    }
}
