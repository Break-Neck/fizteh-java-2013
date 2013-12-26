package ru.fizteh.fivt.students.lizaignatyeva.database.commands;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.lizaignatyeva.database.BadTypeException;
import ru.fizteh.fivt.students.lizaignatyeva.database.Database;
import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;

public class GetCommand extends Command {
    private Database database;

    public GetCommand(Database database) {
        this.database = database;
        name = "get";
        argumentsAmount = 1;
    }

    @Override
    public void run(String[] args) throws Exception {
        String key = args[0];
        if (!database.checkActive()) {
            return;
        }
        try {
            Storeable value = database.currentTable.get(key);
            if (value == null) {
                System.out.println("not found");
            } else {
                System.out.println("found");
                System.out.println(database.serialize(value));
            }
        } catch (BadTypeException e) {
            System.out.println(String.format("wrong type (%s)", e.getMessage()));
        } catch (ColumnFormatException e) {
            System.out.println(String.format("wrong type (%s)", e.getMessage()));
        } catch (Exception e) {
            System.err.println("get: " + e.getMessage());
        }

    }
}
