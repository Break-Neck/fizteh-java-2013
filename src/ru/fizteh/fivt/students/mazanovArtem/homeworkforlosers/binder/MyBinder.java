package ru.fizteh.fivt.students.mazanovArtem.homeworkforlosers.binder;

import org.json.JSONException;
import org.json.JSONObject;
import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.IdentityHashMap;

public class MyBinder<T> implements Binder<T> {
    IdentityHashMap<Object, Object> cyc;
    Class<T> clazz;
    boolean elementaryClass;
    boolean isEnum;


    public MyBinder(Class<T> tmpclazz) {
        if (tmpclazz == null) {
            throw new IllegalArgumentException("class is null");
        }
        cyc = new IdentityHashMap<>();
        clazz = tmpclazz;
        isEnum = false;
        if (isElementary(tmpclazz)) {
            throw new IllegalArgumentException("class can't be serialized/deserialized");
        } else {
            Constructor[] cons = tmpclazz.getConstructors();
            if (cons.length == 0) {
                throw new IllegalArgumentException("Class haven't public constructor");
            } else {
                boolean isEmptyCon = false;
                for (Constructor con : cons) {
                    if (con.getParameterTypes().length == 0) {
                        isEmptyCon = true;
                    }
                }
                if (!isEmptyCon) {
                    throw new IllegalArgumentException("Class haven't empty constructor");
                }
            }
            if (tmpclazz.isArray()) {
                throw new IllegalArgumentException("class is array");
            }
            Field[] fields = tmpclazz.getDeclaredFields();
            if (fields.length == 0) {
                throw new IllegalArgumentException("field haven't fields");
            }
            HashMap<String, Boolean> tmpSet = new HashMap<>();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(DoNotBind.class)) {
                    if (!field.isAnnotationPresent(Name.class)) {
                        if (tmpSet.put(field.getName(), true) != null) {
                            throw new IllegalArgumentException("bad field name");
                        }
                    } else {
                        if (tmpSet.put(field.getAnnotation(Name.class).value(), true) != null) {
                            throw new IllegalArgumentException("bad field name");
                        }
                    }
                }
                if (!isElementary(field.getType())) {
                    if (!cyc.containsKey(field.getType())) {
                        cyc.put(field.getType(), true);
                        checkClass(field.getType());
                        cyc.remove(field.getType());
                    }
                }
            }
        }
    }

    public void checkClass(Class<?> obj) {
        Constructor[] cons = obj.getConstructors();
        if (cons.length == 0) {
            throw new IllegalArgumentException("Class haven't public constructor");
        } else {
            boolean isEmptyCon = false;
            for (Constructor con : cons) {
                if (con.getParameterTypes().length == 0) {
                    isEmptyCon = true;
                }
            }
            if (!isEmptyCon) {
                throw new IllegalArgumentException("Class haven't empty constructor");
            }
        }
        if (obj.isArray()) {
            throw new IllegalArgumentException("class is array");
        }
        Field[] fields = obj.getDeclaredFields();
        if (fields.length == 0) {
            throw new IllegalArgumentException("field haven't fields");
        }
        HashMap<String, Boolean> tmpSet = new HashMap<>();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(DoNotBind.class)) {
                if (!field.isAnnotationPresent(Name.class)) {
                    if (tmpSet.put(field.getName(), true) != null) {
                        throw new IllegalArgumentException("bad field name");
                    }
                } else {
                    if (tmpSet.put(field.getAnnotation(Name.class).value(), true) != null) {
                        throw new IllegalArgumentException("bad field name");
                    }
                }
            }
            if (!isElementary(field.getType())) {
                if (!cyc.containsKey(field.getType())) {
                    cyc.put(field.getType(), true);
                    checkClass(field.getType());
                    cyc.remove(field.getType());
                }

            }
        }
    }

    public String giveString(InputStream in) throws IOException {
        InputStreamReader is = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(is);
        String read = br.readLine();
        while (read != null) {
            sb.append(read);
            read = br.readLine();
        }
        try {
            is.close();
        } finally {
            br.close();
        }
        return sb.toString();
    }

    public T deserialize(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("Input is null");
        }
        T result;
        String str = giveString(input);
        if (elementaryClass) {
            result = (T) getValueFromString(clazz, str);
            return result;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(str);
        } catch (JSONException e) {
            throw new IllegalArgumentException("wrong input format");
        }
        try {
            result = clazz.getConstructor().newInstance();
            deserialize2(result, jsonObject);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class haven't constructor");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Access denied");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Class is abstract");
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Invalid constructor");
        } catch (SecurityException e) {
            throw new IllegalArgumentException("Security exception : " + e.getMessage());
        }
        return result;
    }

    public void deserialize2(T result, JSONObject jsonObject) {
        if (jsonObject.keySet() == null) {
            throw new IllegalArgumentException("keys is null");
        }
        for (Object key : jsonObject.keySet()) {
            if (key == null) {
                throw new IllegalArgumentException("key is null");
            }
            if (key.toString().equals("")) {
                throw new IllegalArgumentException("key is empty");
            }
            if (jsonObject.get(key.toString()) == null) {
                continue;
            }
            Field[] fields = result.getClass().getDeclaredFields();
            if (fields.length == 0) {
                throw new IllegalArgumentException("Class haven't fields");
            }
            boolean fieldExist = false;
            Field needField = null;
            for (Field field : fields) {
                if (!field.isAnnotationPresent(DoNotBind.class)) {
                    if (field.isAnnotationPresent(Name.class)) {
                        if (field.getAnnotation(Name.class).value().equals(key.toString())) {
                            needField = field;
                            fieldExist = true;
                            break;
                        }
                    } else {
                        if (field.getName().equals(key.toString())) {
                            needField = field;
                            fieldExist = true;
                            break;
                        }
                    }
                }
            }
            if (!fieldExist) {
                throw new IllegalArgumentException("No such field : " + key.toString());
            }
            needField.setAccessible(true);
            try {
                if (isElementary(needField.getType())) {
                    if (needField.isEnumConstant()) {
                        needField.set(result, Enum.valueOf(Enum.class,
                                jsonObject.get(key.toString()).toString()).name());
                    } else if (needField.getType().equals(String.class)) {
                        needField.set(result, jsonObject.get(key.toString()).toString());
                    } else {
                        needField.set(result, getValueFromString(needField.getType(),
                                jsonObject.get(key.toString()).toString()));
                    }
                } else {
                    if (needField.get(result) == null) {
                        if (!jsonObject.isNull(key.toString())) {
                            try {
                                needField.set(result, needField.getType().newInstance());
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Something is bad:(");
                            }
                            deserialize2((T) needField.get(result), jsonObject.getJSONObject(key.toString()));
                        }
                    } else {
                        if (!jsonObject.isNull(key.toString())) {
                            deserialize2((T) needField.get(result), jsonObject.getJSONObject(key.toString()));
                        } else {
                            needField.set(result, null);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Access denied : " + e.getMessage());
            }
        }
    }

    public void serialize(T value, OutputStream output) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output is null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Class is empty");
        }
        if (isElementary(value.getClass())) {
            throw new IllegalArgumentException("class is primitive");
        }
        Field[] fields = value.getClass().getDeclaredFields();
        JSONObject result = new JSONObject();
        for (Field field : fields) {
            boolean doNot = false;
            boolean isName = false;
            String newName = null;
            field.setAccessible(true);
            DoNotBind a = field.getAnnotation(DoNotBind.class);
            if (a != null) {
                doNot = true;
            }
            Name b = field.getAnnotation(Name.class);
            if (b != null) {
                isName = true;
                newName = b.value();
            }
            if (doNot) {
                continue;
            }
            if (isElementary(field.getType())) {
                try {
                    if (isName) {
                        if (field.get(value) == null) {
                            result.accumulate(newName, JSONObject.NULL);
                        } else {
                            result.accumulate(newName, field.get(value));
                        }

                    } else {
                        if (field.get(value) == null) {
                            result.accumulate(field.getName(), JSONObject.NULL);
                        } else {
                            result.accumulate(field.getName(), field.get(value));
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Invalid object, can't access field");
                }
            } else {
                Object as = null;
                try {
                    as = field.get(value);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Invalid object, can't access field");
                }
                if (as != null) {
                    cyc.put(value, true);
                    if (isName) {
                        result.accumulate(newName, serialized2(as));
                    } else {
                        result.accumulate(field.getName(), serialized2(as));
                    }
                    cyc.remove(value);
                } else {
                    if (isName) {
                        result.accumulate(newName, JSONObject.NULL);
                    } else {
                        result.accumulate(field.getName(), JSONObject.NULL);
                    }
                }
            }
        }
        output.write(result.toString().getBytes());
    }


    JSONObject serialized2(Object value) throws IOException {
        if (cyc.containsKey(value)) {
            throw new IllegalStateException("Cyclic");
        }
        Field[] fields = value.getClass().getDeclaredFields();
        JSONObject result = new JSONObject();
        for (Field field : fields) {
            boolean doNot = false;
            boolean isName = false;
            boolean isElementary = false;
            String newName = null;
            field.setAccessible(true);
            DoNotBind a = field.getAnnotation(DoNotBind.class);
            if (a != null) {
                doNot = true;
            }
            Name b = field.getAnnotation(Name.class);
            if (b != null) {
                isName = true;
                newName = b.value();
            }
            if (doNot) {
                continue;
            }
            if (isElementary(field.getType())) {
                try {
                    if (isName) {
                        if (field.get(value) == null) {
                            result.accumulate(newName, JSONObject.NULL);
                        } else {
                            result.accumulate(newName, field.get(value));
                        }
                    } else {
                        if (field.get(value) == null) {
                            result.accumulate(field.getName(), JSONObject.NULL);
                        } else {
                            result.accumulate(field.getName(), field.get(value));
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            } else {
                Object as = null;
                try {
                    as = field.get(value);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
                if (as != null) {
                    cyc.put(value, true);
                    if (isName) {
                        result.accumulate(newName, serialized2(as));
                    } else {
                        result.accumulate(field.getName(), serialized2(as));
                    }
                    cyc.remove(value);
                } else {
                    if (isName) {
                        result.accumulate(newName, JSONObject.NULL);
                    } else {
                        result.accumulate(field.getName(), JSONObject.NULL);
                    }
                }
            }
        }
        return result;
    }

    public boolean isElementary(Class clazz) {
        return  (clazz.isPrimitive()
                ||  clazz.equals(String.class)
                ||  clazz.isEnum());
    }

    public Object getValueFromString(Class clazz, String text) {
        if (text == null) {
            return null;
        }
        if (clazz == null) {
            throw new IllegalArgumentException("Wrong class");
        }
        if (clazz.equals(boolean.class)  ||  clazz.equals(Boolean.class)) {
            return Boolean.parseBoolean(text);
        }
        if (clazz.equals(int.class)  ||  clazz.equals(Integer.class)) {
            return Integer.parseInt(text);
        }
        if (clazz.equals(long.class)  ||  clazz.equals(Long.class)) {
            return Long.parseLong(text);
        }
        if (clazz.equals(double.class)  ||  clazz.equals(Double.class)) {
            return Double.parseDouble(text);
        }
        if (clazz.equals(float.class)  ||  clazz.equals(Float.class)) {
            return Float.parseFloat(text);
        }
        if (clazz.equals(byte.class)  ||  clazz.equals(Byte.class)) {
            return Byte.parseByte(text);
        }
        if (clazz.equals(short.class)  ||  clazz.equals(Short.class)) {
            return Short.parseShort(text);
        }
        if (clazz.equals(char.class)  ||  clazz.equals(Character.class)) {
            if (text.length() != 1) {
                throw new IllegalArgumentException("Incorrect type");
            }
            return text.charAt(0);
        }
        if (clazz.equals(String.class)) {
            return text;
        }
        if (clazz.isEnum()) {
            return Enum.valueOf(clazz, text);
        }
        return null;
    }
}
