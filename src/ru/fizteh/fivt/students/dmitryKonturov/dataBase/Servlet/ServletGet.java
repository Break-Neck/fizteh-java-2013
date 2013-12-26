package ru.fizteh.fivt.students.dmitryKonturov.dataBase.Servlet;

import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableProviderImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellEmulator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletGet extends HttpServlet {
    TableProviderImplementation provider;

    public ServletGet(TableProviderImplementation provider) {
        this.provider = provider;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {


        String tidString = request.getParameter("tid");
        if (tidString == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "no tid as parameter");
            return;
        }

        int transactionId;
        try {
            transactionId = ServletShell.parseTransactionId(tidString);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid parameter is wrong");
            return;
        }

        if (!provider.getTransactionPool().transactionExists(transactionId)) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "tid not exists");
            return;
        }

        String key = request.getParameter("key");
        if (key == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "no key as parametr");
            return;
        }

        String value;
        try {
            String tableName = provider.getTransactionPool().getTableName(transactionId);
            TableImplementation table = (TableImplementation) provider.getTable(tableName);
            Storeable storeable = table.get(key, transactionId);
            if (storeable == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "key not found");
                return;
            }
            value = provider.serialize(table, storeable);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ShellEmulator.getNiceMessage(e));
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF8");
        response.getWriter().println(value);
    }
}
