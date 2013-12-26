package ru.fizteh.fivt.students.lizaignatyeva.database.httpserver;

import ru.fizteh.fivt.storage.structured.Storeable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PutServlet extends HttpServlet {
    private Database database;

    public PutServlet(Database database) {
        this.database = database;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String transactionId = req.getParameter("tid");
        if (transactionId == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No tid provided");
            return;
        }
        String key = req.getParameter("key");
        if (key == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No key provided");
            return;
        }
        String value = req.getParameter("value");
        if (value == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No value provided");
            return;
        }
        if (!Database.isValidTransactionName(transactionId)) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Incorrect tid format");
            return;
        }
        Database.Transaction transaction = database.getTransaction(transactionId);
        if (transaction == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No such transaction");
            return;
        }
        transaction.start();
        try {
            Storeable realValue;
            try {
                realValue = database.tableProvider.deserialize(transaction.table, value);
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Incorrect value provided");
                return;
            }
            Storeable oldValue;
            try {
                oldValue = transaction.table.put(key, realValue);
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to put value: " + e.getMessage());
                return;
            }
            if (oldValue == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Old value was null");
                return;
            }
            String result;
            try {
                result = database.tableProvider.serialize(transaction.table, oldValue);
            } catch (Exception e) {
                resp.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to deserialize: " + e.getMessage());
                return;
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");
            resp.getWriter().println(result);
        } finally {
            transaction.end();
        }
    }
}
