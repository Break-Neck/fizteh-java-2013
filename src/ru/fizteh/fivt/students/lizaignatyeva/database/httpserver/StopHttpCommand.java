package ru.fizteh.fivt.students.lizaignatyeva.database.httpserver;

import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;

public class StopHttpCommand extends Command {
    MyServer server;

    public StopHttpCommand(MyServer server) {
        this.server = server;
        this.argumentsAmount = 0;
        this.name = "stophttp";
    }

    @Override
    public void run(String[] args) throws Exception {
        if (!server.isUp()) {
            System.out.println("not started");
            return;
        }
        server.stop();
        System.out.println("stopped at " + server.getPort());
    }
}
