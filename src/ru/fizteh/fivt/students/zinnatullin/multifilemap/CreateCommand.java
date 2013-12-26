package ru.fizteh.fivt.students.zinnatullin.multifilemap;

public class CreateCommand implements ShellCommand {

    String name;

    public CreateCommand() {
        this.name = "create";
    }
    
    @Override
    public boolean execute(String[] args) {
        assert (args.length != 0);
        if (args.length != 2) {
            Shell.printMessage(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }

        boolean result = Shell.getInstance().getDB().createTable(args[1]);
        if (result) {
            Shell.printMessage("created");
            return true;
        } else {
            Shell.printMessage(args[1] + " exists");
            return false;
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
}