package ru.fizteh.fivt.students.krivchansky.filemap;
import java.util.HashSet;
import java.util.Set;

import ru.fizteh.fivt.students.krivchansky.shell.*;

public class FileMapMain {
    public static void main(String[] args) {
    	FileMapShellState state = new FileMapShellState();
    	Set<Commands> com =  new HashSet<Commands>() {{ add(new ExitCommand()); add(new RollbackCommand()); add(new CommitCommand()); 
                add(new PutCommand()); add(new GetCommand()); add(new RemoveKeyCommand());}};
        Shell<FileMapShellState> shell = new Shell<FileMapShellState>(com);
        String dbDirectory = System.getProperty("fizteh.db.dir");
        state.table = new SingleFileTable(dbDirectory, "master");
        shell.setShellState(state);
        shell.consoleWay(state);
    }
}