package ru.fizteh.fivt.students.dmitryKonturov.dataBase.Servlet;

import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableProviderImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellInfo;

public class ServletShellInfo extends ShellInfo {
    ServletShellInfo(TableProviderImplementation provider) {
        super();
        super.setProperty("currentPort", -1);
        super.setProperty("standardPort", 10001);
        super.setProperty("server", null);
        super.setProperty("provider", provider);
    }
}
