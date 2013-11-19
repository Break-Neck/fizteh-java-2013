package ru.fizteh.fivt.students.krivchansky.shell;

public class WhereAreWeCommand implements Commands<ShellState> {
    
    public String getCommandName() {
        return "pwd";
    }

    public int getArgumentQuantity() {
        return 0;
    }
    
    public void implement(String args, ShellState state) {
        System.out.println(state.getCurDir());
    }
}
