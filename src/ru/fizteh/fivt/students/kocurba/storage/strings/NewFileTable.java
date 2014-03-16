package ru.fizteh.fivt.students.kocurba.storage.strings;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.Set;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.students.kocurba.shell.StateWrap;
import ru.fizteh.fivt.students.kocurba.shell.command.Command;
import ru.fizteh.fivt.students.kocurba.shell.command.RmCommand;

public class NewFileTable implements Table {

    private String name;
    private String filename;
    private Map<String, String> data;
    private Map<String, String> trueData;


    public NewFileTable(String name, String filename) {
        this.name = name;
        if (filename == null) {
            //throw new IllegalArgumentException();
            System.exit(1);
        }
        if (!Files.exists(Paths.get(filename))) {
            try {
                Files.createDirectory(Paths.get(filename));
            } catch (IOException e) {
                //
            }
        }
        this.filename = filename;
        try {
            read();
        } catch (IOException e) {
            //do smth
        }
    }


    public void copyMap(Map<String, String> map, Map<String, String> map2) {
        if (!map.isEmpty()) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
             map2.put(entry.getKey(), entry.getValue());
        }
        }
    }

    public int commitSize() {
        int size = 0;
        if (!trueData.isEmpty()) {
            for (Map.Entry<String, String> entry : trueData.entrySet()) {
                if (data.get(entry.getKey()) != entry.getValue()) {
                    size++;
                }

            }
        }
        if (!data.isEmpty())  {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                if (trueData.get(entry.getKey()) == null) {
                    size++;
                }
            }
        }
        return size;
    }

    private void read() throws IOException {

        this.data = new HashMap<String, String>();
        this.trueData = new HashMap<String, String>();
        for (int i = 0; i < 16 ; ++i) {
            File dir = new File(new File(System.getProperty("fizteh.db.dir"), this.name), new String(i + ".dir"));

            for (int j = 0; j < 16 ; ++j) {
                File file = new File(dir, new String(j + ".dat"));
                if (file.exists()) {
                    DataInputStream inStream = new DataInputStream(new FileInputStream(file));
                    try {
                        for (; ; ) {
                            int keyLength = inStream.readInt();
                            int valueLength = inStream.readInt();

                            byte[] rawKey = new byte[keyLength];
                            for (int q = 0; q < keyLength; ++q) {
                                rawKey[q] = inStream.readByte();
                            }

                            byte[] rawValue = new byte[valueLength];
                            for (int q = 0; q < valueLength; ++q) {
                                rawValue[q] = inStream.readByte();
                            }

                            trueData.put(new String(rawKey), new String(rawValue));
                        }



                    }   catch (EOFException e)  {
                        //do nothing
                    }   catch (IOException e2)  {
                        //e2.printStackTrace();
                    }  finally {
                        try {
                            inStream.close();
                        } catch (IOException e) {
                            //
                        }
                    }
                }
            }
        }

    data = new HashMap<String, String>();
    copyMap(this.trueData, this.data);

    }

    @Override
    public String put(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException();
        }
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
        return data.remove(key);
    }

    public int rollback() {

        int result = commitSize();

        data = new HashMap<String, String>();
        copyMap(trueData, data);
        return result;

    }

    private void writeMap(File file, Map<String, String> map) {
        try {
            DataOutputStream outStream = new DataOutputStream(new FileOutputStream(file.getCanonicalFile()));

            for (Map.Entry<String, String> entry : map.entrySet()) {
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
    }
    @Override
    public int commit() {
        try {
            File workingDir = new File(System.getProperty("fizteh.db.dir"), name);
            StateWrap<File> state = new StateWrap<File>(workingDir.getCanonicalFile());
            String[] listFile = workingDir.list();
            if (listFile != null) {
                for (String str : listFile) {
                    String[] args = {"asd", str};
                    Command<File> rm = new RmCommand();
                    rm.executeCommand(state, args);
                }
            }
        } catch (IOException e) {
              //
        }
        int result = commitSize();
        Set<String> keys = data.keySet();
        try {
            if (!keys.isEmpty()) {
                for (int i = 0; i < 16; ++i) {
                    File dir = new File(new File(new File(System.getProperty("fizteh.db.dir")), this.name),
                            new String(i + ".dir"));
                    for (int j = 0; j < 16; ++j) {
                        Map<String, String> map = new HashMap<String, String>();
                        File file = new File(dir, new String(j + ".dat"));
                        for (String key : keys) {

                            int hashCode = Math.abs(key.hashCode());
                            int nDirectory = hashCode % 16;
                            int nFile = hashCode / 16 % 16 ;

                            if ((nDirectory == i) && (nFile == j)) {
                                if (!dir.getCanonicalFile().exists()) {

                                    dir.getCanonicalFile().mkdir();
                                }
                                if (!file.getCanonicalFile().exists()) {
                                    file.getCanonicalFile().createNewFile();
                                }
                                map.put(key, data.get(key));

                            }
                        }

                        if (!map.isEmpty()) {
                            writeMap(file, map);
                        }

                    }

                    if (dir.getCanonicalFile().listFiles() == null) {
                        dir.delete();
                    }
                }
            }
        } catch (Exception e) {
                e.printStackTrace();
        }

        trueData = new HashMap<String, String>();
        copyMap(data, trueData);
        return result;
    }



    @Override
    public String getName() {
        return this.name;
    }


    @Override
    public int size() {
        return this.data.size();
    }

}
