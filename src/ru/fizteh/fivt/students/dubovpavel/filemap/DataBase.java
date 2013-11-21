package ru.fizteh.fivt.students.dubovpavel.filemap;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class DataBase<V> implements DataBaseHandler<String, V> {
    protected File savingEndPoint;
    protected static final Charset charset = StandardCharsets.UTF_8;
    protected static final int MAXLENGTH = 1 << 20;
    private Serial<V> builder;
    protected HashMap<String, V> dict = new HashMap<String, V>();

    private void checkValid() {
        if(savingEndPoint == null) {
            throw new RuntimeException("DataBase pointer was null");
        }
    }

    protected void generateLoadingError(String error, String message, boolean acc) throws DataBaseException {
        dict = new HashMap<>();
        throw new DataBaseException(String.format("Conformity loading: %s: %s. Empty database applied", error, message), acc);
    }

    public void open() throws DataBaseException {
        checkValid();
        try(DataInputStream db = new DataInputStream(new FileInputStream(savingEndPoint))) {
            while(true) {
                int keyLength;
                try {
                    keyLength = db.readInt();
                } catch (EOFException e) {
                    break;
                }
                if(keyLength <= 0 || keyLength > MAXLENGTH) {
                    throw new DataBaseException(String.format("Key length must be in [1; %d]", MAXLENGTH));
                }
                int valueLength = db.readInt();
                if(valueLength <= 0 || valueLength > MAXLENGTH) {
                    throw new DataBaseException(String.format("Value length must be in [1; %d]", MAXLENGTH));
                }
                byte[] keyBuffer = new byte[keyLength];
                db.readFully(keyBuffer, 0, keyLength);
                String key = new String(keyBuffer, charset);
                byte[] valueBuffer = new byte[valueLength];
                db.readFully(valueBuffer, 0, valueLength);
                String value = new String(valueBuffer, charset);
                dict.put(key, builder.deserialize(value));
            }
        } catch (IOException e) {
            generateLoadingError("IOException", e.getMessage(), false);
        } catch (DataBaseException e) {
            generateLoadingError("DataBaseException", e.getMessage(), false);
        } catch (Serial.SerialException e) {
            generateLoadingError("SerialException (deserialization)", e.getMessage(), false);
        } catch (ParseException e) {
            generateLoadingError("ParseException (deserialization)", e.getMessage(), false);
        }
    }

    public DataBase(File path, Serial<V> builder) {
        savingEndPoint = path;
        this.builder = builder;
    }

    protected DataBase(Serial<V> builder) {
        savingEndPoint = null;
        this.builder = builder;
    }

    public void save() throws DataBaseException {
        checkValid();
        try(DataOutputStream db = new DataOutputStream(new FileOutputStream(savingEndPoint))) {
            for(Map.Entry<String, V> entry: dict.entrySet()) {
                byte[] key = entry.getKey().getBytes(charset);
                byte[] value = builder.serialize(entry.getValue()).getBytes(charset);
                db.writeInt(key.length);
                db.writeInt(value.length);
                db.write(key);
                db.write(value);
            }
        } catch(IOException e) {
            throw new DataBaseException(String.format("Conformity saving: IOException: %s", e.getMessage()));
        } catch (Serial.SerialException e) {
            throw new DataBaseException(String.format("Conformity saving: SerialException (serialization): %s", e.getMessage()));
        }
    }

    public V put(String key, V value) {
        if(dict.containsKey(key)) {
            V old = dict.get(key);
            dict.put(key, value);
            return old;
        } else {
            dict.put(key, value);
            return null;
        }
    }

    public V remove(String key) {
        if(dict.containsKey(key)) {
            V removing = dict.get(key);
            dict.remove(key);
            return removing;
        } else {
            return null;
        }
    }

    public V get(String key) {
        if(dict.containsKey(key)) {
            return dict.get(key);
        } else {
            return null;
        }
    }
}
