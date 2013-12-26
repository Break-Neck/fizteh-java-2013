package ru.fizteh.fivt.students.sergeymelikov.multifilemap.commands;

import java.io.IOException;

import ru.fizteh.fivt.students.sergeymelikov.multifilemap.MultiDbState;
import ru.fizteh.fivt.students.sergeymelikov.shell.AbstractCommand;

public class CommandRollBack extends AbstractCommand {
    private final MultiDbState state;
    
    public CommandRollBack(MultiDbState state) {
        super(0);
        this.state = state;
    }
    
    public String getName() {
        return "rollback";
    }
    
    public void execute(String[] args) throws IOException {
        System.out.println(state.rollBack());
    } 
}

