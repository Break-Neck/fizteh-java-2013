package ru.fizteh.fivt.students.zinnatullin.multifilehashmap;

import java.io.IOException;

import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicDataBaseState;
import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicTable;
import ru.fizteh.fivt.students.zinnatullin.shell.Shell;

public class Create<ElementType, TableType extends BasicTable<ElementType>> extends BasicMultiFileHashMapCommand<ElementType, TableType> {
	public Create(BasicDataBaseState<ElementType, TableType> dataBaseState) {
		super(dataBaseState, "create", 1);
	}
	public void executeCommand(String[] arguments, Shell usedShell) throws IOException {    
		dataBaseState.create(arguments[0]);
		usedShell.curShell.getOutStream().println("created");
	}	
}
