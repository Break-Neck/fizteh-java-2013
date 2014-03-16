package ru.fizteh.fivt.students.kocurba.storeable;

import ru.fizteh.fivt.storage.structured.TableProviderFactory;
import ru.fizteh.fivt.students.kocurba.JUnit.NewUseCommand;
import ru.fizteh.fivt.students.kocurba.filemap.command.*;


import ru.fizteh.fivt.students.kocurba.multifilehashmap.command.CreateCommand;
import ru.fizteh.fivt.students.kocurba.multifilehashmap.command.DropCommand;
import ru.fizteh.fivt.students.kocurba.shell.Shell;
import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;

import java.io.IOException;

public class Main {

    public static void main(String[] args)  {

        TableProviderFactory tableProviderFactory = new StoreableTableProviderFactory();
        try {


            StateWrap<State> state = new StateWrap<State>(new State(tableProviderFactory.create(System
                    .getProperty("fizteh.db.dir")), null));


            Shell<State> shell = new Shell<State>(state);


            Command<State> putCommand = new PutCommand();
            Command<State> getCommand = new GetCommand();
            Command<State> removeCommand = new RemoveCommand();
            Command<State> exitCommand = new ExitCommand();
            Command<State> createCommand = new CreateCommand();
            Command<State> dropCommand = new DropCommand();
            Command<State> newUseCommand = new NewUseCommand();
            Command<State> commitCommand = new CommitCommand();
            Command<State> rollbackCommand = new RollbackCommand();
            Command<State> sizeCommand = new SizeCommand();


            Command[] commands = {putCommand, getCommand, removeCommand, createCommand, dropCommand, newUseCommand,
                    exitCommand, commitCommand, rollbackCommand, sizeCommand};

            if (0 != args.length) {
                shell.batchMode(args, commands);
            } else {
                shell.interactiveMode(commands);
            }
        } catch (IllegalArgumentException e) {
            System.exit(1);
        } catch (IOException e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
