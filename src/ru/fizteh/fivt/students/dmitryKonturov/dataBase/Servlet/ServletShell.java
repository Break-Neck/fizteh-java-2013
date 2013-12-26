package ru.fizteh.fivt.students.dmitryKonturov.dataBase.Servlet;


import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableProviderFactoryImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableProviderImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TransactionPool;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellEmulator;

import java.io.IOException;
import java.nio.file.Path;

public class ServletShell extends ShellEmulator {
    private static ServletShellInfo getServletShellInfo(Path workspace) throws IOException {
        TableProviderFactoryImplementation providerFactory = new TableProviderFactoryImplementation();
        TableProviderImplementation tableProvider = (TableProviderImplementation) providerFactory
                .create(workspace.toString());
        return new ServletShellInfo(tableProvider);
    }

    public ServletShell(Path workspace) throws IOException {
        super(getServletShellInfo(workspace));
        super.addToCommandList(ServletShellCommands.getPackageCommands());
    }

    public static int parseTransactionId(String tidString) {
        int maxId = TransactionPool.getMaxTransactionId() - 1;
        int len = 0;
        while (maxId > 0) {
            maxId /= 10;
            ++len;
        }

        if (tidString.length() != len) {
            throw new NumberFormatException();
        }
        int toReturn = Integer.parseInt(tidString);
        if (toReturn < 0) {
            throw new NumberFormatException();
        }
        return toReturn;
    }
}
