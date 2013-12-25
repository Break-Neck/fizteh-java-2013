package ru.fizteh.fivt.students.dzvonarev.filemap;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Servlet {

    private Server server = new Server();
    private TransactionManager manager;

    public Servlet(TransactionManager transactionManager) {
        manager = transactionManager;
    }

    public void stop() throws IllegalStateException {
        if (server == null || !server.isStarted()) {
            throw new IllegalStateException("server is not started");
        }
        try {
            server.stop();
        } catch (Exception ignored) {
            /* it is ok */
        }
        server = null; // no server
    }

    public int getPort() {
        return server.getConnectors()[0].getPort();
    }

    public void start(int port) throws Exception {
        if (server != null && server.isStarted()) {
            throw new IllegalStateException("server is running");
        }
        server = new Server(port);  // connect to the port
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new ServletBegin(manager)), "/begin");
        context.addServlet(new ServletHolder(new ServletGet(manager)), "/get");
        context.addServlet(new ServletHolder(new ServletPut(manager)), "/put");
        context.addServlet(new ServletHolder(new ServletCommit(manager)), "/commit");
        context.addServlet(new ServletHolder(new ServletRollback(manager)), "/rollback");
        context.addServlet(new ServletHolder(new ServletSize(manager)), "/size");
        server.setHandler(context);
        server.start();
    }

}
