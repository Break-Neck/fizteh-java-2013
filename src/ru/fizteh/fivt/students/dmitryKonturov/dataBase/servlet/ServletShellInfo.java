package ru.fizteh.fivt.students.dmitryKonturov.dataBase.servlet;

import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableProviderImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.shellEnvironment.StoreableShellInfo;

public class ServletShellInfo extends StoreableShellInfo {
    ServletShellInfo(TableProviderImplementation provider) {
        super(provider);
        super.setProperty("currentPort", -1);
        super.setProperty("standardPort", 10001);
        super.setProperty("server", null);
        super.setProperty("provider", provider);
    }
}
