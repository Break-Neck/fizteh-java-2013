package ru.fizteh.fivt.students.baranov.shell;

public class Pwd extends BasicCommand {
    public boolean doCommand(String[] args, ShellState currentPath) {
        if (args.length != 1) {
            System.err.println("pwd don't need arguments");
            return true;
        }
        System.out.println(currentPath.getCurrentPath().toString());
        return true;
    }
}
