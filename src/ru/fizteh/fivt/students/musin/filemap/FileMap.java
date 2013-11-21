package ru.fizteh.fivt.students.musin.filemap;

import ru.fizteh.fivt.storage.structured.Storeable;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class FileMap {
    File location;
    HashMap<String, Storeable> map;
    final int maxLength;

    public FileMap(File location) {
        if (location == null) {
            throw new IllegalArgumentException("Null location");
        }
        this.location = location;
        map = new HashMap<>();
        maxLength = 1 << 24;
    }

    public void clear() {
        map.clear();
    }

    public boolean empty() {
        return map.isEmpty();
    }

    public File getFile() {
        return location;
    }

    public int size() {
        return map.size();
    }

    public String[] getKeysList() {
        String[] result = new String[map.size()];
        int i = 0;
        for (String entry : map.keySet()) {
            result[i++] = entry;
        }
        return result;
    }

    private int readBytes(DataInputStream input, int bytes, byte[] buffer) throws IOException {
        int len = 0;
        while (len != bytes) {
            int k = input.read(buffer, len, bytes - len);
            if (k == -1) {
                return len;
            }
            len += k;
        }
        return len;
    }

    /**
     * @throws RuntimeException on fail
     */
    public void loadFromDisk(MultiFileMap table, FileMapProvider tableProvider) throws ParseException, IOException {
        map.clear();
        if (!location.getParentFile().exists() || !location.getParentFile().isDirectory()) {
            throw new RuntimeException("Unable to create a file, directory doesn't exist");
        }
        if (!location.exists()) {
            return;
        }
        if (location.exists() && !location.isFile()) {
            throw new RuntimeException(String.format("%s is not a file", location.getName()));
        }
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(location))) {
            byte[] buffer;
            ByteBuffer cast;
            while (true) {
                buffer = new byte[4];
                int bytesRead = readBytes(inputStream, 4, buffer);
                if (bytesRead == 0) {
                    break;
                }
                if (bytesRead != 4) {
                    throw new IOException("Database loading failed: Wrong key length format");
                }
                cast = ByteBuffer.wrap(buffer);
                int keyLength = cast.getInt();
                bytesRead = readBytes(inputStream, 4, buffer);
                if (bytesRead != 4) {
                    throw new IOException("Database loading failed: Wrong value length format");
                }
                cast = ByteBuffer.wrap(buffer);
                int valueLength = cast.getInt();
                if (keyLength > maxLength || valueLength > maxLength) {
                    throw new IOException("Database loading failed: Field length too big");
                }
                if (keyLength <= 0 || valueLength <= 0) {
                    throw new IOException("Database loading failed: Field length should be positive");
                }
                buffer = new byte[keyLength];
                bytesRead = readBytes(inputStream, keyLength, buffer);
                if (bytesRead != keyLength) {
                    throw new IOException("Database loading failed: Wrong key length");
                }
                String key = new String(buffer, StandardCharsets.UTF_8);
                buffer = new byte[valueLength];
                bytesRead = readBytes(inputStream, valueLength, buffer);
                if (bytesRead != valueLength) {
                    throw new IOException("Database loading failed: Wrong value length");
                }
                String serializedValue = new String(buffer, StandardCharsets.UTF_8);
                Storeable value = tableProvider.deserialize(table, serializedValue);
                map.put(key, value);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @throws RuntimeException on fail
     */
    public void writeToDisk(MultiFileMap table, FileMapProvider tableProvider) throws IOException {
        if (location.exists() && location.isDirectory()) {
            throw new RuntimeException("Database can't be written to the specified location");
        }
        try {
            if (!location.exists()) {
                if (!location.createNewFile()) {
                    throw new RuntimeException("Database can't be written to the specified location");
                }
            }
        } catch (IOException e) {
            throw new IOException("Error creating the file", e);
        }
        try (FileOutputStream outputStream = new FileOutputStream(location)) {
            for (Map.Entry<String, Storeable> entry : map.entrySet()) {
                byte[] key = entry.getKey().getBytes(StandardCharsets.UTF_8);
                byte[] value = tableProvider.serialize(table, entry.getValue()).getBytes(StandardCharsets.UTF_8);
                outputStream.write(ByteBuffer.allocate(4).putInt(key.length).array());
                outputStream.write(ByteBuffer.allocate(4).putInt(value.length).array());
                outputStream.write(key);
                outputStream.write(value);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File was not found", e);
        }
    }

    public Storeable put(String key, Storeable value) {
        return map.put(key, value);
    }

    public Storeable get(String key) {
        return map.get(key);
    }

    public Storeable remove(String key) {
        return map.remove(key);
    }
}
