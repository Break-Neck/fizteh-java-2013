package ru.fizteh.fivt.students.dmitryIvanovsky.servletHolder;

import ru.fizteh.fivt.students.dmitryIvanovsky.fileMap.FileMap;
import ru.fizteh.fivt.students.dmitryIvanovsky.fileMap.FileMapProvider;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletBegin extends HttpServlet {
    FileMapProvider provider;

    public ServletBegin(FileMapProvider provider) {
        this.provider = provider;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
            String name = req.getParameter("table");
            if (name == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "table expected");
                return;
            }

            int transaction;
            try {
                transaction = provider.getPool().createNewTransaction(name);
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }

            FileMap table;
            try {
                table = (FileMap) provider.getTable(name);
                if (table == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "table isn't exist");
                    return;
                }
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");
            resp.getWriter().println("tid=" +  String.format("%05d", transaction));
        }
}
