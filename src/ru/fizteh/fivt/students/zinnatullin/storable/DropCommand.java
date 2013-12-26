package ru.fizteh.fivt.students.zinnatullin.storable;

public class DropCommand implements ShellCommand {

	String name;

	public DropCommand() {
		name = "drop";
	}
	
	@Override
    public boolean execute(String args[]) {
        assert (args.length != 0);
        if (args.length != 2 ) {
            Shell.printMessage(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }

		if (Shell.getInstance().table.getName().equals(args[1]) && Shell.getInstance().table.getOperations() > 0) {
			Shell.printMessage(Shell.getInstance().table.getOperations() + " unsaved changes");
			return false;
		}
		
		DBTable table = (DBTable)Shell.getInstance().provider.getTable(args[1]);
        if (table != null) {
			Shell.getInstance().provider.removeTable(args[1]);
			Shell.printMessage("dropped");
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
