package ru.fizteh.fivt.students.dmitryIvanovsky.servletHolder;

import ru.fizteh.fivt.students.dmitryIvanovsky.fileMap.FileMap;
import ru.fizteh.fivt.students.dmitryIvanovsky.fileMap.FileMapProvider;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static ru.fizteh.fivt.students.dmitryIvanovsky.servletHolder.CommonServletFunction.checkTid;

public class ServletRollback extends HttpServlet {
    FileMapProvider provider;

    public ServletRollback(FileMapProvider provider) {
        this.provider = provider;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
            String name = req.getParameter("tid");
            if (name == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid expected");
                return;
            }

            int transaction;
            try {
                transaction = checkTid(name);
            } catch (IllegalStateException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "wrong tid");
                return;
            }

            if (!provider.getPool().isExistTransaction(transaction)) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid isn't exist");
                return;
            }

            int res;
            try {
                FileMap table = (FileMap) provider.getTable(provider.getPool().getNameTable(transaction));
                res = table.rollback(transaction, true);
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");
            resp.getWriter().println("diff=" +  String.format("%d", res));
        }
}
