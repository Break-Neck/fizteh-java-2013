package ru.fizteh.fivt.students.baranov.binder;

import org.xml.sax.SAXException;
import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MyBinder<T> implements Binder<T> {
    private Class clazz;
    private Field[] fields;
    private HashMap<String, Field> fieldMap;
    public HashSet<String> setOfClasses;

    MyBinder(Class<T> newClazz) {
        this.clazz = newClazz;
        this.fields = clazz.getDeclaredFields();
        this.fieldMap = getMap(fields);
    }

    public T deserialize(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input is null");
        }
        try {
            T result = (T) clazz.newInstance();

            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setValidating(false);
            DocumentBuilder builder = f.newDocumentBuilder();
            Document doc = builder.parse(input);
            NodeList nodes = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodes.getLength(); ++i) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeName = node.getNodeName();
                    Field field = fieldMap.get(nodeName);
                    NodeList childNodes = node.getChildNodes();
                    Class nodeClass = field.getType();
                    if (nodeClass.isPrimitive() || nodeClass.equals(String.class) || nodeClass.isEnum()) {
                        String nodeValue = childNodes.item(0).getTextContent();
                        field.setAccessible(true);
                        field.set(result, casting(field, nodeValue));
                    } else {
                        goThroughNode(node, nodeClass, field, result);
                    }
                }
            }
            return result;
        } catch (InstantiationException e) {
            throw new IOException("can't create new instance in deserializer");
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("XML parser error");
        } catch (IllegalAccessException e) {
            //
        }
        return null;
    }

    private Object casting(Field field, String value) throws IOException {
        Class type = field.getType();
        if (type.equals(byte.class)) {
            return Byte.parseByte(value);
        } else if (type.equals(short.class)) {
            return Short.parseShort(value);
        } else if (type.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(long.class)) {
            return Long.parseLong(value);
        } else if (type.equals(float.class)) {
            return Float.parseFloat(value);
        } else if (type.equals(double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (type.equals(char.class)) {
            if (value.length() == 1) {
                return value.charAt(0);
            } else {
                throw new IOException("char is longer that 1");
            }
        } else if (type.isEnum()) {
            for (Object object : type.getEnumConstants()) {
                if (value.equals(object.toString())) {
                    return object;
                }
            }
        }
        return value;
    }

    public void serialize(T valueOfClass, OutputStream output) throws IOException {
        if (valueOfClass == null) {
            throw new IllegalArgumentException("object is null");
        }
        if (output == null) {
            throw new IllegalArgumentException("output is null");
        }
        try {
            IdentityHashMap<Object, Boolean> mapOfObjects = new IdentityHashMap<>();
            if (circularLinks(valueOfClass, mapOfObjects)) {
                throw new IllegalStateException("class has circular links");
            }
        } catch (IllegalAccessException e) {
            //
        }
        if (clazz.isPrimitive() || clazz.equals(String.class) || clazz.isEnum()) {
            output.write(("<" + clazz.getSimpleName() + ">").getBytes());
            output.write((valueOfClass.toString()).getBytes());
            output.write(("</" + clazz.getSimpleName() + ">").getBytes());
            return;
        }
        output.write(("<" + clazz.getSimpleName() + ">").getBytes());
        for (Field field : fields) {
            String fieldName = field.getName();
            boolean needPrintField = true;
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Name.class)) {
                    Name nameAnnotation = field.getAnnotation(Name.class);
                    fieldName = nameAnnotation.value();
                } else if (annotation.annotationType().equals(DoNotBind.class)) {
                    needPrintField = false;
                }
            }

            field.setAccessible(true);
            try {
                if (field.get(valueOfClass) == null) {
                    needPrintField = false;
                }
            } catch (IllegalAccessException e) {
                //
            }


            if (needPrintField) {
                printField(valueOfClass, field, fieldName, output);
            }
        }
        output.write(("</" + clazz.getSimpleName() + ">").getBytes());
    }

    private HashMap<String, Field> getMap(Field[] fields) {
        HashMap<String, Field> result = new HashMap<>();
        for (int i = 0; i < fields.length; ++i) {
            String fieldName = fields[i].getName();
            Annotation[] annotations = fields[i].getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Name.class)) {
                    Name nameAnnotation = fields[i].getAnnotation(Name.class);
                    fieldName = nameAnnotation.value();
                }
            }
            result.put(fieldName, fields[i]);
        }
        return result;
    }

    private void goThroughNode(Node node, Class nodeClass, Field field, Object result) throws IOException {
        NodeList list = node.getChildNodes();
        try {
            Object obj = nodeClass.newInstance();
            HashMap<String, Field> map = getMap(nodeClass.getDeclaredFields());
            for (int i = 0; i < list.getLength(); ++i) {
                Node n = list.item(i);
                if (n.hasChildNodes()) {
                    Node child = n.getFirstChild();
                    Field f = map.get(child.getNodeName());
                    Class fClass = f.getType();
                    if (fClass.isPrimitive() || fClass.equals(String.class) || fClass.isEnum()) {
                        String value = child.getTextContent();
                        f.setAccessible(true);
                        f.set(obj, casting(f, value));
                    } else {
                        goThroughNode(child, fClass, f, obj);
                    }
                }
            }
            field.setAccessible(true);
            field.set(result, obj);

        } catch (IllegalAccessException e) {
            //
        } catch (InstantiationException e) {
            throw new IOException("can't create new instance");
        }

    }

    private void printField(T valueOfClass, Field field, String fieldName, OutputStream output) throws IOException {
        boolean needPrintField = true;
        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Name.class)) {
                Name nameAnnotation = field.getAnnotation(Name.class);
                fieldName = nameAnnotation.value();
            } else if (annotation.annotationType().equals(DoNotBind.class)) {
                needPrintField = false;
            }
        }

        field.setAccessible(true);
        try {
            field.get(valueOfClass);
        } catch (Exception e) {
            needPrintField = false;
        }

        if (needPrintField) {
            output.write(("<" + fieldName + ">").getBytes());
            Class typeOfField = field.getType();
            if (typeOfField.isPrimitive() || typeOfField.equals(String.class) || typeOfField.isEnum()) {
                try {
                    output.write(field.get(valueOfClass).toString().getBytes());
                } catch (IllegalAccessException e) {
                    //
                }
                output.write(("</" + fieldName + ">").getBytes());
            } else {
                Object objectOfField = null;
                try {
                    objectOfField = field.get(valueOfClass);
                } catch (Exception e) {
                    //
                }
                MyBinderFactory factory = new MyBinderFactory();
                factory.setOfClasses = setOfClasses;
                MyBinder binder = factory.create(typeOfField);
                binder.serialize(objectOfField, output);
                output.write(("</" + fieldName + ">").getBytes());
            }
        }
    }

    private boolean circularLinks(Object obj, IdentityHashMap<Object, Boolean> map) throws IllegalAccessException {
        if (obj != null) {
            Class classOfObject = obj.getClass();
            if (map.containsKey(obj)) {
                return map.get(obj);
            }
            if (classOfObject.isPrimitive() || isWrapperType(classOfObject) || classOfObject.equals(String.class) ||
                    classOfObject.isEnum()) {
                return false;
            }
            map.put(obj, true);
            Field[] fieldsOfObject = classOfObject.getDeclaredFields();
            for (Field field : fieldsOfObject) {
                field.setAccessible(true);
                if (circularLinks(field.get(obj), map)) {
                    return true;
                }
            }
            map.put(obj, false);
            return false;
        } else {
            return false;
        }
    }

    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    private static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }
}
