package ru.fizteh.fivt.students.zinnatullin.filemap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

	private String path;
	private String file;
	private HashMap<String, String> data;
	
	public Database(String path, String file) {
	 	this.path = path;
		this.file = file;
		data = new HashMap();
		//data.put("name", "van");
		//data.put("surname", "Ivanov");
	}
	
	public Database setFilename(String path, String file){
		this.path = path;
		this.file = file;
		return this;
	}
	
	public Database getData() throws FileNotFoundException, IOException{
		
		File inputFile = new File(path, file);
		FileInputStream fis = new FileInputStream(inputFile);

		while(fis.available() > 0){
			byte[] keyLenBytes = new byte[4];
			fis.read(keyLenBytes);
			if(keyLenBytes.length == 0){
				continue;
			}
			int keyLen = Integer.parseInt(new String(keyLenBytes, "UTF-8"), 16);
			
			byte[] valueLenBytes = new byte[4];
			fis.read(valueLenBytes);
			if(valueLenBytes.length == 0){
				continue;
			}
			int valueLen = Integer.parseInt(new String(valueLenBytes, "UTF-8"), 16);
			
			byte[] keyBytes = new byte[keyLen];
			fis.read(keyBytes);
			if(keyBytes.length == 0){
				continue;
			}
			String key = new String(keyBytes, "UTF-8");
			
			byte[] valueBytes = new byte[valueLen];
			fis.read(valueBytes);
			if(valueBytes.length == 0){
				continue;
			}
			String value = new String(valueBytes, "UTF-8");
			
			data.put(key, value);
		}
		
		fis.close();
		return this;
	}
	
	public Database saveData() throws FileNotFoundException, IOException{
		File outputFile = new File(path, file);
		FileOutputStream fos = new FileOutputStream(outputFile);

		for (Map.Entry<String, String> entry : data.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			
			byte[] keyLenBytes;
			String keyLenHex = "";
			int keyLenHexSize = Long.toHexString(key.length()).getBytes().length;
			if(keyLenHexSize < 4){
				for (int i = 0; i < (4 - keyLenHexSize); i++) {
					keyLenHex += Long.toHexString(0);
				}
			}
			keyLenHex += Long.toHexString(key.length());
			keyLenBytes = keyLenHex.getBytes();
			fos.write(keyLenBytes);
			
			byte[] valueLenBytes;
			String valueLenHex = "";
			int valueLenHexSize = Long.toHexString(value.length()).getBytes().length;
			if(valueLenHexSize < 4){
				for (int i = 0; i < (4 - valueLenHexSize); i++) {
					valueLenHex += Long.toHexString(0);
				}
			}
			valueLenHex += Long.toHexString(value.length());
			valueLenBytes = valueLenHex.getBytes();
			fos.write(valueLenBytes);
						
			byte[] keyBytes;
			keyBytes = key.getBytes();
			fos.write(keyBytes);
					
			byte[] valueBytes;
			valueBytes = value.getBytes();
			fos.write(valueBytes);
		}
		
		fos.flush();
		fos.close();
		return this;
	}
		
	public String get(String key){
		String value = "";
		try {
			this.getData();
		} catch (IOException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}
		if(data.containsKey(key)){
			value = data.get(key);
		}
		return value;
	}
	
	public String put(String key, String value){
		String oldValue = "";
		try {
			this.getData();
			if(data.containsKey(key)){
				oldValue = data.get(key);
			}
			data.put(key, value);
			this.saveData();
		} catch (Exception ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}
		return oldValue;
	}
	
	public String remove(String key){
		String oldValue = "";
		try {
			this.getData();
			if(data.containsKey(key)){
				oldValue = data.get(key);
				data.remove(key);
			}
			this.saveData();
		} catch (Exception ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}
		return oldValue;
	}
}
