package ru.fizteh.fivt.students.dobrinevski.binder;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;
import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.IdentityHashMap;

public class MyBinder<T> implements Binder<T> {
    Class<T> tClass;
    public final Integer grey;
    public final Integer black;
    public MyBinder(Class<T> clazz) {
        grey = -1;
        black = 1;
        tClass = clazz;
    }

    public T deserialize(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input is null");
        }
        JSONObject jsonObject = new JSONObject(new JSONTokener(input));

        try {
            T answer = tClass.getDeclaredConstructor().newInstance();
            recDec(answer, jsonObject, tClass);
            return answer;
        }   catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("There is no declared constructor in " + tClass.getName());
        }   catch (SecurityException e) {
            throw new IllegalArgumentException("SecurityException in " + tClass.getName() + " : " + e.toString());
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Something bad in your constructor : " + e.getMessage());
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Class is abstract");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot get access to");
        }
    }

    private void recDec(Object obj, JSONObject jsonObject, Class<?> clazz) {
        String name;
        for (Field one : obj.getClass().getDeclaredFields()) {
            one.setAccessible(true);
            try {
                if (one.isAnnotationPresent(DoNotBind.class)) {
                    continue;
                }

                if (one.isAnnotationPresent(Name.class)) {
                    name = one.getAnnotation(Name.class).value();
                } else {
                    name = one.getName();
                }
                if (name == null || name.equals("") || name.contains("\n")) {
                    throw new IllegalArgumentException("Field name is incorrect");
                }

                Object got = jsonObject.get(name);
                if (got == null) {
                    one.set(obj, null);
                }
                if (one.getType().isPrimitive()) {
                    if (one.getType().getSimpleName().equals("int")) {
                        one.set(obj, Integer.parseInt(got.toString()));
                    } else if (one.getType().getSimpleName().equals("double")) {
                        one.set(obj, Double.parseDouble(got.toString()));
                    } else if (one.getType().getSimpleName().equals("float")) {
                        one.set(obj, Float.parseFloat(got.toString()));
                    } else if (one.getType().getSimpleName().equals("long")) {
                        one.set(obj, Long.parseLong(got.toString()));
                    } else if (one.getType().getSimpleName().equals("boolean")) {
                        one.set(obj, Boolean.parseBoolean(got.toString()));
                    } else if (one.getType().getSimpleName().equals("short")) {
                        one.set(obj, Short.parseShort(got.toString()));
                    } else if (one.getType().getSimpleName().equals("byte")) {
                        one.set(obj, Byte.parseByte(got.toString()));
                    } else if (one.getType().getSimpleName().equals("char")) {
                        one.set(obj, got.toString().charAt(0));
                    }
                    continue;
                }
                if (one.getType().getSimpleName().equals("String")) {
                    one.set(obj, got);
                    continue;
                }

                if (one.getType().isEnum()) {
                    one.set(obj, Enum.valueOf((Class<Enum>) one.getType(), got.toString()));
                    continue;
                }

                if (one.get(obj) == null) {
                    try {
                        one.set(obj, one.getType().newInstance());
                    } catch (InstantiationException e) {
                        throw new IllegalArgumentException("Cannot create");
                    }
                }
                recDec(one.get(obj), (JSONObject) got, one.getType());
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("failed in serialise :" + e.getMessage());
            } catch (JSONException e) {
                throw new IllegalArgumentException("json fail: " + e.getMessage());
            }
        }
    }

    public void serialize(T value, OutputStream output) throws IOException {
        if (value == null || output == null) {
            throw new IllegalArgumentException("input is null");
        }
        try (OutputStreamWriter oStreamWriter = new OutputStreamWriter(output, "UTF-8")) {
            JSONWriter jsonWriter = new JSONWriter(oStreamWriter);
            IdentityHashMap<Object, Integer> idHashMap = new IdentityHashMap<Object, Integer>();
            recDraw(value, idHashMap, jsonWriter);
        }
    }

    private void recDraw(Object obj, IdentityHashMap<Object, Integer> idHashMap, JSONWriter jsonWriter) {
        jsonWriter.object();
        String name;
        for (Field one : obj.getClass().getDeclaredFields()) {
            one.setAccessible(true);
            try {
                if (one.isAnnotationPresent(DoNotBind.class)) {
                    continue;
                }

                if (one.isAnnotationPresent(Name.class)) {
                    name = one.getAnnotation(Name.class).value();
                } else {
                    name = one.getName();
                }
                if (name == null || name.equals("") || name.contains("\n")) {
                    throw new IllegalArgumentException("Field name is incorrect");
                }
                jsonWriter.key(name);

                Object objBuf = one.get(obj);
                if (objBuf == null) {
                    jsonWriter.value(null);
                    continue;
                }

                if (one.getType().isPrimitive()) {
                    jsonWriter.value(objBuf.toString());
                    continue;
                }

                if (one.getType().getSimpleName().equals("String")) {
                    jsonWriter.value(objBuf.toString());
                    continue;
                }

                if (one.isEnumConstant()) {
                    jsonWriter.value(((Enum) objBuf).name());
                    continue;
                }

                if (grey.equals(idHashMap.get(objBuf))) {
                    throw new IllegalStateException("Cycle reference");
                }
                idHashMap.put(objBuf, grey);

                recDraw(objBuf, idHashMap, jsonWriter);
                idHashMap.put(objBuf, black);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("failed in serialise :" + e.getMessage());
            } catch (JSONException e) {
                throw new IllegalArgumentException("json fail: " + e.getMessage());
            }
        }
        jsonWriter.endObject();
    }
}