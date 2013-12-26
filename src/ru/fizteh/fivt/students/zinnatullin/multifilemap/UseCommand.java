package ru.fizteh.fivt.students.zinnatullin.multifilemap;

public class UseCommand implements ShellCommand {

	String name;

	public UseCommand() {
		this.name = "use";
	}
	
	@Override
    public boolean execute(String[] args) {
        assert (args.length != 0);
        if (args.length != 2 ) {
            Shell.printMessage(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }

		boolean result = Shell.getInstance().getDB().useTable(args[1]);
        if (result) {
			Shell.printMessage("use " + args[1]);
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
