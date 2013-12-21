package ru.fizteh.fivt.students.dmitryIvanovsky.ServletHolder;

import ru.fizteh.fivt.students.dmitryIvanovsky.fileMap.FileMap;
import ru.fizteh.fivt.students.dmitryIvanovsky.fileMap.FileMapProvider;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletCommit extends HttpServlet {
    FileMapProvider provider;

    public ServletCommit(FileMapProvider provider) {
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
                transaction = Integer.parseInt(name);
            } catch (Exception e) {
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
                res = table.commit(transaction);
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
