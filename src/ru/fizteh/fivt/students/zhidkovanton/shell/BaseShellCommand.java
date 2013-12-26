package ru.fizteh.fivt.students.zhidkovanton.shell;

public abstract class BaseShellCommand implements ShellCommand {
    protected String name;
    protected int numberOfArgs;
    protected String hint;
    protected Command args;

    @Override
    public void execute() {

    }

    @Override
    public boolean isAvaliableCommand(final Command command) {
        if (name.equals(command.getArg(0))) {
            if (command.length() > numberOfArgs) {
                throw new IllegalArgumentException(name + ": too many arguments");
            }
            if (command.length() < numberOfArgs) {
                throw new IllegalArgumentException(name + " " + hint);
            }
            args = command;
            return true;
        }
        return false;
    }

    public final String getHint() {
        return hint;
    }

    public final void setHint(String newHint) {
        hint = newHint;
    }

    public final String getName() {
        return name;
    }

    public void setName(final String newName) {
        name = newName;
    }

    public void setNumberOfArgs(final int newNumberOfArgs) {
        numberOfArgs = newNumberOfArgs;
    }

    public final int getNumberOfArgs() {
        return numberOfArgs;
    }

    public String getArg(final int index) {
        return args.getArg(index);
    }

    public String getSpacedArg(final int index) {
        return args.getSpacedArg(index);
    }
}
