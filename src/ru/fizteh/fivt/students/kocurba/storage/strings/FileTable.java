package ru.fizteh.fivt.students.kocurba.storage.strings;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.io.*;

import ru.fizteh.fivt.storage.strings.Table;

public class FileTable implements Table {

    private String name;
    private String filename;
    private Map<String, String> data;
    private int commitSize;


    public FileTable(String name, String filename) {
        this.name = name;
        if (filename == null) {
            throw new IllegalArgumentException();
        }
        if (!Files.exists(Paths.get(filename))) {
            try {
                Files.createFile(Paths.get(filename));
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        this.filename = filename;
        rollback();
    }

    @Override
    public String put(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException();
        }
        ++this.commitSize;
        return data.put(key, value);
    }

    @Override
    public String get(String key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        return data.get(key);
    }

    @Override
    public String remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        ++this.commitSize;
        return data.remove(key);
    }

    public int rollback() {
        DataInputStream inStream;
        try {
            inStream = new DataInputStream(new FileInputStream(new File(this.filename)));
        } catch (IOException e) {
            e.printStackTrace();
            int result = commitSize;
            commitSize = 0;
            return result;
        }
        this.data = new HashMap<String, String>();
        try {
        for (; ; ) {
            int keyLength = inStream.readInt();
            int valueLength = inStream.readInt();

            byte[] rawKey = new byte[keyLength];
            for (int j = 0; j < keyLength; ++j) {
                rawKey[j] = inStream.readByte();
            }

            byte[] rawValue = new byte[valueLength];
            for (int j = 0; j < valueLength; ++j) {
                rawValue[j] = inStream.readByte();
            }

            data.put(new String(rawKey), new String(rawValue));
        }
        }   catch (EOFException e)  {
            //do nothing
        }   catch (IOException e2)  {
            e2.printStackTrace();
        }
        int result = commitSize;
        commitSize = 0;
        return result;
    }

    @Override
    public int commit() {

        try {
            DataOutputStream outStream = new DataOutputStream(new FileOutputStream(new File(this.filename)));

            for (Map.Entry<String, String> entry : this.data.entrySet()) {
                int length;
                length =  entry.getKey().getBytes(StandardCharsets.UTF_8).length;
                outStream.writeInt(length);

                length = entry.getValue().getBytes(StandardCharsets.UTF_8).length;
                outStream.writeInt(length);

                outStream.write(entry.getKey().getBytes(StandardCharsets.UTF_8));
                outStream.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
            }
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int result = commitSize;
        commitSize = 0;
        return result;
    }


    @Override
    public String getName() {
        return this.name;
    }
    /*
    public String getFilename() {
        return this.filename;
    } */

    @Override
    public int size() {
        return this.commitSize;
    }

}
