package ru.fizteh.fivt.students.dmitryKonturov.dataBase.Servlet;

import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableProviderImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellEmulator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletCommit extends HttpServlet {
    TableProviderImplementation provider;

    public ServletCommit(TableProviderImplementation provider) {
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

        int changesCount;
        try {
            String tableName = provider.getTransactionPool().getTableName(transactionId);
            TableImplementation table = (TableImplementation) provider.getTable(tableName);
            changesCount = table.commit(transactionId, true);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ShellEmulator.getNiceMessage(e));
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF8");
        response.getWriter().println("diff=" + changesCount);
    }
}
