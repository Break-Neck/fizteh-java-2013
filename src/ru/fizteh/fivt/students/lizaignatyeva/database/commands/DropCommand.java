package ru.fizteh.fivt.students.lizaignatyeva.database.commands;

import ru.fizteh.fivt.students.lizaignatyeva.database.Database;
import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;

public class DropCommand extends Command {
    private Database database;

    public DropCommand(Database database) {
        this.database = database;
        name = "drop";
        argumentsAmount = 1;
    }

    @Override
    public void run(String[] args) throws Exception {
        String tableName = args[0];
        try {
            database.tableProvider.removeTable(tableName);
            if (database.currentTable != null && database.currentTable.getName().equals(tableName)) {
                database.currentTable = null;
            }
            System.out.println("dropped");
        } catch (IllegalStateException e) {
            System.out.println(tableName + " not exists");
        }
    }
}
