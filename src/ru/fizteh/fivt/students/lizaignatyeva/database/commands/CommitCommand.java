package ru.fizteh.fivt.students.lizaignatyeva.database.commands;


import ru.fizteh.fivt.students.lizaignatyeva.database.Database;
import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;

public class CommitCommand extends Command {
    private Database database;

    public CommitCommand(Database database) {
        this.database = database;
        name = "commit";
        argumentsAmount = 0;
    }

    @Override
    public void run(String[] args) throws Exception {
        if (database.checkActive()) {
            System.out.println(database.currentTable.commit());
        }
    }
}
