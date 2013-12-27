package ru.fizteh.fivt.students.zinnatullin.multifilemap;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Table {
    
    private String name;
    private File path;
    public HashMap data;
    
    public Table(File path, String name) {
        this.name = name;
        this.path = new File(path, name);
        
        data = new HashMap();
    }
    
    public Table getData(String nDir, String nFile) throws IOException {
        
        File nPath = new File(path, nDir);
        if (!nPath.exists()) {
            nPath.mkdir();
        }
        File inputFile = new File(nPath, nFile);
        if (!inputFile.exists()) {
            inputFile.createNewFile();
        }
        RandomAccessFile foutput = new RandomAccessFile(inputFile, "r");
        if (foutput.length() > 444444444) {
            throw new IOException("file is too big");
        }
        
        HashMap<String, String> data = new HashMap();
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
        
        if (!this.data.containsKey(nDir)) {
            HashMap filemap = new HashMap();
            filemap.put(nFile, data);
            this.data.put(nDir, filemap);
        } else {
            HashMap dirMap = (HashMap) this.data.get(nDir);
            dirMap.put(nFile, data);
            this.data.put(nDir, dirMap);
        }
        foutput.close();
        return this;
    }
    
    public Table saveData(String nDir, String nFile) throws IOException {
        File nPath = new File(path, nDir);
        if (!nPath.exists()) {
            nPath.mkdir();
        }
        File outputFile = new File(nPath, nFile);
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        
		RandomAccessFile finput = new RandomAccessFile(outputFile, "rw");
        if (finput.length() > 444444444) {
            throw new IOException("file is too big");
        }

		finput.setLength(0);
        HashMap dirMap = (HashMap) data.get(nDir);
        HashMap fileMap = (HashMap) dirMap.get(nFile);
        if (!fileMap.isEmpty()) {
            for (Iterator it = fileMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                String key = entry.getKey();
                String value = entry.getValue();
                
				finput.writeInt(key.getBytes(StandardCharsets.UTF_8).length);
				finput.writeInt(value.getBytes(StandardCharsets.UTF_8).length);
				finput.write(key.getBytes(StandardCharsets.UTF_8));
				finput.write(value.getBytes(StandardCharsets.UTF_8));
            }
        }
        finput.close();
        return this;
    }
}
