package ru.fizteh.fivt.students.kocurba.multifilehashmap;

import ru.fizteh.fivt.storage.strings.TableProviderFactory;
import ru.fizteh.fivt.students.kocurba.filemap.command.*;
//import ru.fizteh.fivt.students.kocurba.multifilehashmap.command.CommandList;
import ru.fizteh.fivt.students.kocurba.multifilehashmap.command.CreateCommand;
import ru.fizteh.fivt.students.kocurba.multifilehashmap.command.DropCommand;
//import ru.fizteh.fivt.students.kocurba.multifilehashmap.command.UseCommand;
import ru.fizteh.fivt.students.kocurba.shell.Shell;
import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;
import ru.fizteh.fivt.students.kocurba.storage.strings.FileTableProviderFactory;

/**
 * Task 04 - MultiFileHashMap
 * 
 * @author Alina Kocurba
 *
 */
public class Main {

    public static void main(String[] args) {

        TableProviderFactory tableProviderFactory = new FileTableProviderFactory();
        StateWrap<NewState> state = new StateWrap<NewState>(new NewState(tableProviderFactory.create(System
                .getProperty("fizteh.db.dir")), null));
        Shell<NewState> shell = new Shell<NewState>(state);


        Command<State> putCommand = new PutCommand();
        Command<State> getCommand = new GetCommand();
        Command<State> removeCommand = new RemoveCommand();
        Command<State> exitCommand = new ExitCommand();
        Command<State> createCommand = new CreateCommand();
        Command<State> dropCommand = new DropCommand();
        //Command<State> useCommand = new UseCommand();

        Command[] commands = {putCommand, getCommand, removeCommand, createCommand, dropCommand, /*useCommand, */
                exitCommand};

        if (0 != args.length) {
            shell.batchMode(args, commands);
        } else {
            shell.interactiveMode(commands);
        }
        System.exit(0);
    }

}
