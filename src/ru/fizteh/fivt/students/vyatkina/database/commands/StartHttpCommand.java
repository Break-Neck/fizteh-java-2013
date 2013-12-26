package ru.fizteh.fivt.students.vyatkina.database.commands;

import ru.fizteh.fivt.students.vyatkina.CommandExecutionException;
import ru.fizteh.fivt.students.vyatkina.database.DatabaseCommand;
import ru.fizteh.fivt.students.vyatkina.database.DatabaseState;
import ru.fizteh.fivt.students.vyatkina.database.servlet.TransactionManager;

public class StartHttpCommand extends DatabaseCommand {

    private TransactionManager manager;
    private static final String DEFAULT_PORT = "10001";

    public StartHttpCommand(DatabaseState state, TransactionManager manager) {
        super(state);
        this.manager = manager;
        this.name = "starthttp";
        this.argsCount = 1;
    }

    @Override
    public String[] parseArgs(String signature) throws IllegalArgumentException {
        String[] args = new String[1];
        if (signature.isEmpty()) {
            args[0] = DEFAULT_PORT;
            return args;
        }
        args = signature.split("\\s+");
        if (args.length != getArgumentCount()) {
            throw new IllegalArgumentException(WRONG_NUMBER_OF_ARGUMENTS);
        }
        return args;
    }

    @Override
    public void execute(String[] args) throws CommandExecutionException {
        if (manager.setPort(args[0])) {
            manager.startHttp();
        }
    }
}
