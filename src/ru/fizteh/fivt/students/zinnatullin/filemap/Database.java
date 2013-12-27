package ru.fizteh.fivt.students.zinnatullin.filemap;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    private String path;
    private String file;
    private HashMap<String, String> data;
    
    public Database(String path, String file) throws IOException {
        File dbFile = new File(path, file);
        if (!dbFile.exists()) {
            dbFile.createNewFile();
        }
        this.path = path;
        this.file = file;
        data = new HashMap();
    }
    
    public Database setFilename(String path, String file) {
        this.path = path;
        this.file = file;
        return this;
    }
    
    public Database getData() throws IOException {
        
        File inputFile = new File(path, file);
        RandomAccessFile foutput = new RandomAccessFile(inputFile, "r");
        if (foutput.length() > 444444444) {
            throw new IOException("file is too big");
        }

        while (foutput.getFilePointer() != foutput.length()) {
            int klength = foutput.readInt();
            if (klength < 1 || klength > foutput.length() - foutput.getFilePointer() + 4) {
                foutput.close();
                throw new IllegalArgumentException("Illegal key length");
            }
            int vlength = foutput.readInt();
            if (vlength < 1 || vlength > foutput.length() - foutput.getFilePointer() + 4) {
                foutput.close();
                throw new IllegalArgumentException("Illegal value length");
            }
            byte[] bytekey = new byte[klength];
            byte[] bytevalue = new byte[vlength];
            foutput.read(bytekey);
            foutput.read(bytevalue);
            String key = new String(bytekey, StandardCharsets.UTF_8);
            String value = new String(bytevalue, StandardCharsets.UTF_8);
            data.put(key, value);
        }
        
        foutput.close();
        return this;
    }
    
    public Database saveData() throws IOException {
        File outputFile = new File(path, file);

        RandomAccessFile finput = new RandomAccessFile(outputFile, "rw");
        if (finput.length() > 444444444) {
            throw new IOException("file is too big");
        }
        
        finput.setLength(0);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            finput.writeInt(key.getBytes(StandardCharsets.UTF_8).length);
            finput.writeInt(value.getBytes(StandardCharsets.UTF_8).length);
            finput.write(key.getBytes(StandardCharsets.UTF_8));
            finput.write(value.getBytes(StandardCharsets.UTF_8));
        }
        
        finput.close();
        return this;
    }
        
    public String get(String key) {
        String value = "";
        try {
            this.getData();
        } catch (IOException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (data.containsKey(key)) {
            value = data.get(key);
        }
        return value;
    }
    
    public String put(String key, String value) {
        String oldValue = "";
        try {
            this.getData();
            if (data.containsKey(key)) {
                oldValue = data.get(key);
            }
            data.put(key, value);
            this.saveData();
        } catch (Exception ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return oldValue;
    }
    
    public String remove(String key) {
        String oldValue = null;
        try {
            this.getData();
            oldValue = data.remove(key);
            this.saveData();
        } catch (Exception ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return oldValue;
    }
}
