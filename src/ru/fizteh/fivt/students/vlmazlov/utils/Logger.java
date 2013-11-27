package ru.fizteh.fivt.students.vlmazlov.utils;

import java.util.Arrays;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import org.json.*;

public class Logger {

	private final JSONObject callLog;
    private static final String SEPARATOR = System.getProperty("line.separator");

	public Logger() {
		callLog = new JSONObject();
	}

	private void logTimestamp() {
        callLog.put("timestamp",  System.currentTimeMillis());
	}

	private void logTargetClass(Object target) {
	    callLog.put("class", target.getClass());
	}

	private void logMethod(Method method) {
        callLog.put("name", method.getName());
	}

	public void logMethodCall(Method method, Object[] args, Object target) throws Throwable {
        Throwable caught = null;
        Object returnValue = null;

        logTimestamp();
        logTargetClass(target);
        logMethod(method);
        logArguments(args);
    }

    private void logArguments(Object[] args) {

    	IdentityHashMap<Iterable, Boolean> identityHashMap = new IdentityHashMap<Iterable, Boolean>();

    	if (args != null) {
    		callLog.put("arguments", logIterable(Arrays.asList(args), identityHashMap));
    	} else {
    		callLog.put("arguments", new JSONArray());
    	}
    }

    private Object logIterable(Iterable iterable, IdentityHashMap<Iterable, Boolean> identityHashMap) {
    	if (identityHashMap.containsKey(iterable)) {
    		return "cyclic";
    	}

    	identityHashMap.put(iterable, true);

    	JSONArray iterableValues = new JSONArray();
        if (iterable != null) {
            for (Object value : iterable) {
                if (value instanceof Iterable) {
                    iterableValues.put(logIterable((Iterable)value, identityHashMap));
                } else {
                	iterableValues.put(value);
                }
            }
        }

        return iterableValues;
    }

    public void logReturnValue(Object returnValue) {
    	callLog.put("returnValue", returnValue);
    }

    public void logThrown(Throwable thrown) {
    	callLog.put("thrown", thrown.toString());
    }

    //for test purposes
    public JSONObject getResultObject() {
        return callLog; 
    }

    @Override
    public String toString() {
    	return (callLog.toString(2) + SEPARATOR);
    }
}
