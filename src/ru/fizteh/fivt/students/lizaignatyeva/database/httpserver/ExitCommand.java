package ru.fizteh.fivt.students.lizaignatyeva.database.httpserver;

import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;

public class ExitCommand extends Command {
    MyServer server;

    public ExitCommand(MyServer server) {
        this.server = server;
        argumentsAmount = 0;
        name = "exit";
    }

    @Override
    public void run(String[] args) throws Exception {
        try {
            server.stop();
        } catch (Exception e) {
            // do nothing
        }
        System.exit(0);
    }
}
