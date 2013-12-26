package ru.fizteh.fivt.students.zinnatullin.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileSystem {
	
	protected static File currentPath = new File("").getAbsoluteFile();
	
	public static File resolvePath(String path) {
        File newPath = new File(path);
        if (newPath.exists() && newPath.isAbsolute()) {
            try {
                return newPath.getCanonicalFile().getAbsoluteFile();
            } catch (IOException e) {
                return null;
            }
        } 
        newPath = new File(currentPath.getAbsolutePath());
        String directories[] = path.split("\\" + File.separator);
        for (String directory : directories) {
            if (!directory.equals(".")) {
                if (directory.equals("..")) {
                    newPath = newPath.getParentFile();
                } else {
                    newPath = new File(newPath.getAbsolutePath(), directory);
                }
                if (newPath == null) {
                    return null;
                }
            }
        }
        return newPath;
    }
	
    public static boolean copy(File source, File destination, String command) {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(source);
            outputStream = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            while (true) {
                int length = inputStream.read(buffer);
                if (length < 0) {
                    break;
                }
                outputStream.write(buffer, 0, length);
            }
            return true;
        } catch (IOException e) {
            System.out.println(command + ": /'" + destination.getAbsolutePath() + "\': file already exists");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    System.out.println(command + ": /'" + source.getAbsolutePath() + "\': cannot close file");
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    System.out.println(command + ": /'" + destination.getAbsolutePath() + "\': cannot close file");
                }
            }
        }
        return false;
    }
	
    public static boolean recursiveCopy(File source, File destination, String command) {
        if (source.isDirectory()) {
            try {
                if (!destination.mkdir()) {
                    System.out.println(command + ": \'" + destination.getAbsolutePath() + "\': couldn't create directory");
                    return false;
                }
            } catch (SecurityException e) {
                System.out.println(command + ": \'" + destination.getAbsolutePath() + "\': haven't rights to create directory");
                return false;
            }
            File files[] = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    File newFile = new File(destination.getAbsolutePath() + File.separator + file.getName());
                    if (!recursiveCopy(file, newFile, command)) {
                        return false;
                    }
                }
            }
        } else {
            try {
                if (!destination.createNewFile()) {
                    System.out.println(command + ": \'" + destination.getAbsolutePath() + "\': couldn't create file");
                    return false;
                }
                if (!destination.canWrite() || !source.canRead()) {
                    Shell.printMessage(command + ": \'" + destination.getAbsolutePath() + "\': haven't rights to create file");
                    return false;
                }
                return copy(source, destination, command);
            } catch (SecurityException e) {
                System.out.println(command + ": \'" + destination.getAbsolutePath() + "\': haven't rights to create file");
                return false;
            } catch (IOException e) {
                System.out.println(command + ": \'" + destination.getAbsolutePath() + "\': couldn't create file");
                return false;
            }
        }
        return true;
    }
	
    public static boolean safeCopy(File source, File destination, String command) {
        if (source == null) {
            System.out.println(command + ": invalid source path");
        } else if (!source.exists()) {
            System.out.println(command + ": source path doesn't exists");
        } else if (destination == null) {
            System.out.println(command + ": invalid destination path");
        } else if (!destination.getParentFile().exists()) {
            System.out.println(command + ": destination path doesn't exists");
        } else if (destination.getParent().equals(source.getParent()) && destination.getName().equals(source.getName())/*destination.getAbsolutePath().contains(source.getAbsolutePath())*/) {
            System.out.println(command + ": source path is part of destination path");
        } else {
            return recursiveCopy(source, destination, command);
        }
        return false;
    }
	
    public static boolean recursiveRemove(File removable, String command) {
        if (removable.isDirectory()) {
            File files[] = removable.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!recursiveRemove(file, command)) {
                        return false;
                    }
                }
            }
        }
        try {
            if (!removable.delete()) {
                System.out.println(command + ": couldn't remove file \'" + removable.getAbsolutePath() + "\'");
            }
            return true;
        } catch (SecurityException e) {
            System.out.println(command + ": couldn't remove file \'" + removable.getAbsolutePath() + "\'");
        }
        return false;
    }
	
}
