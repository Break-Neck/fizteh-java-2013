package src.ru.fizteh.fivt.students.krivchansky.shell;

public interface Commands<State> {

    public String getCommandName();
    
    public int getArgumentQuantity();
    
    abstract public void implement(String args, State state) throws SomethingIsWrongException;
}
