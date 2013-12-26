package ru.fizteh.fivt.students.dmitryIvanovsky.servletHolder;

public class CommonServletFunction {
    public static int checkTid(String tid) {
        if (tid.length() != 5) {
            throw new IllegalStateException();
        }
        try {
            return Integer.parseInt(tid);
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }
}
