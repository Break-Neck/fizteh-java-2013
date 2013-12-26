package ru.fizteh.fivt.students.zinnatullin.shell;

public class PwdCommand implements ShellCommand {
    public boolean execute(String[] args) {
        assert (args.length != 0);
        if (args.length > 1) {
            System.out.println(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
        } else {
            System.out.println(FileSystem.currentPath.getAbsolutePath());
            return true;
        }
        return false;
    }
    
    public static String getName() {
        return "pwd";
    }

}
