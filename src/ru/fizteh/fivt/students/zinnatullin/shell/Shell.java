package ru.fizteh.fivt.students.zinnatullin.shell;

import java.io.*;
import java.util.HashMap;

public class Shell {
    private HashMap<String,ShellCommand> commands;
    
    public Shell() {
        commands = new HashMap();
        commands.put(CdCommand.getName(), new CdCommand());
        commands.put(CpCommand.getName(), new CpCommand());
        commands.put(DirCommand.getName(), new DirCommand());
        commands.put(ExitCommand.getName(), new ExitCommand());
        commands.put(MkdirCommand.getName(), new MkdirCommand());
        commands.put(MvCommand.getName(), new MvCommand());
        commands.put(PwdCommand.getName(), new PwdCommand());
        commands.put(RmCommand.getName(), new RmCommand());
    }
    
    public static void main(String[] args) throws IOException {
        Shell shell = new Shell();
        
        if (args.length > 0) {
            shell.exec(args);
        } else {
            do{
                printSuggestMessage();
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String commandName = reader.readLine();
                if (commandName == null) {
                    break;
                }
                args = commandName.split("[\\s]");
                shell.exec(args);
            } while(!args[0].equals("exit"));
        }
    }

    private void exec(String[] args) {  
        if (!commands.containsKey(args[0])) {
            printMessage(args[0] + ": command not found");
        } else {
            commands.get(args[0]).execute(args);
        }
    }
    
    public static void printMessage(final String message) {
        System.out.println(message);
    }
    
    public static void printSuggestMessage() {
        System.out.print(FileSystem.currentPath.getName() + File.separator + "$ ");
    }
}
