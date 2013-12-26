package ru.fizteh.fivt.students.dzvonarev.filemap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletBegin extends HttpServlet {

    private TransactionManager manager;

    public ServletBegin(TransactionManager transactionManager) {
        manager = transactionManager;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("table");
        if (name == null) {
            String text = "no table";
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, text);
            return;
        } // run transaction
        String transactionId = manager.startTransaction(name);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF8");
        response.getWriter().println("tid=" + transactionId);
    }

}
