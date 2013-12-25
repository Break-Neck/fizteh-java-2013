package ru.fizteh.fivt.students.vishnevskiy.grep;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class GrepMain {

    public static void main(String[] args) {
        try {
            String pattern = null;
            File outputFile = null;
            boolean count = false;
            boolean invert = false;
            List<File> inputFiles = new ArrayList<File>();
            int i = 0;
            while (i < args.length) {
                if (args[i].equals("-p")) {
                    if (pattern != null) {
                        throw new Exception("Only one pattern required");
                    }
                    ++i;
                    pattern = args[i];
                } else if (args[i].equals("-o")) {
                    if (outputFile != null) {
                        throw new Exception("Only one output file required");
                    }
                    ++i;
                    if (i == args.length) {
                        throw new Exception("Output file expected");
                    }
                    outputFile = new File(args[i]);
                } else if (args[i].equals("-c")) {
                    if (count) {
                        throw new Exception("Option -c is already presented");
                    }
                    count = true;
                } else if (args[i].equals("-i")) {
                    if (invert) {
                        throw new Exception("Option -i is already presented");
                    }
                    invert = true;
                } else {
                    inputFiles.add(new File(args[i]));
                }
                ++i;
            }

            if (pattern == null) {
                throw new Exception("Pattern expected");
            }

            OutputStream output = null;
            if (outputFile != null) {
                try {
                    output = new FileOutputStream(outputFile);
                } catch (FileNotFoundException e) {
                    System.err.format("%s: file not found\n", outputFile.getName());
                    System.exit(1);
                }
            } else {
                output = System.out;
            }

            try {
                MyGrep grep = new MyGrep(pattern);
                if (count) {
                    System.out.println(grep.count(inputFiles, invert));
                } else {
                    grep.find(inputFiles, output, invert);
                }
                output.flush();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            } finally {
                if ((output == null) && !output.equals(System.out)) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
