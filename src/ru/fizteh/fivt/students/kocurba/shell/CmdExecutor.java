package ru.fizteh.fivt.students.kocurba.shell;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ru.fizteh.fivt.students.kocurba.shell.command.Command;

public class CmdExecutor<T> {

    Map<String, Command<T>> commandHashMap = new HashMap<String, Command<T>>();

    public CmdExecutor(Command<T>[] commands) {

        for (Command<T> command : commands) {
            commandHashMap.put(command.getCommandName(), command);
        }

    }

    public void execute(StateWrap<T> state, String[] arguments) throws IOException {
        if (commandHashMap.get(arguments[0]) == null) {
            throw new IOException(arguments[0] + " incorrect command.");
        }
        if (arguments.length != (commandHashMap.get(arguments[0]).getArgCount() + 1)
                && commandHashMap.get(arguments[0]).getArgCount() != -1) {
            throw new IOException(commandHashMap.get(arguments[0]).getCommandName() + ": incorrect argument count.");
        }
        commandHashMap.get(arguments[0]).executeCommand(state, arguments);
    }
    }
