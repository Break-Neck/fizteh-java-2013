package ru.fizteh.fivt.students.vyatkina;

public abstract class AbstractCommand<State> implements Command {

    protected String name;
    protected int argsCount;
    public State state;

    public static final String WRONG_NUMBER_OF_ARGUMENTS = "Wrong number of arguments";

    public AbstractCommand(State state) {
        this.state = state;
    }

    @Override
    public abstract void execute(String[] args) throws CommandExecutionException;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getArgumentCount() {
        return argsCount;
    }

    @Override
    public String[] parseArgs(String signature) throws IllegalArgumentException {
        if (signature.isEmpty() && getArgumentCount() == 0) {
            return new String[0];
        }
        String[] args = signature.split("\\s+");
        if (args.length != getArgumentCount()) {
            throw new IllegalArgumentException(WRONG_NUMBER_OF_ARGUMENTS);
        }
        return args;
    }
}
