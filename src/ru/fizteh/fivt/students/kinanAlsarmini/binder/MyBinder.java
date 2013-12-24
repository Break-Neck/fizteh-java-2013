package ru.fizteh.fivt.students.kinanAlsarmini.binder;

import ru.fizteh.fivt.binder.Binder;
import ru.fizteh.fivt.binder.Name;
import ru.fizteh.fivt.binder.DoNotBind;
import ru.fizteh.fivt.students.kinanAlsarmini.binder.MyHandler;

import java.lang.reflect.Field;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Set;
import java.util.HashSet;
import java.util.IdentityHashMap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class MyBinder<T> implements Binder<T> {
    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    private int counter;
    private Class<T> baseClass;

    public MyBinder(Class<T> clazz) {
        counter = 0;
        baseClass = clazz;
    }

    private static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> wrapperTypes = new HashSet<Class<?>>();

        wrapperTypes.add(Boolean.class);
        wrapperTypes.add(Character.class);
        wrapperTypes.add(Byte.class);
        wrapperTypes.add(Short.class);
        wrapperTypes.add(Integer.class);
        wrapperTypes.add(Long.class);
        wrapperTypes.add(Float.class);
        wrapperTypes.add(Double.class);

        return wrapperTypes;
    }

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    @Override
    public T deserialize(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("Invalid input stream: null");
        }

        SAXParserFactory SAXFactory = SAXParserFactory.newInstance();
        MyHandler handler = new MyHandler(baseClass);

        try {
            SAXParser SAX = SAXFactory.newSAXParser();
            SAX.parse(input, handler);
        } catch (ParserConfigurationException | SAXException e) {
            System.err.println(e.getMessage());
        }

        return (T)handler.getDeserialization();
    }

    private boolean checkCircularReference(Object object, IdentityHashMap<Object,Boolean> objectState) throws IllegalAccessException {
        if (object == null) {
            return false;
        }

        if (objectState.containsKey(object)) {
            return objectState.get(object);
        }

        Class<?> currentClass = object.getClass();

        if (currentClass.isPrimitive() || currentClass.isEnum() || isWrapperType(currentClass) || currentClass.equals(String.class)) {
            objectState.put(object, false);
            return false;
        }
        
        objectState.put(object, true);

        Field[] fields = currentClass.getDeclaredFields();
        
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getAnnotation(DoNotBind.class) == null && checkCircularReference(field.get(object), objectState)) {
                return true;
            }
        }

        objectState.put(object, false);

        return false;
    }

    private void serialize(Object object, XMLStreamWriter XMLWriter) throws XMLStreamException, IllegalAccessException {
        Class<?> currentClass = object.getClass();

        if (currentClass.isEnum() || isWrapperType(currentClass) || currentClass.equals(String.class)) {
            XMLWriter.writeCharacters(object.toString());
        } else {
            XMLWriter.writeStartElement(currentClass.getName());

            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);

                if (field.getAnnotation(DoNotBind.class) != null) {
                    continue;
                }

                String fieldName = field.getName();
                Name name = field.getAnnotation(Name.class);
                if (name != null) {
                    fieldName = name.value();
                }

                Object currentField = field.get(object);
                XMLWriter.writeStartElement(fieldName);
                if (currentField == null) {
                    XMLWriter.writeAttribute("value", "null");
                } else if (currentField.equals("")) {
                    XMLWriter.writeAttribute("value", "empty");
                } else {
                    serialize(currentField, XMLWriter);
                }
                XMLWriter.writeEndElement();
            }

            XMLWriter.writeEndElement();
        }
    }

    @Override
    public void serialize(T value, OutputStream output) throws IOException {
        if (value == null || output == null) {
            throw new IllegalArgumentException("Invalid object / outputstream: null");
        }

        try {
            IdentityHashMap<Object,Boolean> objectState = new IdentityHashMap<Object,Boolean>();
            if (checkCircularReference(value, objectState)) {
                throw new IllegalStateException("Circular reference in the object");
            }
            XMLStreamWriter XMLWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(output);
            serialize(value, XMLWriter);
        } catch (XMLStreamException e) {
            throw new IOException("Error occured while writing to XMLStream");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Invalid object fields");
        }
    }
}
