package ru.fizteh.fivt.students.krivchansky.storable;

import java.util.ArrayList;
import java.util.List;

public class TableInfo {
	private String name;
	private List<Class<?>> typesOfColumns;
	
	public TableInfo(String name) {
		this.name = name;
		this.typesOfColumns = new ArrayList<Class<?>>();
	}
	
	public void addColumn(Class<?> columnType) {
		typesOfColumns.add(columnType);
	}
	
	public List<Class<?>> getColumnsTypes() {
		return typesOfColumns;
	}
	
	public String getName() {
		return name;
	}
	
}
