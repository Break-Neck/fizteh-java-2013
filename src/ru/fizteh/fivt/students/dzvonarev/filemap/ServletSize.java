package ru.fizteh.fivt.students.dzvonarev.filemap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletSize extends HttpServlet {

    private TransactionManager manager;

    public ServletSize(TransactionManager transactionManager) {
        manager = transactionManager;
    }

    private boolean isValid(String id) {
        if (id.length() >= 6) {
            return false;
        }
        for (int i = 0; i < id.length(); ++i) {
            if (!(id.charAt(i) >= '0' && id.charAt(i) <= '9')) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String transactionId = request.getParameter("tid");
        if (transactionId == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "transaction id not found");
            return;
        }
        if (!isValid(transactionId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid transaction id");
        }
        Transaction transaction = manager.getTransaction(transactionId);
        if (transaction == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "no transaction is found");
            return;
        } // run transaction
        int size = transaction.size();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF8");
        response.getWriter().println(size);
    }

}
