package ru.fizteh.fivt.students.belousova.filemap;

import ru.fizteh.fivt.students.belousova.shell.Command;

import java.io.IOException;

public class CommandRemove implements Command {
    private TableState state;

    public CommandRemove(TableState state) {
        this.state = state;
    }

    @Override
    public int getArgCount() {
        return 1;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public void execute(String[] args) throws IOException {
        if (state.getCurrentTable() == null) {
            System.out.println("no table");
        } else {
            String key = args[1];
            String value = state.removeFromCurrentTable(key);
            if (value == null) {
                System.out.println("not found");
            } else {
                System.out.println("removed");
            }
        }
    }
}
