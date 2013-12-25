package ru.fizteh.fivt.students.lizaignatyeva.database.httpserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.fizteh.fivt.students.lizaignatyeva.database.MyTableProvider;

public class MyServer {
    private Database database;
    private Server server;
    private int port;

    public MyServer(MyTableProvider tableProvider) {
        database = new Database(tableProvider);
    }

    public void start(int port) throws Exception {
        try {
            server = new Server(port);
            this.port = port;

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/");

            context.addServlet(new ServletHolder(new BeginServlet(database)), "/begin");
            context.addServlet(new ServletHolder(new CommitServlet(database)), "/commit");
            context.addServlet(new ServletHolder(new RollbackServlet(database)), "/rollback");
            context.addServlet(new ServletHolder(new GetServlet(database)), "/get");
            context.addServlet(new ServletHolder(new PutServlet(database)), "/put");
            context.addServlet(new ServletHolder(new SizeServlet(database)), "/size");

            server.setHandler(context);
            server.start();
        } catch (Exception e) {
            server = null;
            throw e;
        }
    }

    public boolean isUp() {
        return server != null;
    }

    public int getPort() {
        return port;
    }

    public void stop() throws Exception {
        try {
            server.stop();
        } catch (Exception e) {
            // do nothing
        } finally {
            server = null;
        }
    }
}
