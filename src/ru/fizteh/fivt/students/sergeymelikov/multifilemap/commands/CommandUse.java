package ru.fizteh.fivt.students.sergeymelikov.multifilemap.commands;

import java.io.IOException;

import ru.fizteh.fivt.students.sergeymelikov.multifilemap.MultiDbState;
import ru.fizteh.fivt.students.sergeymelikov.shell.AbstractCommand;

public class CommandUse extends AbstractCommand{
    private MultiDbState state;

    public CommandUse(MultiDbState st) {
        super(1);
        state = st;
    }
    
    public String getName() {
        return "use";
    }
    
    public void execute(String[] args) throws IOException {    
        try {
            state.use(args[1]);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
        state.getOutputStream().println("using " + args[1]);        
    }
}
