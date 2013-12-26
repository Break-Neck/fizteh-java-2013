package ru.fizteh.fivt.students.dmitryKonturov.dataBase.servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.dataBase.databaseImplementation.TableProviderImplementation;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellCommand;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellEmulator;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellException;
import ru.fizteh.fivt.students.dmitryKonturov.shell.ShellInfo;

public class ServletShellCommands {

    public static ShellCommand[] getPackageCommands() {
        return new ShellCommand[] {
                new StartCommand(),
                new StopCommand(),
                new ExitCommand()
        };
    }

    static class ExitCommand implements ShellCommand {
        @Override
        public String getName() {
            return "exit";
        }

        @Override
        public void execute(String[] args, ShellInfo info) throws ShellException {
            if (args.length != 0) {
                throw new ShellException(getName(), "Bad arguments");
            }
            TableImplementation table = (TableImplementation) info.getProperty("CurrentTable");
            if (table != null) {
                try {
                    int unsavedChanges = table.getUnsavedChangesCount(table.getLocalTransactionId());
                    if (unsavedChanges > 0) {
                        table.commit();
                    }
                } catch (Exception e) {
                    throw new ShellException(getName(), e);
                }
            }
            System.exit(0);
        }
    }

    static class StartCommand implements ShellCommand {
        @Override
        public String getName() {
            return "starthttp";
        }

        @Override
        public void execute(String[] args, ShellInfo info) throws ShellException {
            Server server = (Server) info.getProperty("server");
            int standardPort = (Integer) info.getProperty("standardPort");
            TableProviderImplementation provider = (TableProviderImplementation) info.getProperty("provider");

            if (args.length > 1) {
                throw new ShellException("Too many arguments");
            }

            int portToSet = standardPort;
            if (args.length == 1) {
                try {
                    portToSet = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    System.out.println("not started: wrong port number");
                    return;
                }
            }



            if (server != null && server.isStarted()) {
                System.out.println("not started: already started");
                return;
            }

            try {
                server = new Server(portToSet);
                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

                context.setContextPath("/");
                context.addServlet(new ServletHolder(new ServletBegin(provider)), "/begin");
                context.addServlet(new ServletHolder(new ServletCommit(provider)), "/commit");
                context.addServlet(new ServletHolder(new ServletRollback(provider)), "/rollback");
                context.addServlet(new ServletHolder(new ServletGet(provider)), "/get");
                context.addServlet(new ServletHolder(new ServletPut(provider)), "/put");
                context.addServlet(new ServletHolder(new ServletSize(provider)), "/size");

                server.setHandler(context);
                server.start();
                info.setProperty("server", server);
                info.setProperty("currentPort", portToSet);
                System.out.println(String.format("started at %d", portToSet));
            } catch (Exception e) {
                info.setProperty("server", null);
                info.setProperty("currentPort", -1);
                System.out.println("not started: " + ShellEmulator.getNiceMessage(e));
            }
        }
    }

    static class StopCommand implements ShellCommand {
        @Override
        public String getName() {
            return "stophttp";
        }

        @Override
        public void execute(String[] args, ShellInfo info) throws ShellException {
            Server server = (Server) info.getProperty("server");
            int currentPort = (Integer) info.getProperty("currentPort");

            try {
                if (server == null || !server.isStarted()) {
                    System.out.println("not started");
                } else {
                    try {
                        server.stop();
                    } catch (Exception e) {
                        // ignored
                    }
                    System.out.println(String.format("stopped at %d", currentPort));
                }
            } finally {
                info.setProperty("server", null);
                info.setProperty("currentPort", -1);
            }
        }
    }
}
