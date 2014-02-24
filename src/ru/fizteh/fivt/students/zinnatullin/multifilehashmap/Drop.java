package ru.fizteh.fivt.students.zinnatullin.multifilehashmap;

import java.io.IOException;

import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicDataBaseState;
import ru.fizteh.fivt.students.zinnatullin.basicclasses.BasicTable;
import ru.fizteh.fivt.students.zinnatullin.shell.Shell;

public class Drop<ElementType, TableType extends BasicTable<ElementType>> extends BasicMultiFileHashMapCommand<ElementType, TableType> {
	public Drop(BasicDataBaseState<ElementType, TableType> dataBaseState) {
		super(dataBaseState, "drop", 1);
	}
	public void executeCommand(String[] arguments, Shell usedShell) throws IOException {  
		dataBaseState.drop(arguments[0]);
		usedShell.curShell.getOutStream().println("dropped");
	}
}
