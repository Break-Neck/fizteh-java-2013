package ru.fizteh.fivt.students.zinnatullin.storable;

public class UseCommand implements ShellCommand {

	String name;

	public UseCommand() {
		name = "use";
	}
	
	@Override
    public boolean execute(String[] args) {
        assert (args.length != 0);
        if (args.length != 2 ) {
            Shell.printMessage(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }

		if (Shell.getInstance().table != null && Shell.getInstance().table.getOperations() > 0) {
			Shell.printMessage(Shell.getInstance().table.getOperations() + " unsaved changes");
			return false;
		}
		
		if (Shell.getInstance().provider == null) {
			Shell.printMessage("Empty table provider");
			return false;
		}
		
		DBTable table = (DBTable)Shell.getInstance().provider.getTable(args[1]);
        if (table != null) {
			Shell.printMessage("use " + args[1]);
			Shell.getInstance().table = table;
			return true;
		} else {
			Shell.printMessage(args[1] + " not exists");
			return false;
		}
    }
	
	@Override
	public String getName() {
		return name;
	}
}
