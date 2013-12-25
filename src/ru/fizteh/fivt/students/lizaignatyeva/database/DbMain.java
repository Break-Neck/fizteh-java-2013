package ru.fizteh.fivt.students.lizaignatyeva.database;


import ru.fizteh.fivt.students.lizaignatyeva.database.commands.*;
import ru.fizteh.fivt.students.lizaignatyeva.database.httpserver.ExitCommand;
import ru.fizteh.fivt.students.lizaignatyeva.database.httpserver.MyServer;
import ru.fizteh.fivt.students.lizaignatyeva.database.httpserver.StartHttpCommand;
import ru.fizteh.fivt.students.lizaignatyeva.database.httpserver.StopHttpCommand;
import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;
import ru.fizteh.fivt.students.lizaignatyeva.shell.CommandRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

public class DbMain {

    private static Hashtable<String, Command> createCommands(Database database, MyServer server) {
        Hashtable<String, Command> commandsMap = new Hashtable<String, Command>();
        commandsMap.put("put", new PutCommand(database));
        commandsMap.put("get", new GetCommand(database));
        commandsMap.put("remove", new RemoveCommand(database));
        commandsMap.put("use", new UseCommand(database));
        commandsMap.put("create", new CreateCommand(database));
        commandsMap.put("drop", new DropCommand(database));
        commandsMap.put("commit", new CommitCommand(database));
        commandsMap.put("rollback", new RollbackCommand(database));
        commandsMap.put("size", new SizeCommand(database));
        commandsMap.put("exit", new ExitCommand(server));
        commandsMap.put("starthttp", new StartHttpCommand(server));
        commandsMap.put("stophttp", new StopHttpCommand(server));
        return commandsMap;
    }

    public static void main(String[] args) {
        String dir = System.getProperty("fizteh.db.dir");
        MyTableProviderFactory providerFactory = new MyTableProviderFactory();
        MyTableProvider provider;
        final MyServer server;

        try {
            provider = providerFactory.create(dir);
            server = new MyServer(provider);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
            return;
        }

        Path directory = Paths.get(dir);
        CommandRunner runner = new CommandRunner(directory.toFile(), createCommands(new Database(provider), server));
        runner.run(args);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    server.stop();
                } catch (Exception e) {
                    // do nothing
                }
            }
        });
    }
}
