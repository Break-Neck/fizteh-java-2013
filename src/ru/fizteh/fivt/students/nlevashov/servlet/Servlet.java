package ru.fizteh.fivt.students.nlevashov.servlet;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.nlevashov.factory.MyTable;
import ru.fizteh.fivt.students.nlevashov.factory.MyTableProvider;
import ru.fizteh.fivt.students.nlevashov.factory.MyTableProviderFactory;
import ru.fizteh.fivt.students.nlevashov.shell.Shell;


public class Servlet {
    static Server server;
    //public static Transactions transactions;
    static HashMap<Integer, MyTable> transactions;
    static MyTableProvider provider;

    public Servlet(int port) throws IOException {
        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        context.addServlet(new ServletHolder(new Begin()), "/begin");
        context.addServlet(new ServletHolder(new Commit()), "/commit");
        context.addServlet(new ServletHolder(new Rollback()), "/rollback");
        context.addServlet(new ServletHolder(new Get()), "/get");
        context.addServlet(new ServletHolder(new Put()), "/put");
        context.addServlet(new ServletHolder(new Size()), "/size");

        server.setHandler(context);

        String addr = System.getProperty("fizteh.db.dir");
        if (addr == null) {
            System.err.println("Property \"fizteh.db.dir\" wasn't set");
            System.exit(1);
        }
        Path addrPath = Shell.makePath(addr).toPath();
        MyTableProviderFactory factory = new MyTableProviderFactory();
        provider = factory.create(addrPath.toString());

        transactions = new HashMap<>();
    }

    public static class Begin extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String tableName = req.getParameter("table");

            if ((tableName == null) || (tableName.isEmpty())) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "table expected");
                return;
            }

            MyTable t = provider.getTable(tableName);
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "table not exists");
                return;
            }
            Integer key = transactions.size() + 1;
            transactions.put(key, t);
            String s = key.toString();
            t.addTransaction(key);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");

            String result = "tid=";
            for (int i = 0; i < 5 - s.length(); i++) {
                result = result + "0";
            }
            resp.getWriter().print(result + s);
        }
    }

    public static class Commit extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String tid = req.getParameter("tid");

            if ((tid == null) || (tid.isEmpty())) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid expected");
                return;
            }

            Integer intTid = Integer.parseInt(tid);
            MyTable t = transactions.get(intTid);
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid not defined");
                return;
            }
            Integer diff = t.commit(intTid);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");

            resp.getWriter().println("diff=" + diff.toString());
        }
    }

    public static class Rollback extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String tid = req.getParameter("tid");

            if ((tid == null) || (tid.isEmpty())) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid expected");
                return;
            }

            Integer intTid = Integer.parseInt(tid);
            MyTable t = transactions.get(intTid);
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid not defined");
                return;
            }
            Integer diff = t.rollback(intTid);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");

            resp.getWriter().println("diff=" + diff.toString());
        }
    }

    public static class Get extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String tid = req.getParameter("tid");
            String key = req.getParameter("key");

            if ((tid == null) || (tid.isEmpty())) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid expected");
                return;
            }
            if ((key == null) || (key.isEmpty())) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "key expected");
                return;
            }

            Integer intTid = Integer.parseInt(tid);
            MyTable t = transactions.get(intTid);
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid not defined");
                return;
            }
            Storeable value = t.get(key, intTid);
            if (value == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "key not exists");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");

            resp.getWriter().println(provider.serialize(t, value));
        }
    }

    public static class Put extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String tid = req.getParameter("tid");
            String key = req.getParameter("key");
            String value = req.getParameter("value");


            if ((tid == null) || (tid.isEmpty())) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid expected");
                return;
            }
            if ((key == null) || (key.isEmpty())) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "key expected");
                return;
            }
            if ((value == null) || (value.isEmpty())) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "value expected");
                return;
            }

            Integer intTid = Integer.parseInt(tid);
            MyTable t = transactions.get(intTid);
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid not defined");
                return;
            }
            Storeable ret;
            try {
                ret = t.put(key, provider.deserialize(t, value), intTid);
            } catch (ParseException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return;
            }
            if (ret == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "key not exists");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");

            resp.getWriter().println(provider.serialize(t, ret));
        }
    }

    public static class Size extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String tid = req.getParameter("tid");

            if ((tid == null) || (tid.isEmpty())) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid expected");
                return;
            }

            Integer intTid = Integer.parseInt(tid);
            MyTable t = transactions.get(intTid);
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid not defined");
                return;
            }
            Integer size = t.size(intTid);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");

            resp.getWriter().println(size);
        }
    }

    public void startServer() throws Exception {
        server.start();
    }

    public void stopServer() throws Exception {
        server.stop();
    }
}
