package ru.fizteh.fivt.students.zinnatullin.filemap;

import java.io.*;
import java.util.HashMap;

public class Shell {
	
	private HashMap<String, ShellCommand> commands;
	
	private Database db;
	
	private static Shell shell;	
	
	private Shell() {
        commands = new HashMap();
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
				
		Shell.getInstance().setDB();
		
		if(args.length > 0){
			Shell.getInstance().exec(args);
		} else {
			do{
				printSuggestMessage();
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String commandName = reader.readLine();
				if (commandName == null) {
					break;
				}
				args = commandName.split("[\\s]");
				Shell.getInstance().exec(args);
			} while(!args[0].equals("exit"));
		}
	}
	
	private void exec(String[] args) {  
		if(!commands.containsKey(args[0])){
			printMessage(args[0] + ": command not found");
		} else {
			commands.get(args[0]).execute(args);
		}
    }
	
	public void setDB(){
		String dir = System.getProperty("fizteh.db.dir");
		File file =  new File(dir);
		if(!file.isDirectory()){
			printMessage("Incorrect db path");
			System.exit(1);
		}
		db = new Database(dir, "db.dat");
	}
	
	public Database getDB(){
		return db;
	}
	
	public static void printMessage(final String message) {
        System.out.println(message);
    }
	
    public static void printSuggestMessage() {
        System.out.print(new File("").getAbsoluteFile().getName() + File.separator + "$ ");
    }
}
