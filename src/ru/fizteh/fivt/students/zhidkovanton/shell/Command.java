package ru.fizteh.fivt.students.zhidkovanton.shell;

public class Command {
    private String initialCommand;
    private String[] argList;

    public Command(final String args) {
        initialCommand = args;

        StringBuilder str = new StringBuilder(args.trim());

        int i = 0;
        while (i < str.length() - 1) {
            if (str.charAt(i) == ' ' && str.charAt(i + 1) == ' ') {
                str.deleteCharAt(i);
            } else {
                ++i;
            }
        }

        argList = str.toString().split(" ");
    }

    public String getArg(final int index) {
        return argList[index];
    }

    public int length() {
        return argList.length;
    }
}
