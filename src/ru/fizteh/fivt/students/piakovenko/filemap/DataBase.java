package ru.fizteh.fivt.students.piakovenko.filemap;



import ru.fizteh.fivt.students.piakovenko.shell.MyException;
import ru.fizteh.fivt.students.piakovenko.shell.Shell;
import ru.fizteh.fivt.students.piakovenko.shell.Remove;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.lang.Math;

/**
 * Created with IntelliJ IDEA.
 * User: Pavel
 * Date: 12.10.13
 * Time: 22:45
 * To change this template use File | Settings | File Templates.
 */
public class DataBase {
    private final String pathToDatabaseDirectory = "fizteh.db.dir";
    private String name;
    private RandomAccessFile raDataBaseFile = null;
    private DataBaseMap map = null;
    private Shell shell = null;
    private File dataBaseStorage = null;

    private boolean isValidNameDirectory(String name){
        if (name.length() < 5 || name.length() > 6)
            return false;
        int number = Integer.parseInt(name.substring(0, name.indexOf('.')), 10);
        if (number > 15 || number < 0)
            return false;
        if (!name.substring(name.indexOf('.') + 1).equals("dir"))
            return false;
        return true;
    }

    private boolean isValidNameFile(String name){
        if (name.length() < 5 || name.length() > 6)
            return false;
        int number = Integer.parseInt(name.substring(0, name.indexOf('.')), 10);
        if (number > 15 || number < 0)
            return false;
        if (!name.substring(name.indexOf('.') + 1).equals("dat"))
            return false;
        return true;
    }

    private int ruleNumberDirectory (String key) {
        int b = Math.abs(key.getBytes()[0]);
        return b % 16;
    }

    private int ruleNumberFile (String key) {
        int b = Math.abs(key.getBytes()[0]);
        return b / 16 % 16;
    }


    private void readFromFile() throws IOException, MyException {
        long length = raDataBaseFile.length();
        while (length > 0) {
            int l1 = raDataBaseFile.readInt();
            if (l1 <= 0) {
                throw new MyException(new Exception("Length of new key less or equals zero"));
            } else if (l1 > 1024 * 1024) {
                throw new MyException(new Exception("Key greater than 1 MB"));
            }
            length -= 4;
            int l2 = raDataBaseFile.readInt();
            if (l2 <= 0) {
                throw new MyException(new Exception("Length of new value less or equals zero"));
            } else if (l2 > 1024 * 1024) {
                throw new MyException(new Exception("Value greater than 1 MB"));
            }
            length -= 4;
            byte [] key = new byte [l1];
            byte [] value = new byte [l2];
            if (raDataBaseFile.read(key) < l1) {
                throw new MyException(new Exception("Key: read less, that it was pointed to read"));
            } else {
                length -= l1;
            }
            if (raDataBaseFile.read(value) < l2) {
                throw new MyException(new Exception("Value: read less, that it was pointed to read"));
            } else {
                length -= l2;
            }
            map.primaryPut(new String(key, StandardCharsets.UTF_8), new String(value, StandardCharsets.UTF_8));
        }
    }

    private void readFromFile (File storage, int numberOfDirectory) throws MyException, IOException {
        RandomAccessFile ra = new RandomAccessFile(storage, "rw");
        int numberOfFile =  Integer.parseInt(storage.getName().substring(0, storage.getName().indexOf('.')), 10);
        try {
            long length = ra.length();
            while (length > 0) {
                int l1 = ra.readInt();
                if (l1 <= 0) {
                    throw new MyException(new Exception("Length of new key less or equals zero"));
                } else if (l1 > 1024 * 1024) {
                    throw new MyException(new Exception("Key greater than 1 MB"));
                }
                length -= 4;
                int l2 = ra.readInt();
                if (l2 <= 0) {
                    throw new MyException(new Exception("Length of new value less or equals zero"));
                } else if (l2 > 1024 * 1024) {
                    throw new MyException(new Exception("Value greater than 1 MB"));
                }
                length -= 4;
                byte [] key = new byte [l1];
                byte [] value = new byte [l2];
                if (ra.read(key) < l1) {
                    throw new MyException(new Exception("Key: read less, that it was pointed to read"));
                } else {
                    length -= l1;
                }
                if (ra.read(value) < l2) {
                    throw new MyException(new Exception("Value: read less, that it was pointed to read"));
                } else {
                    length -= l2;
                }
                String keyString = new String(key, StandardCharsets.UTF_8);
                String valueString = new String(value, StandardCharsets.UTF_8);
                if (ruleNumberFile(keyString) != numberOfFile || ruleNumberDirectory(keyString) != numberOfDirectory) {
                    throw new MyException(new Exception("Wrong place of key value! Key: " + keyString + " Value: " + valueString));
                } else {
                    map.primaryPut( keyString, valueString);
                }
            }
        } catch (MyException e) {
            System.err.println("Error! " + e.what());
            ra.close();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
            ra.close();
            System.exit(1);
        }
        ra.close();
    }


    private void saveToFile () throws IOException {
        long length  = 0;
        raDataBaseFile.seek(0);
        for (String key: map.getMap().keySet()) {
            byte [] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte [] valueBytes = map.getMap().get(key).getBytes(StandardCharsets.UTF_8);
            raDataBaseFile.writeInt(keyBytes.length);
            raDataBaseFile.writeInt(valueBytes.length);
            raDataBaseFile.write(keyBytes);
            raDataBaseFile.write(valueBytes);
            length += 4 + 4 + keyBytes.length + valueBytes.length;
        }
        raDataBaseFile.setLength(length);
    }

    private void saveToFile(File f, String key, String value) throws IOException {
        RandomAccessFile ra = new RandomAccessFile(f, "rw");
        ra.seek(ra.length());
        byte [] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte [] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        ra.writeInt(keyBytes.length);
        ra.writeInt(valueBytes.length);
        ra.write(keyBytes);
        ra.write(valueBytes);
        ra.close();
    }

    private void saveToDirectory() throws IOException, MyException{
        if (dataBaseStorage.exists()) {
            Remove.removeRecursively(dataBaseStorage);
        }
        if (!dataBaseStorage.mkdirs()){
            throw new MyException(new Exception("Unable to create this directory - " + dataBaseStorage.getCanonicalPath()));
        }
        for (String key : map.getMap().keySet()) {
            Integer numberOfDirectory = ruleNumberDirectory(key);
            Integer numberOfFile = ruleNumberFile(key);
            File directory = new File (dataBaseStorage, numberOfDirectory.toString() + ".dir");
            if (!directory.exists()) {
                if (!directory.mkdirs()){
                    throw new MyException(new Exception("Unable to create this directory - " + directory.getCanonicalPath()));
                }
            }
            File writeFile = new File(directory, numberOfFile.toString() + ".dat" );
            if (!writeFile.exists()) {
                writeFile.createNewFile();
            }
            saveToFile(writeFile, key, map.getMap().get(key));
        }
    }



    private void loadDataBase (File dataBaseFile) throws IOException, MyException {
       raDataBaseFile = new RandomAccessFile(dataBaseFile, "rw");
       try {
            readFromFile();
       } catch (MyException e) {
            System.err.println("Error! " + e.what());
            System.exit(1);
       }
    }


    private void readFromDirectory(File dir, int numberOfDirectory) throws MyException, IOException {
        for (File f: dir.listFiles()) {
            if (!isValidNameFile(f.getName())) {
                throw new MyException(new Exception("Wrong name of file!"));
            }
            readFromFile(f, numberOfDirectory);
        }
    }



    private void loadFromDirectory (File directory) throws MyException, IOException {
        for (File f : directory.listFiles()) {
            if (!isValidNameDirectory(f.getName())) {
                throw new MyException(new Exception("Wrong name of directory!"));
            }
            int numberOfDirectory = Integer.parseInt(f.getName().substring(0, f.getName().indexOf('.')), 10);
            readFromDirectory(f, numberOfDirectory);
        }
    }


    public DataBase (Shell sl, File storage) {
        map = new DataBaseMap();
        shell  = sl;
        dataBaseStorage = storage;
        name = storage.getName();
    }

    public void load () throws IOException, MyException{
        if (dataBaseStorage.isFile()) {
            loadDataBase(dataBaseStorage);
        } else {
            loadFromDirectory(dataBaseStorage);
        }
    }

    public String getName () {
        return name;
    }

    public void initialize () {
        shell.addCommand(new Exit(this));
        shell.addCommand(new Put(this));
        shell.addCommand(new Get(this));
        shell.addCommand(new ru.fizteh.fivt.students.piakovenko.filemap.Remove(this));
        try {
            load();
        } catch (IOException e) {
            System.err.println("Error! " + e.getMessage());
            System.exit(1);
        } catch (MyException e) {
            System.err.println("Error! " + e.what());
            System.exit(1);
        }
    }


    public void saveDataBase () throws IOException, MyException {
        if (dataBaseStorage.isFile()) {
            try {
                saveToFile();
            } finally {
                raDataBaseFile.close();
            }
        } else {
            saveToDirectory();
        }
    }

    public void get (String key) {
        map.get(key);
    }

    public void put (String key, String value) {
        map.put(key, value);
    }

    public void remove(String key) {
        map.remove(key);
    }

    public File returnFiledirectory() {
        return dataBaseStorage;
    }
}
