package ru.fizteh.fivt.students.baranov.wordcounter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("WordCounter needs arguments");
            System.err.println("Arguments:");
            System.err.println("Paths to files: file1.txt test/file2.txt //for example...");
            System.err.println("-o FILENAME if //you want to write results to file");
            System.err.println("-a //if you want to count number of words in all files");
            System.exit(1);
        }

        boolean output = false;
        boolean aggregate = false;
        boolean outputIsFound = false;
        List<File> files = new ArrayList<>();
        File outputFile = null;
        String dir = System.getProperty("user.dir");


        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-o")) {
                output = true;
                continue;
            }
            if (args[i].equals("-a")) {
                aggregate = true;
                continue;
            }
            if (output && !outputIsFound && args[i - 1].equals("-o")) {
                outputFile = new File(args[i]);
                outputIsFound = true;
                continue;
            }

            File newFile = new File(args[i]);
            if (!newFile.isAbsolute()) {
                newFile = new File(dir, args[i]);
            }

            files.add(newFile);
        }

        MyWordCounterFactory factory = new MyWordCounterFactory();
        MyWordCounter counter = factory.create();
        OutputStream stream = System.out;

        if (output) {
            if (outputIsFound) {
                try {
                    stream = new FileOutputStream(outputFile);
                } catch (FileNotFoundException e) {
                    System.err.println(e);
                    System.exit(1);
                }
            } else {
                System.err.println("output not found");
                System.exit(1);
            }
        }

        try {
            counter.count(files, stream, aggregate);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        try {
            stream.close();
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }
}
