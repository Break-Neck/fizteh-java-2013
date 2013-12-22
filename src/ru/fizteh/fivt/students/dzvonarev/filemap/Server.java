package ru.fizteh.fivt.students.dzvonarev.filemap;

public class Server {

    private static boolean isStarted = false;
    private static Servlet myServlet;


    public static void stopServlet() {
        isStarted = false;
    }

    public static Servlet getServlet() {
        return myServlet;
    }

    public static void assignServlet(Servlet servlet) {
        myServlet = servlet;
        isStarted = true;
    }

    public static boolean isStarted() {
        return isStarted;
    }

}
