package ru.fizteh.fivt.students.sergeymelikov.multifilemap;

import java.io.IOException;
import ru.fizteh.fivt.students.sergeymelikov.utils.Shell;

public class MultiFileMapMain {

    public static void main(String[] args) {
        MultiFileMapState st = null;
        try {
            st = new MultiFileMapState(System.in, System.out);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        int status = Shell.startShell(args, st);
        System.exit(status);
    }
}
