package ru.fizteh.fivt.students.zinnatullin.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import ru.fizteh.fivt.storage.strings.Table;

public class DBTable implements Table{

	private String name;
	private File path;
	private HashMap data;
	private int operations = 0;
	
	public DBTable(String name, File path) {
		this.name = name;
		this.path = path;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String get(String key) {
		String nDir = getNDir(key);
		String nFile = getNFile(key);
		if(data == null){
			readData();
		}
		String value = null;
		if(data != null){
			HashMap dirMap = (HashMap)data.get(nDir);
			if(dirMap != null){
				HashMap fileMap = (HashMap)dirMap.get(nFile);
				if(fileMap != null && fileMap.containsKey(key)){
					value = (String)fileMap.get(key);
				}
			}
		}
		return value;
	}

	@Override
	public String put(String key, String value) {
		String oldValue = null;
		String nDir = getNDir(key);
		String nFile = getNFile(key);
		if(data == null){
			readData();
		}
		if(data != null){
			HashMap dirMap = (HashMap)data.get(nDir);
			if(dirMap == null){
				dirMap = new HashMap();
			}	
			HashMap fileMap = (HashMap)dirMap.get(nFile);
			if(fileMap == null){
				fileMap = new HashMap();
			}
			if(fileMap.containsKey(key)){
				oldValue = (String)fileMap.get(key);
			}
			fileMap.put(key, value);
			dirMap.put(nFile, fileMap);
			data.put(nDir, dirMap);
		}
		if(oldValue != null && !oldValue.equals(value)){
			operations++;
		}
		return oldValue;
	}

	@Override
	public String remove(String key) {
		String oldValue = "";
		String nDir = getNDir(key);
		String nFile = getNFile(key);
		if(data == null){
			readData();
		}
		HashMap dirMap = (HashMap)data.get(nDir);
		HashMap fileMap = (HashMap)dirMap.get(nFile);
		if(fileMap.containsKey(key)){
			oldValue = (String)fileMap.get(key);
			fileMap.remove(key);
			dirMap.put(nFile, fileMap);
			data.put(nDir, dirMap);
		}
		operations++;
		return oldValue;
	}

	@Override
	public int size() {
		if(data == null){
			readData();
		}
		int count = 0;
		if(!data.isEmpty()){
			for (Iterator it = data.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, HashMap> entry = (Map.Entry<String, HashMap>)it.next();
				String nDir = entry.getKey();
				HashMap dirMap = entry.getValue();
				if(!dirMap.isEmpty()){
					for(Iterator it1 = dirMap.entrySet().iterator(); it1.hasNext();){
						Map.Entry<String, HashMap> entry1 = (Map.Entry<String, HashMap>)it1.next();
						String nFile = entry1.getKey();
						HashMap value = entry1.getValue();
						count += value.size();
					}
				}
			}
		}
		return count;
	}

	@Override
	public int commit() {
		if(!data.isEmpty()){
			for (Iterator it = data.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, HashMap> entry = (Map.Entry<String, HashMap>)it.next();
				String nDir = entry.getKey();
				HashMap dirMap = entry.getValue();
				if(!dirMap.isEmpty()){
					for(Iterator it1 = dirMap.entrySet().iterator(); it1.hasNext();){
						Map.Entry<String, HashMap> entry1 = (Map.Entry<String, HashMap>)it1.next();
						String nFile = entry1.getKey();
						saveData(nDir, nFile);
					}
				}
			}
		}
		int countOperations = operations;
		operations = 0;
		return countOperations;
	}

	@Override
	public int rollback() {
		readData();
		int countOperations = operations;
		operations = 0;
		return countOperations;
	}
	
	public DBTable readData(){
		data = new HashMap();
		for(File dir : path.listFiles()){
			if(dir.isDirectory()){
				String nDir = dir.getName();
				for(File inputFile : dir.listFiles()){
					if(inputFile.isFile()){
						String nFile = inputFile.getName();
						
						try{
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
								fis.close();
							}
						} catch(Exception ex){
							System.err.println("Error read database data");
						}
					}
				}
			}
		}		

		return this;
	}
	
	public DBTable saveData(String nDir, String nFile){
		File nPath = new File(path, nDir);
		if(!nPath.exists()){
			nPath.mkdir();
		}
		File outputFile = new File(nPath, nFile);
		if(!outputFile.exists()){
			try {
				outputFile.createNewFile();
			} catch (IOException ex) {
				return null;
			}
		}
		try{
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
		} catch(Exception ex){
			return null;
		}
		return this;
	}
	
	public int getOperations(){
		return operations;
	}
	
	public static String getNDir(String key){
		int hash = Math.abs(key.hashCode()%16);
		return new Integer(hash).toString();
	}
	
	public static String getNFile(String key){
		int hash = Math.abs(key.hashCode()/16%16);
		return hash + ".data";
	}
}
