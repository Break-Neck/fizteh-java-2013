package ru.fizteh.fivt.students.zhidkovanton.multifilemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.InvalidCommandException;
import ru.fizteh.fivt.students.zhidkovanton.shell.Main;
import ru.fizteh.fivt.students.zhidkovanton.shell.Parser;
import ru.fizteh.fivt.students.zhidkovanton.shell.Shell;

import java.io.IOException;
import java.util.Scanner;

public class FileService {
    private static Shell shell;
    private static DataBaseFactory dataBaseFactory;

    static void interactiveMode() throws IOException {

        Scanner reader = new Scanner(System.in);
        System.out.print("$ ");

        while (true) {
            try {
                if (!reader.hasNext()) {
                    throw new ShellExitException("Ctrl + D exit");
                }

                String commands = reader.nextLine();

                if (Main.checkTerminate(commands)) {
                    throw new ShellExitException("Ctrl + D exit or EOF!");
                }

                Parser parser = new Parser(commands);

                if (!parser.isEmpty()) {
                    shell.execute(parser.getCommand());
                }
            } catch (InvalidCommandException e) {
                System.err.println(e.getMessage());
            } finally {
                System.out.print("$ ");
            }
        }
    }

    static void notInteractiveMode(String[] commands) throws IOException {
        Parser parser = new Parser(commands);
        while (!parser.isEmpty()) {
            shell.execute(parser.getCommand());
        }
    }

    public static void main(String[] args) throws IOException {

        dataBaseFactory = new DataBaseFactory(System.getProperty("fizteh.db.dir"));

        shell = new Shell();

        shell.add(new ShellPut(dataBaseFactory));
        shell.add(new ShellExit());
        shell.add(new ShellGet(dataBaseFactory));
        shell.add(new ShellRemove(dataBaseFactory));
        shell.add(new ShellCreate(dataBaseFactory));
        shell.add(new ShellDrop(dataBaseFactory));
        shell.add(new ShellUse(dataBaseFactory));

        try {
            if (args.length == 0) {
                interactiveMode();
            } else {
                notInteractiveMode(args);
                if (dataBaseFactory.dataBase != null) {
                    dataBaseFactory.dataBase.print();
                }
            }
        } catch (ShellExitException e) {
            if (dataBaseFactory.dataBase != null) {
                dataBaseFactory.dataBase.print();
            }
            System.exit(0);
        } catch (FileAccessException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

    }
}
