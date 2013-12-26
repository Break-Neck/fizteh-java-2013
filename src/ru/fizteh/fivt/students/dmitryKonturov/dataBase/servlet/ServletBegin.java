package ru.fizteh.fivt.students.dmitryKonturov.dataBase.servlet;


import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableProviderImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellEmulator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletBegin extends HttpServlet {
    private TableProviderImplementation provider;

    public ServletBegin(TableProviderImplementation provider) {
        this.provider = provider;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String tableName = request.getParameter("table");
        if (tableName == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "no table as parameter");
            return;
        }

        int transactionId;
        try {
            transactionId = provider.getTransactionPool().createTransaction(tableName);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ShellEmulator.getNiceMessage(e));
            return;
        }

        TableImplementation table;
        try {
            table = (TableImplementation) provider.getTable(tableName);
            if (table == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "table not exists");
                return;
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ShellEmulator.getNiceMessage(e));
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF8");
        response.getWriter().println("tid=" +  String.format("%05d", transactionId));
    }
}
