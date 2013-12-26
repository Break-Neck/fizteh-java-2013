package ru.fizteh.fivt.students.lizaignatyeva.database.httpserver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SizeServlet extends HttpServlet {
    private Database database;

    public SizeServlet(Database database) {
        this.database = database;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String transactionId = req.getParameter("tid");
        if (transactionId == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No tid provided");
            return;
        }
        if (!Database.isValidTransactionName(transactionId)) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Incorrect tid format");
            return;
        }
        Database.Transaction transaction = database.getTransaction(transactionId);
        if (transaction == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No such tid");
            return;
        }
        transaction.start();
        try {
            int result;
            try {
                result = transaction.table.size();
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get size: " + e.getMessage());
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
