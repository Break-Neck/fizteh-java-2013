package ru.fizteh.fivt.students.zinnatullin.storable;

public class SizeCommand implements ShellCommand {

    String name;

    public SizeCommand() {
        name = "size";
    }
    
    @Override
    public boolean execute(String[] args) {
        assert (args.length != 0);
        if (args.length != 1) {
            Shell.printMessage(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }

        if (Shell.getInstance().table == null) {
            Shell.printMessage("empty table");
            return false;
        }
        
        int size = 0;
        size = Shell.getInstance().table.size();
        Shell.printMessage("size: " + size);
        return true;
    }
    
    @Override
    public String getName() {
        return name;
    }
}
