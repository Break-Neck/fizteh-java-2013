package ru.fizteh.fivt.students.zinnatullin.junit;

import java.io.*;
import java.util.HashMap;

public class Shell {
	
	private HashMap<String, ShellCommand> commands;
	
	public DBTableProvider provider;
	public DBTable table;
	
	private static Shell shell;	
	
	private Shell() {
        commands = new HashMap();
		commands.put(new PutCommand().getName(), new PutCommand());
		commands.put(new CreateCommand().getName(), new CreateCommand());
		commands.put(new DropCommand().getName(), new DropCommand());
		commands.put(new UseCommand().getName(), new UseCommand());
		commands.put(new CommitCommand().getName(), new CommitCommand());
		commands.put(new RollbackCommand().getName(), new RollbackCommand());
		commands.put(new SizeCommand().getName(), new SizeCommand());
        commands.put(new PutCommand().getName(), new PutCommand());
        commands.put(new GetCommand().getName(), new GetCommand());
        commands.put(new RemoveCommand().getName(), new RemoveCommand());
        commands.put(new ExitCommand().getName(), new ExitCommand());
    }

	public static Shell getInstance(){
		if(shell == null){
			shell = new Shell();
		}
		return shell;
	}
	
	public static void main(String[] args) throws IOException{
		
		String dbPath = System.getProperty("fizteh.db.dir");
		if(dbPath.isEmpty()){
			printMessage("Empty db path");
			System.exit(1);
		}
		
		DBTableProviderFactory providerFactory = new DBTableProviderFactory();
		DBTableProvider provider = (DBTableProvider)providerFactory.create(dbPath);
		
		if(provider == null){
			printMessage("Incorrect db path");
			System.exit(1);
		}
		Shell.getInstance().provider = provider;
		
		if(args.length > 0){
			Shell.getInstance().exec(args);
		} else {
			boolean status = false;
			do{
				printSuggestMessage();
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String commandName = reader.readLine();
				if (commandName == null) {
					break;
				}
				args = commandName.split("[\\s]");
				status = Shell.getInstance().exec(args);
			} while(!(args[0].equals("exit") && status));
		}
	}
	
	private boolean exec(String[] args) {  
		boolean status = false;
		if(!commands.containsKey(args[0])){
			printMessage(args[0] + ": command not found");
		} else {
			status = commands.get(args[0]).execute(args);
		}
		return status;
    }
		
	public static void printMessage(final String message) {
        System.out.println(message);
    }
	
    public static void printSuggestMessage() {
        System.out.print(new File("").getAbsoluteFile().getName() + File.separator + "$ ");
    }
}
