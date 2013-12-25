package ru.fizteh.fivt.students.vyatkina.database.servlet.commands;

import ru.fizteh.fivt.students.vyatkina.database.StorableTable;
import ru.fizteh.fivt.students.vyatkina.database.servlet.TransactionManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class ServletCommand extends HttpServlet {
    protected TransactionManager manager;
    protected Integer transactionID;

    public ServletCommand(TransactionManager manager) {
      this.manager = manager;
    }

    public abstract String getName();

    protected String getValue(String name, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String value = req.getParameter(name);
        if (value == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, name + " expected");
        }
        return value;
    }

    protected StorableTable getTable(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = getValue("tid", req, resp);
        if (id == null) {
            return null;
        }
        if (!id.matches("[0-9]{5}")) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, id + ": invalid tid format");
            return null;
        }
        transactionID = Integer.parseInt(id);
        StorableTable table = manager.getTableByID(transactionID);
        if (table == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "This transaction:" + transactionID
                    + " doesn't appeal to any table");
            return null;
        }
        return table;
    }
}
