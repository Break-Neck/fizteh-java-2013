import ru.fizteh.fivt.students.krivchansky.shell;
package ru.fizteh.fivt.students.krivchansky.filemap;


public class PutCommand implements Commands {

    public String getCommandName() {
        return "put";
    }

    public int getArgumentQuantity() {
        return 2;
    }

    public void implement(String[] args, Shell.ShellState state)
            throws SomethingIsWrongException {
        String temp = state.table.put(args[0], args[1]);
        if (temp != null) {
                System.out.println("overwrite\n" + temp);
        } else {
            System.out.println("new");
        }
    }

}
