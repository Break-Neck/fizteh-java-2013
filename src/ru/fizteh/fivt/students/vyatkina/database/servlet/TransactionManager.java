package ru.fizteh.fivt.students.vyatkina.database.servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.fizteh.fivt.students.vyatkina.State;
import ru.fizteh.fivt.students.vyatkina.database.StorableDatabaseAdapter;
import ru.fizteh.fivt.students.vyatkina.database.StorableTable;
import ru.fizteh.fivt.students.vyatkina.database.servlet.commands.BeginCommand;
import ru.fizteh.fivt.students.vyatkina.database.servlet.commands.CommitCommand;
import ru.fizteh.fivt.students.vyatkina.database.servlet.commands.GetCommand;
import ru.fizteh.fivt.students.vyatkina.database.servlet.commands.PutCommand;
import ru.fizteh.fivt.students.vyatkina.database.servlet.commands.RollbackCommand;
import ru.fizteh.fivt.students.vyatkina.database.servlet.commands.ServletCommand;
import ru.fizteh.fivt.students.vyatkina.database.servlet.commands.SizeCommand;
import ru.fizteh.fivt.students.vyatkina.database.storable.StorableTableProviderImp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TransactionManager {

    private Map<Integer, String> tables = new HashMap<>();
    private Server server = null;
    private int port;
    private State state;
    private StorableTableProviderImp tableProvider;
    private ReadWriteLock transactionKeeper = new ReentrantReadWriteLock(true);
    private static final String NOT_STARTED = "not started";
    private List<ServletCommand> servletCommands = new ArrayList<>();

    public TransactionManager(State state, StorableTableProviderImp tableProvider) {
        this.state = state;
        this.tableProvider = tableProvider;
        servletCommands.add(new BeginCommand(this));
        servletCommands.add(new CommitCommand(this));
        servletCommands.add(new GetCommand(this));
        servletCommands.add(new PutCommand(this));
        servletCommands.add(new RollbackCommand(this));
        servletCommands.add(new SizeCommand(this));
    }

    public boolean setPort(String portString) {
        try {
            port = Integer.parseInt(portString);
        } catch (ClassCastException e) {
            state.printErrorMessage(NOT_STARTED + ": bad port number" + portString);
            return false;
        }
        return true;
    }

    public void startHttp() {
        if (server != null) {
            state.printUserMessage(NOT_STARTED + ": already started");
        }

        server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        for (ServletCommand command : servletCommands) {
            context.addServlet(new ServletHolder(command), command.getName());
        }
        server.setHandler(context);

        try {
            server.start();
        } catch (Exception e) {
            state.printErrorMessage(NOT_STARTED + ": " + server.getState());
            server = null;
            return;
        }

        state.printUserMessage("started at " + port);
    }

    public void stopHttp() {
        if (server == null) {
            state.printUserMessage(NOT_STARTED);
            return;
        }
        try {
            server.stop();
        } catch (Exception e) {
            state.printErrorMessage(NOT_STARTED + ": " + e);
            return;
        } finally {
            tables.clear();
        }
        state.printUserMessage("stopped at " + port);
        server = null;
    }

    public Integer startNewTransaction(String tableName) {
        if (server == null) {
            throw new IllegalStateException("server is not started");
        }
        boolean success = (tableProvider.getTable(tableName) != null);
        if (!success) {
            return null;
        }
        transactionKeeper.writeLock().lock();
        try {
            for (int i = 0; i < 100000; i++) {
                if (!tables.containsKey(i)) {
                    tables.put(i, tableName);
                    return i;
                }
            }
        } finally {
            transactionKeeper.writeLock().unlock();
        }
        throw new IllegalStateException("There's no more free transactions");
    }

    public StorableTable getTableByID(int id) {
        transactionKeeper.readLock().lock();
        try {
            String tableName = tables.get(id);
            if (tableName != null) {
                return tableProvider.getTable(tableName);
            }
            return null;
        } finally {
            transactionKeeper.readLock().unlock();
        }
    }

    public StorableDatabaseAdapter makeDatabaseAdapter(StorableTable table) {
        return new StorableDatabaseAdapter(tableProvider, table);
    }

    public void deleteTransaction(Integer id) {
        transactionKeeper.writeLock().lock();
        try {
        tables.remove(id);
        } finally {
            transactionKeeper.writeLock().unlock();
        }
    }

}
