package ru.fizteh.fivt.students.vyatkina.database.servlet.commands;

import ru.fizteh.fivt.students.vyatkina.WrappedIOException;
import ru.fizteh.fivt.students.vyatkina.database.StorableTable;
import ru.fizteh.fivt.students.vyatkina.database.servlet.TransactionManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GetCommand extends ServletCommand {

    private static final String NAME  = "/get";

    public GetCommand(TransactionManager manager) {
        super(manager);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String key = getValue("key", req, resp);
        if (key == null) {
            return;
        }
        StorableTable table = getTable(req, resp);
        if (table == null) {
            return;
        }
        String value;
        try {
            table.useTransantion(transactionID);
            value = manager.makeDatabaseAdapter(table).get(key);
        } catch (WrappedIOException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        } catch (IllegalStateException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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

    @Override
    public String getName() {
        return NAME;
    }
}
