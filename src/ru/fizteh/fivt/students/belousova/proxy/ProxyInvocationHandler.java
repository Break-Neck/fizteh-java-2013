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

    public ProxyInvocationHandler(Writer writer, Object implementation) {
        this.writer = writer;
        this.implementation = implementation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        JSONObject jsonObject = new JSONObject();
        IdentityHashMap<Object, Boolean> objects = new IdentityHashMap<>();

        jsonObject.put("timestamp", System.currentTimeMillis());
        jsonObject.put("class", implementation.getClass().getName());
        jsonObject.put("method", method.getName());
        writeArguments(args, jsonObject, objects);

        Object methodResult = null;

        try {
            methodResult = method.invoke(implementation, args);
            if (!method.getReturnType().getName().equals("void")) {
                writeResult(methodResult, jsonObject, objects);
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

    private void writeArguments(Object[] args, JSONObject jsonObject,
                                IdentityHashMap<Object, Boolean> objects) throws JSONException {
        if (args == null) {
            jsonObject.put("arguments", new JSONArray());
        } else {
            jsonObject.put("arguments", makeJSONArray(Arrays.asList(args), jsonObject, objects));
            objects.clear();
        }
    }

    private void writeResult(Object result, JSONObject jsonObject,
                             IdentityHashMap<Object, Boolean> objects) throws JSONException {
        Object resultValue;
        if (result != null) {
            if (result instanceof Iterable) {
                resultValue = makeJSONArray((Iterable) result, jsonObject, objects);
            } else {
                resultValue = result;
            }
        } else {
            resultValue = JSONObject.NULL;
        }
        jsonObject.put("returnValue", resultValue);
        objects.clear();
    }

    private JSONArray makeJSONArray(Iterable collection, JSONObject jsonObject,
                                    IdentityHashMap<Object, Boolean> objects) {
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
                jsonArray.put(makeJSONArray((Iterable) value, jsonObject, objects));
                continue;
            }

            jsonArray.put(value);
        }
        return jsonArray;
    }
}
