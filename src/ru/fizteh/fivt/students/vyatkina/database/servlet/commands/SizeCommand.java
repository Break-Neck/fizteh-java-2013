package ru.fizteh.fivt.students.vyatkina.database.servlet.commands;


import ru.fizteh.fivt.students.vyatkina.database.StorableTable;
import ru.fizteh.fivt.students.vyatkina.database.servlet.TransactionManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SizeCommand extends ServletCommand {
    private static final String NAME = "/size";

    public SizeCommand(TransactionManager manager) {
        super(manager);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StorableTable table = getTable(req, resp);
        if (table == null) {
            return;
        }
        int size;
        try {
            table.useTransantion(transactionID);
            size = table.size();
        } catch (IllegalStateException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            if (table.isClosed()) {
                manager.deleteTransaction(transactionID);
            }
            return;
        } finally {
            table.retrieveThreadTable();
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF8");
        resp.getWriter().println(size);
    }
}
