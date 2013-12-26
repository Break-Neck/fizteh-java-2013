package ru.fizteh.fivt.students.zinnatullin.storable;

public class CreateCommand implements ShellCommand {

	String name;

	public CreateCommand() {
		name = "create";
	}
	
	@Override
    public boolean execute(String args[]) {
        assert (args.length != 0);
        if (args.length != 2 ) {
			Shell.printMessage(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }

		DBTable table = (DBTable)Shell.getInstance().provider.createTable(args[1]);
        if (table != null) {
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