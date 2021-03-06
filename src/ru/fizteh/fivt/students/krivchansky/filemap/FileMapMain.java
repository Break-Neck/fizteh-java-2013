package ru.fizteh.fivt.students.krivchansky.filemap;
import java.util.HashSet;
import java.util.Set;

import ru.fizteh.fivt.students.krivchansky.shell.Commands;
import ru.fizteh.fivt.students.krivchansky.shell.Shell;

public class FileMapMain {
    public static void main(String[] args) {
    	FileMapShellState state = new FileMapShellState();
    	Set<Commands<?>> com =  new HashSet<Commands<?>>() {{ add(new ExitCommand());
    			add(new RollbackCommand<FileMapShellState>()); add(new CommitCommand<FileMapShellState>()); 
                add(new PutCommand<MyTable, String, String, FileMapShellState>());
                add(new GetCommand<MyTable, String, String, FileMapShellState>()); 
                add(new RemoveKeyCommand<MyTable, String, String, FileMapShellState>());}};
        Shell<FileMapShellState> shell = new Shell<FileMapShellState>(com);
        String dbDirectory = System.getProperty("fizteh.db.dir");
        state.table = new SingleFileTable(dbDirectory, "master");
        state.table.setAutoCommit(true);
        shell.setShellState(state);
        shell.consoleWay(state);
    }
}