package src.ru.fizteh.fivt.students.krivchansky.filemap;
import src.ru.fizteh.fivt.students.krivchansky.shell.*;

public class CommitCommand<State extends FileMapShellStateInterface> extends SomeCommand<State> {
    
    public String getCommandName() {
        return "commit";
    }

    public int getArgumentQuantity() {
        return 0;
    }

    public void implement(String args, State state)
            throws SomethingIsWrongException {
        if (state.getTable() == null) {
            throw new SomethingIsWrongException ("no table");
        }
        System.out.println(state.commit());
    }

}
