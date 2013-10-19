import ru.fizteh.fivt.students.krivchansky.shell;
package ru.fizteh.fivt.students.krivchansky.filemap;

public class FileMapMain {
    public void main() {
        Commands[] com = {new ExitCommand(), new RollbackCommand(), new CommitCommand(), 
                new PutCommand(), new GetCommand(), new RemoveKeyCommand() };
        Shell shell = new Shell(com);
        Shell.ShellState state = shell.new ShellState(System.getProperty("user.dir"));
        SingleFileTable temp = new SingleFileTable(state.getCurDir(), SingleFileTable.DATABASENAME);
        state.setTable(temp);
        shell.consoleWay(state);
    }
}