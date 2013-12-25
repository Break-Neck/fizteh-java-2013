package ru.fizteh.fivt.students.zhidkovanton.shell;

import java.util.ArrayList;

public class Shell {
    private ArrayList<ShellCommand> commands;

    public Shell() {
        commands = new ArrayList<ShellCommand>();
    }

    public final void add(final ShellCommand command) {
        commands.add(command);
    }

    public final void execute(final Command command) {
        for (int i = 0; i < commands.size(); ++i) {
            if (commands.get(i).isAvaliableCommand(command)) {
                commands.get(i).execute();
                return;
            }
        }
        if (command.length() > 0 && !command.getArg(0).equals("")) {
            throw new InvalidCommandException("Unknown command: " + command.getArg(0));
        }
    }

}
