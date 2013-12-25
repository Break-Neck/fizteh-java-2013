package ru.fizteh.fivt.students.zhidkovanton.shell;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    static final int END_OF_INPUT = -1;
    static final int END_OF_TRANSMISSION = 4;
    private static Shell shell;

    public static boolean checkTerminate(final String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) == END_OF_INPUT || s.charAt(i) == END_OF_TRANSMISSION) {
                return true;
            }
        }
        return false;
    }

    public static void interactiveMode() throws IOException {

        Scanner reader = new Scanner(System.in);
        System.out.print("$ ");

        while (true) {
            try {
                if (!reader.hasNext()) {
                    System.exit(0);
                }

                String commands = reader.nextLine();

                if (checkTerminate(commands)) {
                    System.exit(0);
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

    public static void notInteractiveMode(String[] commands) throws IOException {
        Parser parser = new Parser(commands);
        while (!parser.isEmpty()) {
            shell.execute(parser.getCommand());
        }
    }

    public static void main(String[] args) throws IOException {
        shell = new Shell();

        if (args.length == 0) {
            interactiveMode();
        } else {
            notInteractiveMode(args);
        }

    }
}
