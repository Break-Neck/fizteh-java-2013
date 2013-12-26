package ru.fizteh.fivt.students.zinnatullin.filemap;

public class GetCommand implements ShellCommand {

	String name;

	public GetCommand() {
		this.name = "get";
	}
	
	@Override
    public boolean execute(String args[]) {
        assert (args.length != 0);
        if (args.length != 2 ) {
            System.out.println(args[0] + ": invalid number of arguments in the \'" + args[0] + "\' command");
            return false;
        }
		String value = "";
		value = Shell.getInstance().getDB().get(args[1]);
		if(value != ""){
			System.out.println("found");
			System.out.println(value);
		} else {
			System.out.println("not found");
		}
        return true;
    }
	
	@Override
	public String getName(){
		return name;
	}
}
