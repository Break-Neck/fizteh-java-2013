package ru.fizteh.fivt.students.dzvonarev.filemap;

import ru.fizteh.fivt.students.dzvonarev.shell.CommandInterface;

import java.io.IOException;
import java.util.ArrayList;

public class ServletStart implements CommandInterface {

    private MyTableProvider provider;

    public ServletStart(MyTableProvider tableProvider) {
        provider = tableProvider;
    }

    public void execute(ArrayList<String> args) throws IOException, IllegalArgumentException {
        String str = args.get(0);
        int port = 8080;
        int spaceIndex = str.indexOf(' ', 0);
        if (spaceIndex != -1) {
            while (str.indexOf(' ', spaceIndex + 1) == spaceIndex + 1) {
                ++spaceIndex;
            }
            if (str.indexOf(' ', spaceIndex + 1) != -1) {
                throw new IllegalArgumentException("HttpStart: wrong input");
            }
            port = Integer.parseInt(str.substring(spaceIndex + 1, str.length()));
        }
        try {
            Servlet servlet = new Servlet(new TransactionManager(provider));
            servlet.start(port);
            Server.assignServlet(servlet);
            System.out.println("started at " + servlet.getPort());
        } catch (Exception e) {
            System.out.println("not started: " + e.getMessage());
        }
    }

}
