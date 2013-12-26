package ru.fizteh.fivt.students.zinnatullin.shell;

import java.io.File;

public class CpCommand implements ShellCommand {
    public boolean execute(String[] args) {
        if (args.length != 3) {
            System.out.println(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }
        File source = FileSystem.resolvePath(args[1]);
        File destination = FileSystem.resolvePath(args[2]);
        
        return FileSystem.safeCopy(source, destination, getName());
    }
    
    public static String getName() {
        return "cp";
    }

}