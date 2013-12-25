package ru.fizteh.fivt.students.lizaignatyeva.database.commands;

import ru.fizteh.fivt.students.lizaignatyeva.database.Database;
import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;

public class ExitCommand extends Command {
    private Database database;

    public ExitCommand(Database database) {
        this.database = database;
        name = "exit";
        argumentsAmount = 0;
    }

    @Override
    public void run(String[] args) throws Exception {
        try {
            if (database.currentTable != null) {
                database.currentTable.write();
            }
        } catch (Exception e) {
            // do nothing
        }
        System.exit(0);
    }
}
