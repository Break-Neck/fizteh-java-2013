package src.ru.fizteh.fivt.students.krivchansky.storable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum StoreableTypes {
	
	INTEGER("int", Integer.class) {
		public Object ParseValue(String arg) {
			return Integer.parseInt(arg);
		}
	},
	
	LONG("long", Long.class) {
		public Object ParseValue(String arg) {
			return Long.parseLong(arg);
		}
	},
	
	FLOAT("float", Float.class) {
		public Object ParseValue(String arg) {
			return Float.parseFloat(arg);
		}
	},
	
	DOUBLE("double", Double.class) {
		public Object ParseValue(String arg) {
			return Double.parseDouble(arg);
		}
	},
	
	STRING("string", String.class) {
		public Object ParseValue(String arg) {
			return arg;
		}
	},
	
	BYTE("byte", Byte.class) {
		public Object ParseValue(String arg) {
			return Byte.parseByte(arg);
		}
	},
	
	BOOLEAN("boolean", Boolean.class) {
		public Object ParseValue(String arg) {
			return Boolean.parseBoolean(arg);
		}
	};
	
	private final String name;
	private final Class<?> type;
	
	private StoreableTypes(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}
	
	private static final Map<String, StoreableTypes> typesByName;
	private static final Map<Class<?>, StoreableTypes> typesByClass;
	
	static {
		HashMap<String, StoreableTypes> tempByName = new HashMap<>();
		HashMap<Class<?>, StoreableTypes> tempByClass = new HashMap<>();
		for (StoreableTypes value : values()) {
			tempByName.put(value.name, value);
			tempByClass.put(value.type, value);
		}
		typesByName = Collections.unmodifiableMap(tempByName);
		typesByClass = Collections.unmodifiableMap(tempByClass);
	}
	public static Class<?> getTypeByName(String name) {
		StoreableTypes formatter = typesByName.get(name);
		if (formatter == null) {
			throw new IllegalArgumentException("unknown type");
		}
		return formatter.type;
	}
	
	public static String getSimpleName(Class<?> type) {
		StoreableTypes formatter = typesByClass.get(type);
		if (formatter == null) {
			throw new IllegalArgumentException("unknown format");
		}
		return formatter.name;
	}
	
	public abstract Object ParseValue(String string);
	
	public static Object parseByClass(String string, Class<?> type) {
		StoreableTypes formatter = typesByClass.get(type);
		if (formatter == null) {
			throw new IllegalArgumentException ("unknown type");
		}
		return formatter.ParseValue(string);
	}
} 
