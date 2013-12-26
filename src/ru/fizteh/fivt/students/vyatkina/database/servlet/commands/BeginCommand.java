package ru.fizteh.fivt.students.vyatkina.database.servlet.commands;


import ru.fizteh.fivt.students.vyatkina.database.servlet.TransactionManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BeginCommand extends ServletCommand {
    private static final String NAME  = "/begin";

    public BeginCommand(TransactionManager manager) {
        super(manager);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String tableName = getValue("table", req, resp);
        if (tableName == null) {
            //resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Expected tableName, but null was found");
            return;
        }
        try {
           transactionID = manager.startNewTransaction(tableName);
        } catch (IllegalStateException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }
        if (transactionID == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Table doesn't exists");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF8");
        resp.getWriter().println("tid=" + convertId(transactionID));
    }

    String convertId(int id) {
        char[] str = new char[5];
        for (int i = 4; i >= 0; i--) {
            str[i] = (char) ('0' + id % 10);
            id /= 10;
        }
       return new String(str);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
