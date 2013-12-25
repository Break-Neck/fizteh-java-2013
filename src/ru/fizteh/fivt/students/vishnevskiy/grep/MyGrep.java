package ru.fizteh.fivt.students.vishnevskiy.grep;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ru.fizteh.fivt.file.Grep;

public class MyGrep implements Grep {
    private Pattern pattern;

    public MyGrep(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Invalid pattern");
        }
        try {
            this.pattern = Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            System.err.println("Invalid pattern syntax");
        }
    }

    public void find(List<File> inputFiles, OutputStream output, boolean inverse) throws IOException {
        if ((inputFiles == null) || inputFiles.isEmpty()) {
            throw new IllegalArgumentException("Invalid list of input files");
        }
        if (output == null) {
            throw new IllegalArgumentException("Invalid output stream");
        }

        String colon = ':' + System.lineSeparator();
        for (File file : inputFiles) {
            output.write(file.getName().getBytes(StandardCharsets.UTF_8));
            output.write(colon.getBytes(StandardCharsets.UTF_8));

            if (!file.exists() || (file.exists() && file.isDirectory())) {
                String message = "file not found" + System.lineSeparator();
                output.write(message.getBytes(StandardCharsets.UTF_8));
                continue;
            }
            if (!file.canRead()) {
                String message = "file not available" + System.lineSeparator();
                output.write(message.getBytes(StandardCharsets.UTF_8));
                continue;
            }

            boolean matches = false;
            try (Reader fileReader = new FileReader(file);
                 BufferedReader reader = new BufferedReader(fileReader)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find() != inverse) {
                        matches = true;
                        line += System.lineSeparator();
                        output.write(line.getBytes(StandardCharsets.UTF_8));
                    }
                }
                if (!matches) {
                    String message = "no matches" + System.lineSeparator();
                    output.write(message.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    public int count(List<File> inputFiles, boolean inverse) throws IOException {
        if ((inputFiles == null) || inputFiles.isEmpty()) {
            throw new IllegalArgumentException("Invalid list of input files");
        }

        int matches = 0;
        for (File file : inputFiles) {

            if (!file.exists() || (file.exists() && file.isDirectory())) {
                continue;
            }
            if (!file.canRead()) {
                continue;
            }

            try (Reader fileReader = new FileReader(file);
                 BufferedReader reader = new BufferedReader(fileReader)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find() != inverse) {
                        ++matches;
                    }
                }
            }
        }
        return matches;
    }

}
