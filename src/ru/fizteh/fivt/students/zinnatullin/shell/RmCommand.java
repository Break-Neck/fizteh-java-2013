package ru.fizteh.fivt.students.zinnatullin.shell;

import java.io.File;

public class RmCommand implements ShellCommand {
    public boolean execute(String[] args) {
        if (args.length != 2) {
            System.out.println(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }

         File file = FileSystem.resolvePath(args[1]);
        if (file.exists()) {
            return FileSystem.recursiveRemove(file, args[0]);
        }
        System.out.println(args[0] + ": cannot remove \'" + args[1] + "\': No such file or directory");
        return false;
    }
    
    public static String getName() {
        return "rm";
    }

}