package ru.fizteh.fivt.students.krivchansky.filemap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class ReadingUtils {
    
    protected RandomAccessFile tempFile;
    private static int valueShift = -1;
    
    public ReadingUtils(String pathToFile) throws SomethingIsWrongException {
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
            throw new SomethingIsWrongException("Error aqcuired while seeking a file: " + e.getMessage());
        }
    }
    
    public String readKey() throws SomethingIsWrongException {
            try {
                if (tempFile.getFilePointer() >= valueShift) {
                    return null;
                }
                ArrayList<Byte> bytes = new ArrayList<Byte>();
                byte b = tempFile.readByte();
                while(b != 0) {
                    bytes.add(b);
                    b = tempFile.readByte();
                }
                byte[] array = UtilMethods.bytesToArray(bytes);
                return new String(array, UtilMethods.ENCODING);
            } catch (IOException e) {
                throw new SomethingIsWrongException("Error acquired: " + e.getMessage());
            }
        }
    
        public String readValue() throws SomethingIsWrongException {
            int offset = readOffset();
            int nextOffset = readNextOffset();
            long currentOffset;
            try {
                currentOffset = tempFile.getFilePointer();
                tempFile.seek(offset);
                int valueLength = nextOffset-offset;
                byte[] bytes = new byte[valueLength];
                tempFile.read(bytes, 0, valueLength);
                tempFile.seek(currentOffset);
                return new String(bytes, UtilMethods.ENCODING);
            } catch (IOException e) {
                throw new SomethingIsWrongException("Error acquired: " + e.getMessage());
            }
            
        }
    
        public boolean endOfFile() throws SomethingIsWrongException {
            if (tempFile == null) {
                return true;
            }
    
            boolean result = true;
            try {
                result = (tempFile.getFilePointer() == valueShift);
            } catch (IOException e) {
                throw new SomethingIsWrongException("Error aqcuired while reading a file " + e.getMessage());
            }
            return result;
        }
    
    
        private int readNextOffset() throws SomethingIsWrongException {
            try {
                long currentOffset = tempFile.getFilePointer();
                int nextOffset;
                if (readKey() == null) {
                    nextOffset = (int)tempFile.length();
                } else {
                    nextOffset = readOffset();
                }
                tempFile.seek(currentOffset);
                return nextOffset;
            } catch (IOException e) {
                throw new SomethingIsWrongException("Error aqcuired while reading a file: " + e.getMessage());
            }
        }
    
        private void skipKey() throws SomethingIsWrongException {
            byte b;
            do {
                try {
                    b = tempFile.readByte();
                } catch (IOException e) {
                    throw new SomethingIsWrongException("Error aqcuired while reading a file: " + e.getMessage());
                }
            } while(b != 0);
        }
    
        private int readOffset() throws SomethingIsWrongException {
            try {
                return tempFile.readInt();
            } catch (IOException e) {
                throw new SomethingIsWrongException("Error aqcuired while reading a file: " + e.getMessage());
            }
        }
}
