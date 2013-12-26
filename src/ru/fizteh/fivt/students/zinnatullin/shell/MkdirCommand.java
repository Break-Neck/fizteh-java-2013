package ru.fizteh.fivt.students.zinnatullin.shell;
import java.io.File;

public class MkdirCommand implements ShellCommand {
    public boolean execute(String[] args) {
        if (args.length != 2) {
            System.out.println(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }
        if (!(new File(FileSystem.currentPath.getAbsolutePath() + File.separator + args[1]).mkdir())) {
            System.out.println(args[0] + ": \'" + args[1] + "\': couldn't create directory");
            return false;
        }
        return true;
        
    }
    
    public static String getName() {
        return "mkdir";
    }

}
