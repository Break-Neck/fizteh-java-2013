package ru.fizteh.fivt.students.lizaignatyeva.database.httpserver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BeginServlet extends HttpServlet {
    private Database database;

    public BeginServlet(Database database) {
        this.database = database;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tableName = req.getParameter("table");
        if (tableName == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No table name provided");
            return;
        }
        String transactionId;
        try {
            transactionId = database.generateTransactionName(tableName);
            if (transactionId == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No transactions id left");
                return;
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Illegal table name");
            return;
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF8");

        resp.getWriter().println("tid=" + transactionId);
    }
}
