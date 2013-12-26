package ru.fizteh.fivt.students.dmitryKonturov.dataBase.shellEnvironment;

import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellInfo;


public class StoreableShellInfo extends ShellInfo {

    public StoreableShellInfo(TableProvider provider) {
        super();
        super.setProperty("TableProvider", provider);
        super.setProperty("CurrentTable", null);
    }
}
