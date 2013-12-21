package ru.fizteh.fivt.students.krivchansky.filemap;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class FileMapReadingUtils implements Closeable{
    
    protected static RandomAccessFile tempFile;
    private static int valueShift = -1;
    
    public FileMapReadingUtils(String pathToFile) throws IOException {
        try {
            tempFile = new RandomAccessFile(pathToFile, "r");
        } catch (FileNotFoundException e) {
            tempFile = null;
            valueShift = 0;
            return;
        }
        skipKey();
        valueShift = readOffset();
        try {
            tempFile.seek(0);
        } catch (IOException e) {
            throw new IOException("Error aqcuired while seeking through file: " + e.getMessage());
        }
    }
    
    public String readKey() throws IOException {
        byte[] array;
        if (tempFile.getFilePointer() >= valueShift) {
            return null;
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte b = tempFile.readByte();
        while(b != 0) {
            bytes.write(b);
            b = tempFile.readByte();
        }
        array = GlobalUtils.bytesToArray(bytes);
        String returnKey;
        returnKey = new String(array, GlobalUtils.ENCODING);
        return returnKey;
    }
    
    public String readValue() throws IOException {
        int offset = readOffset();
        int nextOffset = readNextOffset();
        long currentOffset;
        currentOffset = tempFile.getFilePointer();
        tempFile.seek(offset);
        int valueLength = nextOffset-offset;
        byte[] bytes = new byte[valueLength];
        tempFile.read(bytes, 0, valueLength);
        tempFile.seek(currentOffset);
        return new String(bytes, GlobalUtils.ENCODING);
            
    }
    
    public static void scanFromDisk(String file, TableBuilder builder) throws IOException {
        if (!GlobalUtils.doesExist(file)) {
            throw new IOException("didn't exist");
        }
        FileMapReadingUtils read = new FileMapReadingUtils(file);
        while (!read.endOfFile()) {
            String key = read.readKey();
            String value = read.readValue();
            builder.put(key, value);
        }
        read.close();
    }
    
    public boolean endOfFile() throws IOException {
        if (tempFile == null) {
            return true;
        }
            boolean result = true;
        try {
            result = (tempFile.getFilePointer() == valueShift);
        } catch (EOFException ee) {
            return true;
        }
        return result;
    }
    
    
    private int readNextOffset() throws IOException {
        int nextOffset = 0;
        int currentOffset = (int) tempFile.getFilePointer();
        if (readKey() == null) {
            nextOffset = (int)tempFile.length();
        } else {
            nextOffset = readOffset();
        }
        tempFile.seek(currentOffset);
        return nextOffset;
    }
    
        private void skipKey() throws IOException {
            byte b;
            do {
                b = tempFile.readByte();
            } while(b != 0);
        }
    
        private int readOffset() throws IOException {
            return tempFile.readInt();
        }
        
        public void close () {
        	GlobalUtils.closeCalm(tempFile);
        }
}
