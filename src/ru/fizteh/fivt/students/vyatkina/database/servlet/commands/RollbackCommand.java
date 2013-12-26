package ru.fizteh.fivt.students.vyatkina.database.servlet.commands;


import ru.fizteh.fivt.students.vyatkina.database.StorableTable;
import ru.fizteh.fivt.students.vyatkina.database.servlet.TransactionManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RollbackCommand extends ServletCommand {

    private static final String NAME = "/rollback";

    public RollbackCommand(TransactionManager manager) {
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
        int diff;
        try {
            table.useTransantion(transactionID);
            diff = table.rollback();
            table.removeTransaction(transactionID);

        } catch (IllegalStateException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        } finally {
            manager.deleteTransaction(transactionID);
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF8");
        resp.getWriter().println("diff=" + diff);
    }
}
