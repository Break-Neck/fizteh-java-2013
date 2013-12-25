package ru.fizteh.fivt.students.lizaignatyeva.database.httpserver;

import ru.fizteh.fivt.students.lizaignatyeva.shell.Command;

public class StartHttpCommand extends Command {
    private MyServer server;

    public StartHttpCommand(MyServer server) {
        this.server = server;
        name = "starthttp";
    }

    @Override
    protected boolean checkArguments(String[] args) throws Exception {
        if (args.length > 1) {
            return false;
        }
        return true;
    }

    @Override
    public void run(String[] args) throws Exception {
        if (server.isUp()) {
            System.out.println("not started: already started");
            return;
        }
        int port;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("not started: invalid port number");
                return;
            }
        } else {
            port = 10001;
        }
        try {
            server.start(port);
        } catch (Exception e) {
            System.out.println("not started: " + e.getMessage());
            return;
        }
        System.out.println("started at " + port);
    }
}
