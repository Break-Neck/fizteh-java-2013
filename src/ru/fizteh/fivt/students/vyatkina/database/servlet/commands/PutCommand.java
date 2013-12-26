package ru.fizteh.fivt.students.vyatkina.database.servlet.commands;


import ru.fizteh.fivt.students.vyatkina.WrappedIOException;
import ru.fizteh.fivt.students.vyatkina.database.StorableTable;
import ru.fizteh.fivt.students.vyatkina.database.servlet.TransactionManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PutCommand extends ServletCommand {

    private static final String NAME = "/put";

    public PutCommand(TransactionManager manager) {
        super(manager);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String key = getValue("key", req, resp);
        if (key == null) {
            return;
        }
        String value = getValue("value", req, resp);
        if (value == null) {
            return;
        }
        StorableTable table = getTable(req, resp);
        if (table == null) {
            return;
        }
        try {
            table.useTransantion(transactionID);
            value = manager.makeDatabaseAdapter(table).put(key, value);
        } catch (WrappedIOException | IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        } catch (IllegalStateException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            if (table.isClosed()) {
                manager.deleteTransaction(transactionID);
            }
            return;
        } finally {
            table.retrieveThreadTable();
        }

        if (value == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, key + ": no such key");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF8");
        resp.getWriter().println(value);
    }
}
