package ru.fizteh.fivt.students.dmitryKonturov.dataBase.servlet;


import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableProviderFactoryImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableProviderImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TransactionPool;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.shellEnvironment.WorkWithChosenTableCommands;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.shellEnvironment.WorkWithTableProviderCommands;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellEmulator;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellException;

import java.io.IOException;
import java.nio.file.Path;

public class ServletShell extends ShellEmulator {
    private static ServletShellInfo getServletShellInfo(Path workspace) throws IOException {
        TableProviderFactoryImplementation providerFactory = new TableProviderFactoryImplementation();
        TableProviderImplementation tableProvider = (TableProviderImplementation) providerFactory
                .create(workspace.toString(), 1);
        return new ServletShellInfo(tableProvider);
    }

    public ServletShell(Path workspace) throws IOException {
        super(getServletShellInfo(workspace));
        super.addToCommandList(WorkWithChosenTableCommands.getPackageCommands());
        super.addToCommandList(WorkWithTableProviderCommands.getPackageCommands());
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


    @Override
    protected String[] shellParseArguments(String bigArg) {
        String newBigArg = bigArg.trim();
        String[] args;
        if (newBigArg.length() == 0) {
            args = new String[0];
        } else {
            args = new String[1];
            args[0] = newBigArg;
        }
        return args;
    }

    @Override
    public void packageMode(String query) throws ShellException {
        super.packageMode(query + " exit");
    }
}
