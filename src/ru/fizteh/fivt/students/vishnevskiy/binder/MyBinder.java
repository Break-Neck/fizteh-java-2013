package ru.fizteh.fivt.students.vishnevskiy.binder;

import ru.fizteh.fivt.binder.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.lang.reflect.Field;
import javax.xml.stream.*;
import javax.xml.parsers.*;

import org.xml.sax.SAXException;

public class MyBinder<T> implements Binder<T> {
    private Class<T> clazz;
    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

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

    public MyBinder(Class<T> clazz) {
        this.clazz = clazz;
    }

    public T deserialize(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("Invalid input stream");
        }

        Handler handler = new Handler(this.clazz);
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(input, handler);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Parsing failed");
        }

        return (T) handler.getObject();
    }

    private void checkCyclic(Object object) {
        IdentityHashMap<Object, Object> objects = new IdentityHashMap<Object, Object>();
        checkSubObjects(objects, object);
    }

    private void checkSubObjects(IdentityHashMap<Object, Object> objects, Object object) {
        if (object == null) {
            return;
        }
        if (object.getClass().isPrimitive() || isWrapperType(object.getClass())
                || object.getClass().equals(String.class) || object.getClass().isEnum()) {
            return;
        }
        if (objects.containsKey(object)) {
            throw new IllegalStateException("The object has a circular reference");
        }
        objects.put(object, null);
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getAnnotation(DoNotBind.class) == null) {
                try {
                    checkSubObjects(objects, field.get(object));
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Invalid object");
                }
            }
        }
    }

    private void writeXML(Object object, XMLStreamWriter writer) throws XMLStreamException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        if (clazz.isPrimitive() || isWrapperType(clazz) || clazz.equals(String.class) || clazz.isEnum()) {
            writer.writeCharacters(object.toString());
        } else {
            writer.writeStartElement(clazz.getName());

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getAnnotation(DoNotBind.class) != null) {
                    continue;
                }

                String fieldName;
                Name newName = field.getAnnotation(Name.class);
                if (newName != null) {
                    fieldName = newName.value();
                } else {
                    fieldName = field.getName();
                }
                writer.writeStartElement(fieldName);

                Object fieldObj = field.get(object);
                if (fieldObj == null) {
                    writer.writeAttribute("value", "null");
                } else {
                    writeXML(fieldObj, writer);
                }
                writer.writeEndElement();
            }

            writer.writeEndElement();
        }
    }

    public void serialize(T value, OutputStream output) throws IOException {
        if (value == null) {
            throw new IllegalArgumentException("Invalid object");
        }
        if (output == null) {
            throw new IllegalArgumentException("Invalid output stream");
        }

        try {
            checkCyclic(value);
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(output);
            writeXML(value, writer);
        } catch (XMLStreamException e) {
            throw new IOException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Invalid object");
        }
    }
}