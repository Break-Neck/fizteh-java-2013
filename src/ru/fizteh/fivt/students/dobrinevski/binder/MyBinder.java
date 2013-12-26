package ru.fizteh.fivt.students.dobrinevski.binder;

import org.json.*;
import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.*;
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
            recDec(answer, jsonObject);
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

    private void recDec(Object obj, JSONObject jsonObject) {
        if (obj == null) {
            throw new IllegalArgumentException("bad constructor");
        }
        try {
            for (Object i : jsonObject.keySet()) {
                if (i == null) {
                    throw new IllegalArgumentException("key is null");
                }
                if (i.toString().equals("")) {
                    throw new IllegalArgumentException("key is empty");
                }
                if (jsonObject.get(i.toString()) == null) {
                    continue;
                }
                Field field = null;
                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field f : fields) {
                    if (!f.isAnnotationPresent(DoNotBind.class)) {
                        if (f.getName().equals(i.toString())) {
                            if (field == null) {
                                field = f;
                            } else {
                                throw new IllegalArgumentException("2 field with equal name");
                            }
                        }
                        if (f.isAnnotationPresent(Name.class)) {
                            if (f.getAnnotation(Name.class).value().equals(i.toString())) {
                                if (field == null) {
                                    field = f;
                                } else {
                                    throw new IllegalArgumentException("2 field with equal name");
                                }
                            }
                        }
                    }
                }

                if (field == null) {
                    throw new IllegalArgumentException("No field with name " + i.toString());
                }

                field.setAccessible(true);
                if (field.getType().isPrimitive()) {
                    if (field.getType().getSimpleName().equals("int")) {
                        field.set(obj, Integer.parseInt(jsonObject.get(i.toString()).toString()));
                    } else if (field.getType().getSimpleName().equals("double")) {
                        field.set(obj, Double.parseDouble(jsonObject.get(i.toString()).toString()));
                    } else if (field.getType().getSimpleName().equals("float")) {
                        field.set(obj, Float.parseFloat(jsonObject.get(i.toString()).toString()));
                    } else if (field.getType().getSimpleName().equals("long")) {
                        field.set(obj, Long.parseLong(jsonObject.get(i.toString()).toString()));
                    } else if (field.getType().getSimpleName().equals("boolean")) {
                        field.set(obj, Boolean.parseBoolean(jsonObject.get(i.toString()).toString()));
                    } else if (field.getType().getSimpleName().equals("short")) {
                        field.set(obj, Short.parseShort(jsonObject.get(i.toString()).toString()));
                    } else if (field.getType().getSimpleName().equals("byte")) {
                        field.set(obj, Byte.parseByte(jsonObject.get(i.toString()).toString()));
                    } else if (field.getType().getSimpleName().equals("char")) {
                        field.set(obj, jsonObject.get(i.toString()).toString().charAt(0));
                    }
                    continue;
                }

                if (field.getType().getSimpleName().equals("String")) {
                    field.set(obj, jsonObject.get(i.toString()).toString());
                    continue;
                }

                if (field.isEnumConstant()) {
                    field.set(obj, Enum.valueOf(((Class<Enum>) field.getType()),
                            jsonObject.get(i.toString()).toString()).name());
                    continue;
                }

                recDec(field.get(obj), (JSONObject) jsonObject.get(i.toString()));
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("illegal access " + e.getMessage());
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
