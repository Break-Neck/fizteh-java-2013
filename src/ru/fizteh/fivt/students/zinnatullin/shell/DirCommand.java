package ru.fizteh.fivt.students.zinnatullin.shell;

public class DirCommand implements ShellCommand {
    public boolean execute(String[] args) {
        assert (args.length != 0);
        if (args.length != 1) {
            System.out.println(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }
        String[] directories = FileSystem.currentPath.list();
        if (directories != null) {
            for (String directory : directories) {
                System.out.println(directory);
            }
        }
        return true;
    }
    
    public static String getName() {
        return "dir";
    }

}