import ru.fizteh.fivt.students.krivchansky.shell;
package ru.fizteh.fivt.students.krivchansky.filemap;


public class RemoveKeyCommand implements Commands {

    public String getCommandName() {
        return "remove";
    }

    public int getArgumentQuantity() {
        return 1;
    }

    public void implement(String[] args, Shell.ShellState state)
            throws SomethingIsWrongException {
        String a = state.table.remove(args[0]);
        if (a.isEmpty()) {
            System.out.println("not found");
        } else {
            System.out.println("found\n" + a);
        }
    }

}
