package ru.fizteh.fivt.students.zinnatullin.filemap;

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
		value = Shell.getInstance().getDB().remove(args[1]);
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
