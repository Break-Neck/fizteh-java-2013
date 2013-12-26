package ru.fizteh.fivt.students.sergeymelikov.multifilemap.commands;

import java.io.IOException;

import ru.fizteh.fivt.students.sergeymelikov.multifilemap.MultiDbState;
import ru.fizteh.fivt.students.sergeymelikov.shell.AbstractCommand;

public class CommandDrop extends AbstractCommand {
    
    private MultiDbState state;
    
    public CommandDrop(MultiDbState st) {
        super(1);
        state = st;
    }
    
    public String getName() {
        return "drop";
    }
    
    public void execute(String[] args) throws IOException {
        try {
            state.drop(args[1]);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }
        state.getOutputStream().println("dropped");
    }
}
