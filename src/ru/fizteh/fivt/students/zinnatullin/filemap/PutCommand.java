package ru.fizteh.fivt.students.zinnatullin.filemap;

public class PutCommand implements ShellCommand {
    
	String name;

	public PutCommand() {
		this.name = "put";
	}
	
	@Override
	public boolean execute(String args[]) {
        assert (args.length != 0);
        if (args.length != 3) {
            System.out.println(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }
		
		String value = "";
		value = Shell.getInstance().getDB().put(args[1], args[2]);
		if(value != "") {
			System.out.println("overwrite");
			System.out.println(value);
		} else {
			System.out.println("new");
		}
        return true;
    }
	
	@Override
	public String getName() {
		return name;
	}
}
