package ru.fizteh.fivt.students.zinnatullin.multifilemap;

import java.io.File;

public class TableProvider {
	
	public static Table get(File path, String name){
		Table table = null;
		
		File tablePath = new File(path, name);
		if(tablePath.exists() && tablePath.isDirectory()){
			return new Table(path, name);
		}
		
		return table;
	}
	
	public static boolean create(File path, String name) {
		Table table = null;

		File tablePath = new File(path, name);
		if (tablePath.exists() && tablePath.isDirectory()) {
			return false;
		}
		if (tablePath.exists()) {
			tablePath.delete();
		}
		tablePath.mkdir();
		return true;
	}
	
	public static boolean drop(File path, String name) {
		Table table = null;

		File tablePath = new File(path, name);
		if (tablePath.exists() && tablePath.isDirectory()) {
			tablePath.delete();
			return true;
		}
		if (tablePath.exists()) {
			tablePath.delete();
		}
		tablePath.mkdir();
		return false;
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
