package ru.fizteh.fivt.students.zhidkovanton.filemap;

import ru.fizteh.fivt.students.zhidkovanton.shell.InvalidCommandException;
import ru.fizteh.fivt.students.zhidkovanton.shell.Main;
import ru.fizteh.fivt.students.zhidkovanton.shell.Parser;
import ru.fizteh.fivt.students.zhidkovanton.shell.Shell;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class FileService {
    private static Shell shell;
    private static State state;

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

    static void readTable(File input) throws IOException {
        if (input.exists()) {
            RandomAccessFile in = new RandomAccessFile(input, "rw");

            while (in.getFilePointer() < in.length() - 1) {
                int keyLength = in.readInt();
                int valueLength = in.readInt();
                if ((keyLength <= 0) || (valueLength <= 0)) {
                    in.close();
                    throw new IOException("wrong format");
                }

                byte[] key;
                byte[] value;

                try {
                    key = new byte[keyLength];
                    value = new byte[valueLength];
                } catch (OutOfMemoryError e) {
                    in.close();
                    throw new IOException("too large key or value");
                }
                in.read(key);
                in.read(value);
                String keyString = new String(key, "UTF-8");
                String valueString = new String(value, "UTF-8");
                state.put(keyString, valueString);
            }
            in.close();
        } else {
            input.createNewFile();
        }
    }

    public static void main(String[] args) throws IOException {
        File data = new File(System.getProperty("fizteh.db.dir"), "db.dat");
        state = new State();
        readTable(data);

        shell = new Shell();

        shell.add(new ShellPut(state));
        shell.add(new ShellExit());
        shell.add(new ShellGet(state));
        shell.add(new ShellRemove(state));

        try {
            if (args.length == 0) {
                interactiveMode();
            } else {
                notInteractiveMode(args);
            }
        } catch (ShellExitException e) {
            state.print(data);
            System.exit(0);
        }
    }
}
