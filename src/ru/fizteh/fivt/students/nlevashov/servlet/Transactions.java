package ru.fizteh.fivt.students.nlevashov.servlet;

import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.storage.structured.TableProviderFactory;
import ru.fizteh.fivt.students.nlevashov.factory.MyTableProviderFactory;
import ru.fizteh.fivt.students.nlevashov.shell.Shell;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

public class Transactions {
    HashMap<Integer, Table> tids;
    TableProvider provider;
    Integer counter;

    Transactions() throws IOException {
        String addr = System.getProperty("fizteh.db.dir");
        if (addr == null) {
            System.err.println("Property \"fizteh.db.dir\" wasn't set");
            System.exit(1);
        }
        Path addrPath = Shell.makePath(addr).toPath();
        TableProviderFactory factory = new MyTableProviderFactory();
        provider = factory.create(addrPath.toString());

        tids = new HashMap<>();
        counter = 0;
    }

    Integer getTid(String tableName) {
        Table t = provider.getTable(tableName);
        if (t == null) return null;
        counter++;
        tids.put(counter, t);
        return counter;
    }

    Table getTable(Integer tid) {
        return tids.get(tid);
    }

    TableProvider getProvider() {
        return provider;
    }

}
