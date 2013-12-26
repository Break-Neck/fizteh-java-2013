package ru.fizteh.fivt.students.zinnatullin.junit;

public class RollbackCommand implements ShellCommand {

	String name;

	public RollbackCommand() {
		name = "rollback";
	}
	
	@Override
    public boolean execute(String args[]) {
        assert (args.length != 0);
        if (args.length != 1) {
            Shell.printMessage(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }

		if(Shell.getInstance().table == null) {
			Shell.printMessage("empty table");
			return false;
		}
		
		int operations = 0;
		operations = Shell.getInstance().table.rollback();
		Shell.printMessage("operations: " + operations);
        return true;
    }
	
	@Override
	public String getName() {
		return name;
	}
}