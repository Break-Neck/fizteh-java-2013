package ru.fizteh.fivt.students.musin.filemap;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;


public class HTTPDatabaseServer {
    FileMapProvider provider;
    TransactionPool transactionPool;
    Server server;
    boolean started;
    int port;

    public HTTPDatabaseServer(FileMapProvider provider, TransactionPool transactionPool) {
        this.provider = provider;
        this.transactionPool = transactionPool;
        started = false;
    }

    public class BeginServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            String name = req.getParameter("table");
            if (name == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Table name expected");
                return;
            }
            MultiFileMap table = provider.getTable(name);
            if (table == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Specified table doesn't exist");
                return;
            }
            String id = transactionPool.createTransaction(table);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");
            resp.getWriter().println(String.format("tid=%s", id));
        }
    }

    public class CommitServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            String id = req.getParameter("tid");
            if (id == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Transaction id expected");
                return;
            }
            Transaction transaction;
            try {
                transaction = transactionPool.getTransaction(id);
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }
            int changes = transaction.getTable().commit(transaction.getDiff());
            transactionPool.removeTransaction(id);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");
            resp.getWriter().println(String.format("diff=%d", changes));
        }
    }

    public class RollbackServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            String id = req.getParameter("tid");
            if (id == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Transaction id expected");
                return;
            }
            Transaction transaction;
            try {
                transaction = transactionPool.getTransaction(id);
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }
            int changes = transaction.getTable().rollback(transaction.getDiff());
            transactionPool.removeTransaction(id);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");
            resp.getWriter().println(String.format("diff=%d", changes));
        }
    }

    public class GetServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            String id = req.getParameter("tid");
            if (id == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Transaction id expected");
                return;
            }
            String key = req.getParameter("key");
            if (key == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "key expected");
                return;
            }
            Transaction transaction;
            try {
                transaction = transactionPool.getTransaction(id);
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }
            Storeable value = transaction.getTable().get(transaction.getDiff(), key);
            if (value == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "not found");
                return;
            }
            String answer;
            try {
                answer = provider.serialize(transaction.getTable(), value);
            } catch (ColumnFormatException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");
            resp.getWriter().println(answer);
        }
    }

    public class PutServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            String id = req.getParameter("tid");
            if (id == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Transaction id expected");
                return;
            }
            String key = req.getParameter("key");
            if (key == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "key expected");
                return;
            }
            String valueString = req.getParameter("value");
            if (valueString == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "value expected");
                return;
            }
            Transaction transaction;
            try {
                transaction = transactionPool.getTransaction(id);
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }
            Storeable value;
            try {
                value = provider.deserialize(transaction.getTable(), valueString);
            } catch (ParseException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }
            Storeable oldValue = transaction.getTable().put(transaction.getDiff(), key, value);
            String answer;
            try {
                if (oldValue == null) {
                    //answer = "new";
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "new");
                    return;
                } else {
                    answer = provider.serialize(transaction.getTable(), oldValue);
                }
            } catch (ColumnFormatException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");
            resp.getWriter().println(answer);
        }
    }

    public class RemoveServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            String id = req.getParameter("tid");
            if (id == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Transaction id expected");
                return;
            }
            String key = req.getParameter("key");
            if (key == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "key expected");
                return;
            }
            Transaction transaction;
            try {
                transaction = transactionPool.getTransaction(id);
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }
            Storeable value = transaction.getTable().remove(transaction.getDiff(), key);
            if (value == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "not found");
                return;
            }
            String answer;
            try {
                answer = provider.serialize(transaction.getTable(), value);
            } catch (ColumnFormatException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");
            resp.getWriter().println(answer);
        }
    }

    public class SizeServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            String id = req.getParameter("tid");
            if (id == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Transaction id expected");
                return;
            }
            Transaction transaction;
            try {
                transaction = transactionPool.getTransaction(id);
            } catch (IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }
            int changes = transaction.getTable().size(transaction.getDiff());
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");
            resp.getWriter().println(changes);
        }
    }

    public void start(int port) throws Exception {
        server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new BeginServlet()), "/begin");
        context.addServlet(new ServletHolder(new CommitServlet()), "/commit");
        context.addServlet(new ServletHolder(new RollbackServlet()), "/rollback");
        context.addServlet(new ServletHolder(new GetServlet()), "/get");
        context.addServlet(new ServletHolder(new PutServlet()), "/put");
        context.addServlet(new ServletHolder(new RemoveServlet()), "/remove");
        context.addServlet(new ServletHolder(new SizeServlet()), "/size");
        server.setHandler(context);
        server.start();
        started = true;
        this.port = port;
    }

    public int stop() throws Exception {
        if (started) {
            server.stop();
            started = false;
        }
        return port;
    }

    public boolean isStarted() {
        return started;
    }
}
