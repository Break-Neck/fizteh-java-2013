package ru.fizteh.fivt.students.baranov.binder;

import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.binder.Name;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class MyBinder<T> implements Binder<T> {
    private String ls = System.lineSeparator();
    private String tab = "\t";

    private IdentityHashMap<Object, Boolean> mapOfObjects = new IdentityHashMap<>();
    private Class clazz;
    private Field[] fields;
    private HashMap<String, Field> fieldMap;
    private int level;

    MyBinder(Class<T> newClazz, int newLevel) {
        this.clazz = newClazz;
        this.fields = clazz.getFields();
        this.fieldMap = getMap(fields);
        this.level = newLevel;
    }

    private void printTabs(OutputStream output) throws IOException {
        for (int i = 0; i < level; ++i) {
            output.write(tab.getBytes());
        }
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
                    for (int j = 0; j < childNodes.getLength(); ++j) {
                        Node innerNode = childNodes.item(j);
                        String nodeValue = innerNode.getTextContent();
                        if (innerNode.hasChildNodes()) {
                            goThroughNode(node, field, result);
                        } else {
                            field.set(result, casting(field, nodeValue));
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new IOException("XML parsing error");
        }
    }

    private Object casting(Field field, String value) {
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
            return value.charAt(0);
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

        if (clazz.isPrimitive()) {
            output.write(("<" + clazz.getName() + ">").getBytes());
            output.write((valueOfClass.toString()).getBytes());
            output.write(("</" + clazz.getName() + ">" + ls).getBytes());
            return;
        }

        mapOfObjects.put(valueOfClass, true);

        output.write(("<" + clazz.getName() + ">" + ls).getBytes());
        level++;
        for (Field field : fields) {
            if (!(field.getType().isPrimitive() || field.getType().equals(String.class))) {
                if (!mapOfObjects.containsKey(field)) {
                    mapOfObjects.put(field, true);
                } else {
                    throw new IllegalStateException("circular reference");
                }
            }
            String fieldName = field.getName();
            boolean needPrintField = true;
            Annotation[] annotations = field.getAnnotations();
            Name nameAnnotation = field.getAnnotation(Name.class);
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Name.class)) {
                    fieldName = nameAnnotation.value();
                } else if (annotation.annotationType().equals(DoNotBind.class)) {
                    needPrintField = false;
                    continue;
                }
            }
            if (needPrintField) {
                printField(valueOfClass, field, fieldName, output);
            }
        }
        level--;
        printTabs(output);
        output.write(("</" + clazz.getName() + ">" + ls).getBytes());

    }

    private HashMap<String, Field> getMap(Field[] fields) {
        HashMap<String, Field> result = new HashMap<>();
        for (int i = 0; i < fields.length; ++i) {
            result.put(fields[i].getName(), fields[i]);
        }
        return result;
    }

    private void goThroughNode(Node parent, Field field, Object result) throws IllegalAccessException {
        Object innerObj = field.get(result);
        HashMap<String, Field> innerFieldMap = getMap(innerObj.getClass().getFields());

        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                NodeList childNodes = node.getChildNodes();
                String nodeName = node.getNodeName();
                Field innerField = innerFieldMap.get(nodeName);
                for (int j = 0; j < childNodes.getLength(); ++j) {
                    Node innerNode = childNodes.item(j);
                    String nodeValue = innerNode.getTextContent();
                    if (innerNode.hasChildNodes()) {
                        goThroughNode(innerNode, innerField, innerObj);
                    } else {
                        innerField.set((T)innerObj, casting(innerField, nodeValue));
                    }
                }
            }
        }

        field.set(result, innerObj);
    }

    private void printField(T valueOfClass, Field field, String fieldName, OutputStream output) throws IOException {
        boolean needPrintField = true;
        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.equals(Name.class)) {
                try {
                    fieldName = Name.class.getField("value").toString();
                } catch (NoSuchFieldException e) {
                    //
                }
            } else if (annotation.equals(DoNotBind.class)) {
                needPrintField = false;
                continue;
            }
        }
        if (needPrintField) {
            printTabs(output);
            output.write(("<" + fieldName + ">").getBytes());
            Class typeOfField = field.getType();
            if (typeOfField.isPrimitive() || typeOfField.equals(String.class)) {
                field.setAccessible(true);
                try {
                    output.write(field.get(valueOfClass).toString().getBytes());
                } catch (Exception e) {
                    //
                }
                output.write(("</" + fieldName + ">" + ls).getBytes());
            } else {
                level++;
                output.write(ls.getBytes());
                printTabs(output);
                Object objectOfField = null;
                field.setAccessible(true);
                try {
                    objectOfField = field.get(valueOfClass);
                } catch (Exception e) {
                    //
                }
                MyBinderFactory factory = new MyBinderFactory();
                MyBinder binder = factory.create(typeOfField, level);
                binder.serialize(objectOfField, output);
                level--;
                printTabs(output);
                output.write(("</" + fieldName + ">" + ls).getBytes());
            }
        }
    }
}
