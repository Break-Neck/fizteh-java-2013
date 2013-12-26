package ru.fizteh.fivt.students.zinnatullin.junit;

public class RemoveCommand implements ShellCommand {
	
	String name;

	public RemoveCommand() {
		name = "remove";
	}
	
	@Override
    public boolean execute(String args[]) {
        assert (args.length != 0);
        if (args.length != 2) {
            System.out.println(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }
		
		if (Shell.getInstance().table == null) {
			Shell.printMessage("empty table");
			return false;
		}
		
		String value = null;
		value = Shell.getInstance().table.remove(args[1]);
		if (value != null) {
			System.out.println("removed");
			System.out.println(value);
		} else {
			System.out.println("not found");
		}
        return true;
    }
	
	@Override
	public String getName() {
		return name;
	}
}
