package ru.fizteh.fivt.students.dubovpavel.filemap;

import ru.fizteh.fivt.students.dubovpavel.filemap.Performers.*;

import java.io.PrintStream;
import java.util.ArrayList;

public class Dispatcher {
    private Parser parser;
    private int invalidSequences, invalidOperations;
    private ArrayList<Performer> performers;
    private boolean forwarding;
    private boolean shutdown;
    private final String dbPathProperty = "fizteh.db.dir";
    private DataBase dataBase;

    public class DispatcherException extends Exception {
        public DispatcherException(String msg) {
            super(msg);
        }
    }

    public enum MessageType {
        SUCCESS,
        WARNING,
        ERROR
    }

    public Dispatcher(boolean forwarding) {
        invalidSequences = 0;
        parser = new Parser();
        performers = new ArrayList<Performer>();
        performers.add(new PerformerPut());
        performers.add(new PerformerGet());
        performers.add(new PerformerRemove());
        performers.add(new PerformerExit());
        performers.add(new PerformerHalt());
        this.forwarding = forwarding;
        String path = System.getProperty(dbPathProperty);
        if(path == null) {
            callbackWriter(MessageType.ERROR, String.format("'%s' property is null", dbPathProperty));
            shutdown = true;
        } else {
            shutdown = false;
            dataBase = new DataBase(path, this);
        }
    }

    public String callbackWriter(MessageType type, String msg) {
        PrintStream stream = null;
        if(type == MessageType.ERROR) {
            stream = System.err;
        } else if(type == MessageType.SUCCESS || type == MessageType.WARNING) {
            stream = System.out;
        }
        stream.println(msg);
        return msg;
    }

    public void shutDown() {
        shutdown = true;
    }

    public boolean online() {
        return !shutdown;
    }

    public DataBaseHandler<String, String> getDataBase() {
        return dataBase;
    }

    public void sortOut(String commandSequence) throws DispatcherException {
        try {
            ArrayList<Command> commands = parser.getCommands(this, commandSequence);
            for(Command command: commands) {
                try {
                    boolean performed = false;
                    for(Performer performer: performers) {
                        if(performer.pertains(command)) {
                            performer.execute(this, command);
                            performed = true;
                            break;
                        }
                    }
                    if(!performed) {
                        callbackWriter(MessageType.ERROR, String.format("%s is not correct", command.getDescription()));
                    }
                } catch(Performer.PerformerException e) {
                    invalidOperations++;
                    if(forwarding) {
                        throw new DispatcherException(e.getMessage());
                    }
                }
                if(shutdown) {
                    break;
                }
            }
        } catch(Parser.IncorrectSyntaxException e) {
            invalidSequences++;
            if(forwarding) {
                throw new DispatcherException(e.getMessage());
            }
        }
    }
}
