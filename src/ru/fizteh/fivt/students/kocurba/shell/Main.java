package ru.fizteh.fivt.students.kocurba.shell;

import java.io.File;
import java.io.IOException;

import ru.fizteh.fivt.students.kocurba.shell.command.*;

/***
 * 
 * Task 02 - Shell
 * 
 * @author Alina Kotsurba
 * 
 */
public class Main {


    public static void main(String[] args) throws IOException {

        File workingDir = new File(System.getProperty("user.dir"));
        StateWrap<File> state = new StateWrap<File>(workingDir.getCanonicalFile());
        Shell<File> shell = new Shell<File>(state);

        Command<File> pwd = new PwdCommand();

        Command<File> dir = new DirCommand();

        Command<File> mkdir = new MkdirCommand();

        Command<File> rm = new RmCommand();

        Command<File> cd = new CdCommand();

        Command<File> cp = new CpCommand();

        Command<File> mv = new MvCommand();

        Command<File> exit = new ExitCommand();


        Command[] command = {pwd, dir, mkdir, rm, cd, cp, mv, exit};


        if (0 != args.length) {
            shell.batchMode(args, command);
        } else {
            shell.interactiveMode(command);
        }

        System.exit(0);
    }
    }
