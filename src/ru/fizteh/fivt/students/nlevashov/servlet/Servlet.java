package ru.fizteh.fivt.students.nlevashov.servlet;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;


public class Servlet {
    Server server;
    public static Transactions transactions;

    public Servlet() throws IOException {
        server = new Server(8008);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        context.addServlet(new ServletHolder(new Begin()), "/begin");
        context.addServlet(new ServletHolder(new Commit()), "/commit");
        context.addServlet(new ServletHolder(new Rollback()), "/rollback");
        context.addServlet(new ServletHolder(new Get()), "/get");
        context.addServlet(new ServletHolder(new Put()), "/put");

        server.setHandler(context);

        transactions = new Transactions();
    }

    public static class Begin extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String tableName = req.getParameter("table");

            if ((tableName == null) || (tableName.isEmpty())) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "table expected");
                return;
            }

            Integer tid = transactions.getTid(tableName);
            if (tid == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "table not exists");
                return;
            }
            String s = tid.toString();

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

            Table t = transactions.getTable(Integer.parseInt(tid));
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid not defined");
                return;
            }
            Integer diff = t.commit();

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

            Table t = transactions.getTable(Integer.parseInt(tid));
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid not defined");
                return;
            }
            Integer diff = t.rollback();

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

            Table t = transactions.getTable(Integer.parseInt(tid));
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid not defined");
                return;
            }
            Storeable value = t.get(key);
            if (value == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "key not exists");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");

            resp.getWriter().println(transactions.getProvider().serialize(t, value));
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

            Table t = transactions.getTable(Integer.parseInt(tid));
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid not defined");
                return;
            }
            Storeable ret;
            try {
                ret = t.put(key, transactions.getProvider().deserialize(t, value));
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

            resp.getWriter().println(transactions.getProvider().serialize(t, ret));
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

            Table t = transactions.getTable(Integer.parseInt(tid));
            if (t == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid not defined");
                return;
            }
            Integer size = t.size();

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
