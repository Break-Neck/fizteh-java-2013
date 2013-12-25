package ru.fizteh.fivt.students.krivchansky.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class Shell<State> {
    private final Map<String, Commands> availableCommands;
    private static final String greeting = "$ ";
    State state;
    
    
    public Shell (Set<Commands<?>> commands) {
        Map <String, Commands> tempCommands = new HashMap<String, Commands>();
        for (Commands<?> temp : commands) {
            tempCommands.put(temp.getCommandName(), temp);
        }
        availableCommands = Collections.unmodifiableMap(tempCommands);
    }
    
    public void setShellState(State state) {
        this.state = state;
    }
    
    private void runCommand(String[] data, State state) throws SomethingIsWrongException { 
        for (String command : data) {
        	if (data[0].length() == 0) {
            	throw new SomethingIsWrongException ("Empty string.");
        	}
        	String name = Parser.getName(command);
        	Commands<State> usedOne = availableCommands.get(name);
        	String args = Parser.getParameters(command);
        	if (usedOne == null) {
            	throw new SomethingIsWrongException ("Unknown command.");
        	} 
        	usedOne.implement(args, state);
        	}
        }
    
    /* public static String[] splitLine(String str) {
    	str = str.trim();
        return str.split("(\\s*;\\s*)", -1);
    }
   private void runLine(String[] line, State state) throws SomethingIsWrongException {
        int count = line.length - 1;
        for (String temp : line) {
            --count;
            if (count >= 0) {
                runCommand(temp, state);     //This thing is needed to check if after last ";"
            } else if (count < 0) {                       				 //situated a command or an empty string. So it
                if (temp.length() != 0) {                 				 //won't throw a "Wrong command" exception when it
                    runCommand(temp, state); //looks like: "dir; cd ..; dir;" neither it will
                }                                         				 //loose a "dir" in: "dir; cd ..; dir; dir".
            }                                             				 //So it does nothing if it is the end, but performs
        }                                                 				 //if there is a command after last ";".
    }*/
    
    public void consoleWay(State state) {
        Scanner forInput = new Scanner(System.in);
        while (!Thread.currentThread().isInterrupted()) {	
            System.out.print(greeting);
            try {
            	String temp = forInput.nextLine();
            	String[] commands = Parser.parseFullCommand(temp);
                runCommand(commands, state);                  
            } catch (SomethingIsWrongException exc) {
                if (exc.getMessage().equals("EXIT")) {
                    forInput.close();
                    System.exit(0);
                } else {
                    System.err.println(exc.getMessage());
                }
            }// catch (IllegalArgumentException | IllegalStateException e) {
            //	System.err.println(e.getMessage());
            //}
        }
        forInput.close();
    }
    
    /*public static void main(String[] args) {
    	ShellState state = new ShellState(System.getProperty("user.dir"));
        Set<Commands> commands =  new HashSet<Commands>() {{ add(new WhereAreWeCommand()); add(new CopyCommand()); add(new DirectoryInfoCommand());
        	add(new ExitCommand()); add(new MakeDirectoryCommand()); add(new MoveCommand()); add(new RemoveCommand());  }};
        Shell<ShellState> shell = new Shell<ShellState>(commands);
      
        if (args.length != 0) {
            String arg = UtilMethods.uniteItems(Arrays.asList(args), " ");
            try {
                shell.runLine(arg, state);                  
            } catch (SomethingIsWrongException exc) {
                if (exc.getMessage().equals("EXIT")) {
                    System.exit(0);
                } else {
                    System.err.println(exc.getMessage());
                    System.exit(-1);
                }
            }
        } else {
            shell.consoleWay(state);
        }
        System.exit(0);
    }*/
    
    
    public void run(String[] args, Shell<State> shell) {
    	if (args.length != 0) {
            String arg = UtilMethods.uniteItems(Arrays.asList(args), " ");
            String[] commands = Parser.parseFullCommand(arg);
            try {
                shell.runCommand(commands, state);                  
            } catch (SomethingIsWrongException exc) {
                if (exc.getMessage().equals("EXIT")) {
                    System.exit(0);
                } else {
                    System.err.println(exc.getMessage());
                    System.exit(-1);
                }
            }
        } else {
            shell.consoleWay(state);
        }
        System.exit(0);
    }
    

}
