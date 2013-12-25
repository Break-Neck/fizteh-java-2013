package ru.fizteh.fivt.students.ichalovaDiana.filemap;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import ru.fizteh.fivt.storage.structured.Storeable;

public class JettyServer {
    
    static final int DEFAULT_PORT = 10001;
    static final int MAX_TRANSACTION_ID = 99999;
    
    private Server server;
    private int port;
    
    private TableProviderImplementation tableProvider;
    
    private ConcurrentHashMap<String, TableImplementation> transactions 
        = new ConcurrentHashMap<String, TableImplementation>();
    private final Lock lock = new ReentrantLock(true);
    
    public JettyServer(TableProviderImplementation tableProvider) {
        this.tableProvider = tableProvider;
    }
    
    public void init(int port) {
        server = new Server(port);
        this.port = port;
        setContext();
    }
    
    public void init() {
        server = new Server(DEFAULT_PORT);
        port = DEFAULT_PORT;
        setContext();
    }
    
    private void setContext() {
        ServletContextHandler context;
        context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        context.addServlet(new ServletHolder(new BeginServlet()), "/begin");
        context.addServlet(new ServletHolder(new CommitServlet()), "/commit");
        context.addServlet(new ServletHolder(new RollbackServlet()), "/rollback");
        context.addServlet(new ServletHolder(new GetServlet()), "/get");
        context.addServlet(new ServletHolder(new PutServlet()), "/put");
        context.addServlet(new ServletHolder(new SizeServlet()), "/size");

        server.setHandler(context);
    }
    
    public void start() throws Exception {
        server.start();
    }
    
    public void stop() throws Exception {
        server.stop();
    }
    
    public int getPort() {
        return port;
    }
    
    public boolean isStarted() {
        return server.isStarted();
    }
    
    private String getNewTransactionID(TableImplementation table) {
        String tid;
        for (int id = 0; id <= MAX_TRANSACTION_ID; ++id) {
            tid = String.format("%05d", id);
            
            if (!transactions.containsKey(tid)) {
                lock.lock();
                try {
                    if (!transactions.containsKey(tid)) {
                        transactions.put(tid, table);
                        return tid;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
        return null;
    }
    
    public class BeginServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            
            String tablename = request.getParameter("table");
            
            if (tablename == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "missing arguments: should be table");
                return;
            }
            
            TableImplementation table = (TableImplementation) tableProvider.getTable(tablename);
            if (table == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, tablename + " not exists");
                return;
            }
            
            String transactionID = getNewTransactionID(table);
            
            if (transactionID == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server is too busy");
                return;
            }
            
            table.createTransaction(transactionID);

            response.setStatus(HttpServletResponse.SC_OK);

            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF8");

            response.getWriter().println("tid=" + transactionID);
        }
    }
    
    public class CommitServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            
            String transactionID = request.getParameter("tid");
            
            if (transactionID == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "missing arguments: should be tid");
                return;
            }
            if (!transactions.containsKey(transactionID)) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "transaction not found");
                return;
            }
            
            TableImplementation table = transactions.get(transactionID);
            
            int diff = table.commit(transactionID);
            
            transactions.remove(transactionID);

            response.setStatus(HttpServletResponse.SC_OK);

            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF8");

            response.getWriter().println("diff=" + diff);
        }
    }
    
    public class RollbackServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            
            String transactionID = request.getParameter("tid");
            
            if (transactionID == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "missing arguments: should be tid");
                return;
            }
            if (!transactions.containsKey(transactionID)) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "transaction not found");
                return;
            }
            
            TableImplementation table = transactions.get(transactionID);
            
            int diff = table.rollback(transactionID);
            
            transactions.remove(transactionID);

            response.setStatus(HttpServletResponse.SC_OK);

            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF8");

            response.getWriter().println("diff=" + diff);
        }
    }
    
    public class GetServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            
            String transactionID = request.getParameter("tid");
            String key = request.getParameter("key");
            
            if (transactionID == null || key == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "missing arguments: should be tid and key");
                return;
            }
            if (!transactions.containsKey(transactionID)) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "transaction not found");
                return;
            }
            
            TableImplementation table = transactions.get(transactionID);
            
            Storeable value = table.get(transactionID, key);
            
            if (value == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, key + " not found");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);

            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF8");

            response.getWriter().println(tableProvider.serialize(table, value));
        }
    }
    
    public class PutServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            
            String transactionID = request.getParameter("tid");
            String key = request.getParameter("key");
            String valueString = request.getParameter("value");
            
            if (transactionID == null || key == null || valueString == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "missing arguments: should be tid, key and value");
                return;
            }
            if (!transactions.containsKey(transactionID)) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "transaction not found");
                return;
            }
            
            TableImplementation table = transactions.get(transactionID);
            
            Storeable value;
            try {
                value = tableProvider.deserialize(table, valueString);
            } catch (ParseException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid value: " + e.getMessage());
                return;
            }
            
            Storeable oldValue = table.put(transactionID, key, value);
            
            if (oldValue == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "new value");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);

            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF8");

            response.getWriter().println(tableProvider.serialize(table, oldValue));
        }
    }
    
    public class SizeServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            
            String transactionID = request.getParameter("tid");
            
            if (transactionID == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "missing arguments: should be tid");
                return;
            }
            if (!transactions.containsKey(transactionID)) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "transaction not found");
                return;
            }
            
            TableImplementation table = transactions.get(transactionID);
            
            int size = table.size(transactionID);

            response.setStatus(HttpServletResponse.SC_OK);

            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF8");

            response.getWriter().println(size);
        }
    }
}
