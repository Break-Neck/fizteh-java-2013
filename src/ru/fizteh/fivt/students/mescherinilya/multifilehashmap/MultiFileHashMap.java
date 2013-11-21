package ru.fizteh.fivt.students.mescherinilya.multifilehashmap;

import java.io.*;
import java.util.*;

public class MultiFileHashMap {

    private static Map<String, Command> commandList;

    static File rootDir;

    public static boolean batchMode(String input) throws TimeToExitException {

        String[] commands = input.split("\\s*;\\s*");

        for (String cmd : commands) {
            cmd = cmd.trim(); //сомневаюсь, нужно ли это
            if (cmd.isEmpty()) {
                continue;
            }

            String cmdName;
            String[] cmdArgs;
            if (cmd.contains(" ")) {
                cmdName = cmd.substring(0, cmd.indexOf(" "));
                cmdArgs = cmd.substring(cmd.indexOf(" ") + 1, cmd.length())
                        .trim().split("\\s+");
                for (String cmdArg : cmdArgs) {
                    cmdArg = cmdArg.trim();
                }
            } else {
                cmdName = cmd;
                cmdArgs = new String[0];
            }

            if (cmdName.equals("exit")) {
                throw new TimeToExitException();
            }

            Command command = commandList.get(cmdName);
            if (command != null) {
                if (command.getArgsCount() != cmdArgs.length) {
                    System.err.println(command.getName() + ": Wrong count of arguments!");
                    return false;
                }
                try {
                    command.execute(cmdArgs);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    return false;
                }
            } else {
                System.err.println("Unknown command: " + cmdName);
                return false;
            }
        }

        return true;
    }

    public static void interactiveMode() throws TimeToExitException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            batchMode(input);
        }


    }

    public static void main(String[] args) {

        commandList = new HashMap<String, Command>();
        commandList.put("put", new CommandPut());
        commandList.put("get", new CommandGet());
        commandList.put("remove", new CommandRemove());
        commandList.put("create", new CommandCreate());
        commandList.put("drop", new CommandDrop());
        commandList.put("use", new CommandUse());

        DatabaseWorker.storage = new TreeMap<String, String>();

        rootDir = new File(System.getProperty("fizteh.db.dir")).getAbsoluteFile();
        if (!rootDir.exists() || !rootDir.isDirectory()
                || !rootDir.canRead() || !rootDir.canWrite()) {
            System.err.println("Bad root directory!");
            System.exit(1);
        }

        try {
            if (args.length == 0) {
                interactiveMode();
            } else {
                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    sb.append(arg).append(" ");

                }

                batchMode(sb.toString());
            }
        } catch (TimeToExitException te) {
            try {
                DatabaseWorker.writeDatabase();

            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }

            System.exit(0);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }


        System.exit(1);
    }

}
