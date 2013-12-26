package ru.fizteh.fivt.students.sergeymelikov.utils;

public class Utils {
    private Utils() {
    }

    public static int getNumberOfFile(String key) {
        int hashcode = key.hashCode();
        int ndirectory = Math.abs(hashcode % 16);
        int nfile = Math.abs(hashcode / 16 % 16);
        return ndirectory * 16 + nfile;
    }
}
