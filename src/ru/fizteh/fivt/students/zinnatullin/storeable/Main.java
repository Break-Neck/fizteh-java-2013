package ru.fizteh.fivt.students.zinnatullin.storeable;

import java.io.IOException;

import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.zinnatullin.filemap.Get;
import ru.fizteh.fivt.students.zinnatullin.filemap.Put;
import ru.fizteh.fivt.students.zinnatullin.filemap.Remove;
import ru.fizteh.fivt.students.zinnatullin.junit.Commit;
import ru.fizteh.fivt.students.zinnatullin.junit.Rollback;
import ru.fizteh.fivt.students.zinnatullin.junit.Size;
import ru.fizteh.fivt.students.zinnatullin.multifilehashmap.Drop;
import ru.fizteh.fivt.students.zinnatullin.multifilehashmap.Use;
import ru.fizteh.fivt.students.zinnatullin.shell.Exit;
import ru.fizteh.fivt.students.zinnatullin.shell.Shell;

public class Main {
	public static void main(String[] arguments) {
		StoreableTableProvider provider = null;
		try {
			provider = (new StoreableTableProviderFactory()).create(System.getProperty("fizteh.db.dir"));
		} catch (IllegalArgumentException catchedException) {
			System.err.println(catchedException.getMessage());
			System.exit(1);
		} catch (IOException catchedException) {
			System.err.println(catchedException.getMessage());
			System.exit(1);
		}
		
		StoreableState state = new StoreableState(provider);
		
		Shell usedShell = new Shell(System.getProperty("user.dir"), System.in, System.out);
		usedShell.curShell.loadCommand(new Get(state));
		usedShell.curShell.loadCommand(new Put(state));
		usedShell.curShell.loadCommand(new Remove(state));
		usedShell.curShell.loadCommand(new Create(state));
		usedShell.curShell.loadCommand(new Drop<Storeable, StoreableTable>(state));
		usedShell.curShell.loadCommand(new Use<Storeable, StoreableTable>(state));
		usedShell.curShell.loadCommand(new Commit<Storeable, StoreableTable>(state));
		usedShell.curShell.loadCommand(new Size<Storeable, StoreableTable>(state));
		usedShell.curShell.loadCommand(new Rollback<Storeable, StoreableTable>(state));
		usedShell.curShell.loadCommand(new Exit());
		
		try {
			provider.readFilesMaps();
			usedShell.startShell(arguments);
			provider.writeFilesMaps();
		} catch (IOException catchedException) {
			System.err.println(catchedException.getMessage());
			System.exit(1);
		}
	}
}
