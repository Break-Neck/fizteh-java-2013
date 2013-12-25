package ru.fizteh.fivt.students.lizaignatyeva.database.commands;

import ru.fizteh.fivt.students.lizaignatyeva.database.Database;
import ru.fizteh.fivt.students.lizaignatyeva.database.MyTable;
import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;

public class UseCommand extends Command {
    private Database database;

    public UseCommand(Database database) {
        this.database = database;
        name = "use";
        argumentsAmount = 1;
    }

    @Override
    public void run(String[] args) throws Exception {
        if (database.currentTable != null) {
            int uncommitedEntries = database.currentTable.keysToCommit();
            if (uncommitedEntries != 0) {
                System.out.println(uncommitedEntries + " unsaved changes");
                return;
            }
        }

        String tableName = args[0];

        MyTable newTable = database.tableProvider.getTable(tableName);
        if (newTable == null) {
            System.out.println(tableName + " not exists");
            return;
        }

        database.currentTable = newTable;
        System.out.println("using " + tableName);


    }
}
