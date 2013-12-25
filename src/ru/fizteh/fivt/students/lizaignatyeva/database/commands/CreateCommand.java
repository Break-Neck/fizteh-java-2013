package ru.fizteh.fivt.students.lizaignatyeva.database.commands;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.students.lizaignatyeva.database.BadTypeException;
import ru.fizteh.fivt.students.lizaignatyeva.database.Database;
import ru.fizteh.fivt.students.lizaignatyeva.database.MyTable;
import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;

public class CreateCommand extends Command {
    private Database database;

    public CreateCommand(Database database) {
        this.database = database;
        name = "create";
        argumentsAmount = 2;
    }

    @Override
    protected boolean checkArguments(String[] args) throws Exception {
        return args.length >= 2;
    }

    @Override
    public void run(String[] args) throws Exception {
        String tableName = args[0];
        StringBuilder paramsBuilder = new StringBuilder();
        for (int index = 1; index < args.length; index ++) {
            if (index > 1) {
                paramsBuilder.append(" ");
            }
            paramsBuilder.append(args[index]);
        }
        String params = paramsBuilder.toString().trim();
        if (!params.startsWith("(") || !params.endsWith(")")) {
            System.out.println(String.format("wrong type (%s is incorrect type description)", params));
            return;
        }
        params = paramsBuilder.substring(1, params.length() - 1);
        String[] classNames = params.split(" ");
        MyTable table;
        try {
            table = database.tableProvider.createTable(tableName, MyTable.convert(classNames));
        } catch (BadTypeException e) {
            System.out.println(String.format("wrong type (%s)", e.getMessage()));
            return;
        } catch (ColumnFormatException e) {
            System.out.println(String.format("wrong type (%s)", e.getMessage()));
            return;
        } catch (IllegalArgumentException e) {
            System.out.println(String.format("wrong type (%s)", e.getMessage()));
            return;
        }
        if (table == null) {
            System.out.println(tableName + " exists");
        } else {
            System.out.println("created");
        }
    }
}
