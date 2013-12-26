package ru.fizteh.fivt.students.zinnatullin.multifilemap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Table {
	
	private String name;
	private File path;
	public HashMap data;
	//public HashMap<String, String> data;
	
	public Table(File path, String name){
		this.name = name;
		this.path = new File(path, name);
		
		data = new HashMap();
	}
	
	public Table getData(String nDir, String nFile) throws FileNotFoundException, IOException{
		
		File nPath = new File(path, nDir);
		if(!nPath.exists()){
			nPath.mkdir();
		}
		File inputFile = new File(nPath, nFile);
		if(!inputFile.exists()){
			inputFile.createNewFile();
		}
		FileInputStream fis = new FileInputStream(inputFile);
		
		HashMap<String, String> data = new HashMap();
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
		
		if(!this.data.containsKey(nDir)){
			HashMap filemap = new HashMap();
			filemap.put(nFile, data);
			this.data.put(nDir, filemap);
		} else {
			HashMap dirMap = (HashMap)this.data.get(nDir);
			dirMap.put(nFile, data);
			this.data.put(nDir, dirMap);
		}
		fis.close();
		return this;
	}
	
	public Table saveData(String nDir, String nFile) throws FileNotFoundException, IOException{
		File nPath = new File(path, nDir);
		if(!nPath.exists()){
			nPath.mkdir();
		}
		File outputFile = new File(nPath, nFile);
		if(!outputFile.exists()){
			outputFile.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(outputFile);

		HashMap dirMap = (HashMap)data.get(nDir);
		HashMap fileMap = (HashMap)dirMap.get(nFile);
		if(!fileMap.isEmpty()){
			for (Iterator it = fileMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>)it.next();
				String key = entry.getKey();
				String value = entry.getValue();
				byte[] keyLenBytes;
				String keyLenHex = "";
				int keyLenHexSize = Long.toHexString(key.getBytes().length).getBytes().length;
				if(keyLenHexSize < 4){
					for (int i = 0; i < (4 - keyLenHexSize); i++) {
						keyLenHex += Long.toHexString(0);
					}
				}
				keyLenHex += Long.toHexString(key.getBytes().length);
				keyLenBytes = keyLenHex.getBytes();
				fos.write(keyLenBytes);
				byte[] valueLenBytes;
				String valueLenHex = "";
				int valueLenHexSize = Long.toHexString(value.getBytes().length).getBytes().length;
				if(valueLenHexSize < 4){
					for (int i = 0; i < (4 - valueLenHexSize); i++) {
						valueLenHex += Long.toHexString(0);
					}
				}
				valueLenHex += Long.toHexString(value.getBytes().length);
				valueLenBytes = valueLenHex.getBytes();
				fos.write(valueLenBytes);
				byte[] keyBytes;
				keyBytes = key.getBytes();
				fos.write(keyBytes);
				byte[] valueBytes;
				valueBytes = value.getBytes();
				fos.write(valueBytes);
			}
		}
		fos.flush();
		fos.close();
		return this;
	}
}
