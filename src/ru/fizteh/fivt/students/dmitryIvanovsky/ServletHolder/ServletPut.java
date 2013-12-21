package ru.fizteh.fivt.students.dmitryIvanovsky.ServletHolder;

import ru.fizteh.fivt.students.dmitryIvanovsky.fileMap.FileMap;
import ru.fizteh.fivt.students.dmitryIvanovsky.fileMap.FileMapProvider;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletPut extends HttpServlet {
    FileMapProvider provider;

    public ServletPut(FileMapProvider provider) {
        this.provider = provider;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
            String tid = req.getParameter("tid");
            String key = req.getParameter("key");
            String value = req.getParameter("value");

            if (tid == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid expected");
                return;
            }
            if (key == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "key expected");
                return;
            }
            if (value == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "value expected");
                return;
            }

            int transaction;
            try {
                transaction = Integer.parseInt(tid);
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "wrong tid");
                return;
            }

            if (!provider.getPool().isExistTransaction(transaction)) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid isn't exist");
                return;
            }

            String res;
            try {
                FileMap table = (FileMap) provider.getTable(provider.getPool().getNameTable(transaction));
                res = provider.serialize(table, table.put(key, provider.deserialize(table, value), transaction));
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF8");
            resp.getWriter().println(res);
        }
}