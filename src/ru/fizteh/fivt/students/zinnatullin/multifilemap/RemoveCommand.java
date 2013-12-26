package ru.fizteh.fivt.students.zinnatullin.multifilemap;

public class RemoveCommand implements ShellCommand {
	
	String name;

	public RemoveCommand() {
		this.name = "remove";
	}
	
	@Override
    public boolean execute(String[] args) {
        assert (args.length != 0);
        if (args.length != 2) {
            System.out.println(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }
		
		String value = "";
		try {
			value = Shell.getInstance().getDB().remove(args[1]);
		} catch (Exception ex) {
			Shell.printMessage(ex.getMessage());
			return false;
		}
		if (value != "") {
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
