package ru.fizteh.fivt.students.zinnatullin.multifilemap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

	private File path;
	private Table table;
	
	public Database(File path) {
	 	this.path = path;
	}
	
	public boolean createTable(String name){
		return TableProvider.create(path, name);
	}
	
	public boolean useTable(String name){
		table = TableProvider.get(path, name);
		if(table instanceof Table){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean dropTable(String name){
		return TableProvider.drop(path, name);
	}
		
	public String get(String key) throws Exception{
		if(table == null){
			throw new Exception("Table not select");
		}
		String value = null;
		try {
			table.getData(TableProvider.getNDir(key), TableProvider.getNFile(key));
		} catch (IOException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}
		HashMap dirMap = (HashMap)table.data.get(TableProvider.getNDir(key));
		HashMap fileMap = (HashMap)dirMap.get(TableProvider.getNFile(key));
		if(fileMap.containsKey(key)){
			value = (String)fileMap.get(key);
		}
		return value;
	}
	
	public String put(String key, String value) throws Exception{
		if(table == null){
			throw new Exception("Table not select");
		}
		String oldValue = "";
		try {
			table.getData(TableProvider.getNDir(key), TableProvider.getNFile(key));
			HashMap dirMap = (HashMap)table.data.get(TableProvider.getNDir(key));
			HashMap fileMap = (HashMap)dirMap.get(TableProvider.getNFile(key));
			if(fileMap.containsKey(key)){
				oldValue = (String)fileMap.get(key);
			}
			fileMap.put(key, value);
			dirMap.put(TableProvider.getNFile(key), fileMap);
			table.data.put(TableProvider.getNDir(key), dirMap);
			table.saveData(TableProvider.getNDir(key), TableProvider.getNFile(key));
		} catch (Exception ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}
		return oldValue;
	}
	
	public String remove(String key) throws Exception{
		if(table == null){
			throw new Exception("Table not select");
		}
		String oldValue = "";
		try {
			table.getData(TableProvider.getNDir(key), TableProvider.getNFile(key));
			HashMap dirMap = (HashMap)table.data.get(TableProvider.getNDir(key));
			HashMap fileMap = (HashMap)dirMap.get(TableProvider.getNFile(key));
			if(fileMap.containsKey(key)){
				oldValue = (String)fileMap.get(key);
				fileMap.remove(key);
				dirMap.put(TableProvider.getNFile(key), fileMap);
				table.data.put(TableProvider.getNDir(key), dirMap);
			}
			table.saveData(TableProvider.getNDir(key), TableProvider.getNFile(key));
		} catch (Exception ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}
		return oldValue;
	}
	
	public void commit() throws Exception{
		if(table == null){
			throw new Exception("Table not select");
		}
		if(!table.data.isEmpty()){
			for (Iterator it = table.data.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, HashMap> entry = (Map.Entry<String, HashMap>)it.next();
				String nDir = entry.getKey();
				HashMap dirMap = entry.getValue();
				if(!dirMap.isEmpty()){
					for(it = dirMap.entrySet().iterator(); it.hasNext();){
						String nFile = entry.getKey();
						try {
							table.saveData(nDir, nFile);
						} catch (IOException ex) {
							Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			}
		}
	}
}
