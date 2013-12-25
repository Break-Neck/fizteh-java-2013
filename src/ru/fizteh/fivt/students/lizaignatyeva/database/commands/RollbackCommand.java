package ru.fizteh.fivt.students.lizaignatyeva.database.commands;

import ru.fizteh.fivt.students.lizaignatyeva.database.Database;
import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;

public class RollbackCommand extends Command {
    private Database database;

    public RollbackCommand(Database database) {
        this.database = database;
        name = "rollback";
        argumentsAmount = 0;
    }

    @Override
    public void run(String[] args) throws Exception {
        if (!database.checkActive()) {
            return;
        }
        System.out.println(database.currentTable.rollback());
    }
}
