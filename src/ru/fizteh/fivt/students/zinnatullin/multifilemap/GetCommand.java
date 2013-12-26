package ru.fizteh.fivt.students.zinnatullin.multifilemap;

public class GetCommand implements ShellCommand {

	String name;

	public GetCommand() {
		this.name = "get";
	}
	
	@Override
    public boolean execute(String[] args) {
        assert (args.length != 0);
        if (args.length != 2 ) {
            Shell.printMessage(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }
		String value = null;
		try {
			value = Shell.getInstance().getDB().get(args[1]);
		} catch (Exception ex) {
			Shell.printMessage(ex.getMessage());
			return false;
		}
		if (value != null) {
			System.out.println("found");
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
