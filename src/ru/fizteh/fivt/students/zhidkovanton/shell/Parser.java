package ru.fizteh.fivt.students.zhidkovanton.shell;

public class Parser {
    private StringBuilder commands;

    public Parser(final String[] args) {
        commands = new StringBuilder();

        for (int i = 0; i < args.length; ++i) {
            commands.append(" ").append(args[i]);
        }

        if (commands.charAt(commands.length() - 1) != ';') {
            commands.append(';');
        }
    }

    public Parser(final String arg) {
        commands = new StringBuilder(arg);
        if ((!arg.isEmpty()) && (commands.charAt(commands.length() - 1) != ';')) {
            commands.append(';');
        }
    }

    public Command getCommand() {
        String command = "";
        for (int i = 0; i < commands.length(); ++i) {
            if (commands.charAt(i) == ';') {
                command = commands.substring(0, i);
                commands.replace(0, i + 1, "");
                break;
            }
        }
        return new Command(command);
    }


    public boolean isEmpty() {
        return (commands.length() == 0);
    }
}
