package ru.fizteh.fivt.students.zinnatullin.shell;
import java.io.File;

public class CdCommand implements ShellCommand {
    public boolean execute(String[] args) {
        if (args.length != 2) {
            System.out.println(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }

        File newPath = FileSystem.resolvePath(args[1]);
        if (newPath == null || !newPath.exists()) {
            System.out.println(args[0] + ": \'" + args[1] + "\': No such file or directory");
        } else if (!newPath.isDirectory()) {
            System.out.println(args[0] + ": \'" + args[1] + "': expected directory name, but file found");
        } else {
            FileSystem.currentPath = newPath.getAbsoluteFile();
            return true;
        }
        return false;
    }
	
    public static String getName() {
        return "cd";
    }

}