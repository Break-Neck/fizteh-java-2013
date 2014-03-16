package ru.fizteh.fivt.students.kocurba.filemap;

import java.io.IOException;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.students.kocurba.filemap.command.*;
import ru.fizteh.fivt.students.kocurba.shell.Shell;
import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;
import ru.fizteh.fivt.students.kocurba.storage.strings.FileTable;

/**
 * Task 03 - FileMap
 * 
 * @author Alina Kocurba
 * 
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Table dataBase = new FileTable("db", System.getProperty("fizteh.db.dir") + "/db.dat");
        StateWrap<NewState> state = new StateWrap<NewState>(new NewState(null, dataBase));
        Shell<NewState> shell = new Shell<NewState>(state);

        Command<State> putCommand = new PutCommand();
        Command<State> getCommand = new GetCommand();
        Command<State> removeCommand = new RemoveCommand();
        Command<State> exitCommand = new ExitCommand();

        Command[] commands = {putCommand, getCommand, removeCommand, exitCommand};


        if (0 != args.length) {
            shell.batchMode(args, commands);
        } else {
            shell.interactiveMode(commands);
        }
        System.exit(0);
    }

}
