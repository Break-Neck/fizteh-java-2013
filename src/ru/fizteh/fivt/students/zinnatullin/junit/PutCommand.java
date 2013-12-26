package ru.fizteh.fivt.students.zinnatullin.junit;

public class PutCommand implements ShellCommand {
    
	String name;

	public PutCommand() {
		name = "put";
	}
	
	@Override
	public boolean execute(String args[]) {
        assert (args.length != 0);
        if (args.length != 3) {
            System.out.println(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }
		
		if(Shell.getInstance().table == null){
			Shell.printMessage("empty table");
			return false;
		}
		
		String value = null;
		value = Shell.getInstance().table.put(args[1], args[2]);
		if(value != null){
			System.out.println("overwrite");
			System.out.println(value);
		} else {
			System.out.println("new");
		}
        return true;
    }
	
	@Override
	public String getName(){
		return name;
	}
}
