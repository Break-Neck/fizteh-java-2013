package ru.fizteh.fivt.students.baldindima.junit;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;


import java.util.concurrent.ConcurrentHashMap;

import ru.fizteh.fivt.storage.structured.TableProvider;


public class DataBaseFile {
    public final Map<String, String> mapFromFile;
    protected File dataBaseFile;
    protected String fileName;
    private TableProvider provider;
    private DataBase table;
    private int fileNumber;
    private int directoryNumber;

    public DataBaseFile(String fullName, int nDirectoryNumber, int nFileNumber,
                        TableProvider nProvider, DataBase nTable) throws IOException {
        fileName = fullName;
        provider = nProvider;
        table = nTable;
        dataBaseFile = new File(fileName);
        fileNumber = nFileNumber;
        directoryNumber = nDirectoryNumber;
        mapFromFile = new ConcurrentHashMap<>();

        read();
        check();

    }

    public boolean check() throws IOException {
        for (Map.Entry<String, String> curPair : mapFromFile.entrySet()) {
            if (!(((Math.abs(curPair.getKey().getBytes("UTF-8")[0]) % 16) == directoryNumber)
                    && ((Math.abs(curPair.getKey().getBytes("UTF-8")[0] / 16) % 16 == fileNumber)))) {
                throw new IOException("Wrong file format key[0] =  "
                        + String.valueOf(Math.abs(curPair.getKey().getBytes("UTF-8")[0]))
                        + " in file " + fileName);
            }
            try {
                provider.deserialize(table, (curPair.getValue()));
            } catch (ParseException e) {
                throw new IOException("Invalid file format! (parse exception error!)");
            }
        }
        return true;
    }


    public void read() throws IOException {
        File dataBaseDirectory = new File(dataBaseFile.getParent());
        if (dataBaseDirectory.exists() && dataBaseDirectory.list().length == 0) {
            throw new IOException("Empty dir!");
        }
        if (!dataBaseDirectory.exists() || !dataBaseFile.exists()) {
            return;
        }
        RandomAccessFile randomDataBaseFile = new RandomAccessFile(fileName, "rw");
        if (randomDataBaseFile.length() == 0) {
            randomDataBaseFile.close();
            return;
        }

        while (randomDataBaseFile.getFilePointer() < randomDataBaseFile.length() - 1) {
            int keyLength = randomDataBaseFile.readInt();
            int valueLength = randomDataBaseFile.readInt();
            if ((keyLength <= 0) || (valueLength <= 0)) {
                randomDataBaseFile.close();
                throw new IOException("wrong format");
            }

            byte[] key;
            byte[] value;
            try {
                key = new byte[keyLength];
                value = new byte[valueLength];
            } catch (OutOfMemoryError e) {
                randomDataBaseFile.close();
                throw new IOException("too large key or value");
            }
            randomDataBaseFile.read(key);
            randomDataBaseFile.read(value);
            String keyString = new String(key, "UTF-8");
            String valueString = new String(value, "UTF-8");
            mapFromFile.put(keyString, valueString);
        }
        randomDataBaseFile.close();
        if (mapFromFile.size() == 0) {
            throw new IOException("Empty file!");
        }

    }


    public void write() throws IOException {
        File dataBaseDirectory = new File(dataBaseFile.getParent());
        if (mapFromFile.isEmpty()) {
            if ((dataBaseFile.exists()) && (!dataBaseFile.delete())) {
                throw new DataBaseException("Cannot delete a file!");
            }

            if (dataBaseDirectory.exists() && dataBaseDirectory.list().length <= 0) {
                if (!dataBaseDirectory.delete()) {
                    throw new DataBaseException("Cannot delete a directory");
                }
            }
        } else {
            if (!dataBaseDirectory.exists() && !dataBaseDirectory.mkdir()) {
                throw new DataBaseException("Cannot create a directory");
            }
            if (!dataBaseFile.exists()) {
                if (!dataBaseFile.createNewFile()) {
                    throw new DataBaseException("Cannot create a file " + fileName);
                }
            }
            RandomAccessFile randomDataBaseFile = new RandomAccessFile(fileName, "rw");

            randomDataBaseFile.getChannel().truncate(0);
            for (Map.Entry<String, String> curPair : mapFromFile.entrySet()) {

                randomDataBaseFile.writeInt(curPair.getKey().getBytes("UTF-8").length);
                randomDataBaseFile.writeInt(curPair.getValue().getBytes("UTF-8").length);
                randomDataBaseFile.write(curPair.getKey().getBytes("UTF-8"));
                randomDataBaseFile.write(curPair.getValue().getBytes("UTF-8"));


            }
            randomDataBaseFile.close();


        }
    }

    
}	