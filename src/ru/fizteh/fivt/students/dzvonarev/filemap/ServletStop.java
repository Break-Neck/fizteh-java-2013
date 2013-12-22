package ru.fizteh.fivt.students.dzvonarev.filemap;

import ru.fizteh.fivt.students.dzvonarev.shell.CommandInterface;

import java.io.IOException;
import java.util.ArrayList;

public class ServletStop implements CommandInterface {

    private MyTableProvider provider;

    public ServletStop(MyTableProvider tableProvider) {
        provider = tableProvider;
    }

    public void execute(ArrayList<String> args) throws IOException, IllegalArgumentException {
        String str = args.get(0);
        int spaceIndex = str.indexOf(' ', 0);
        if (spaceIndex != -1) {
            throw new IllegalArgumentException("HttpStop: wrong input");
        }
        try {
            Servlet currServlet = Server.getServlet();
            if (!Server.isStarted()) {
                System.out.println("not started");
                return;
            }
            System.out.println("stopped at " + currServlet.getPort());
            currServlet.stop();
            Server.stopServlet();
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

}
