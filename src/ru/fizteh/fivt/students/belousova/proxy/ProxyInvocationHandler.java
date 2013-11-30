package ru.fizteh.fivt.students.belousova.proxy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.IdentityHashMap;

public class ProxyInvocationHandler implements InvocationHandler {
    private final Writer writer;
    private final Object implementation;
    private JSONObject jsonObject = new JSONObject();
    private IdentityHashMap<Object, Boolean> objects = new IdentityHashMap<>();

    public ProxyInvocationHandler(Writer writer, Object implementation) {
        this.writer = writer;
        this.implementation = implementation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        jsonObject.put("timestamp", System.currentTimeMillis());
        jsonObject.put("class", implementation.getClass().getName());
        jsonObject.put("method", method.getName());
        writeArguments(args);

        Object methodResult = null;

        try {
            methodResult = method.invoke(implementation, args);
            if (!method.getReturnType().getName().equals("void")) {
                jsonObject.put("returnValue", methodResult);
            }
        } catch (InvocationTargetException e) {
            jsonObject.put("thrown", e.getTargetException().toString());
            throw e.getTargetException();
        } catch (Exception e) {
            //do nothing
        } finally {
            if (method.getDeclaringClass() != Object.class) {
                writer.write(jsonObject.toString(2) + "\n");
            }
        }
        return methodResult;
    }

    private void writeArguments(Object[] args) throws JSONException {
        if (args == null) {
            jsonObject.put("arguments", new JSONArray());
        } else {
            jsonObject.put("arguments", makeJSONArray(Arrays.asList(args)));
        }
    }

    private JSONArray makeJSONArray(Iterable collection) {
        JSONArray jsonArray = new JSONArray();
        for (Object value : collection) {
            if (value == null) {
                jsonArray.put(value);
                continue;
            }

            if (value.getClass().isArray()) {
                jsonArray.put(value.toString());
                continue;
            }

            boolean isContainer = false;
            boolean isEmpty = false;

            if (value instanceof Iterable) {
                isContainer = true;
                isEmpty = !((Iterable) value).iterator().hasNext();
            }

            if (objects.containsKey(value) && isContainer && !isEmpty) {
                jsonArray.put("cyclic");
                continue;
            }

            objects.put(value, true);

            if (isContainer) {
                jsonArray.put(makeJSONArray((Iterable) value));
                continue;
            }

            jsonArray.put(value);
        }
        return jsonArray;
    }
}
