package ru.fizteh.fivt.students.vyatkina.database.commands;

import ru.fizteh.fivt.students.vyatkina.CommandExecutionException;
import ru.fizteh.fivt.students.vyatkina.database.DatabaseCommand;
import ru.fizteh.fivt.students.vyatkina.database.DatabaseState;
import ru.fizteh.fivt.students.vyatkina.database.servlet.TransactionManager;

public class StopHttpCommand extends DatabaseCommand {
    private TransactionManager manager;
    public StopHttpCommand(DatabaseState state, TransactionManager manager) {
        super(state);
        this.name = "stophttp";
        this.argsCount = 0;
        this.manager = manager;
    }

    @Override
    public void execute(String[] args) throws CommandExecutionException {
          manager.stopHttp();
    }

}
