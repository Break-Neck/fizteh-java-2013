package ru.fizteh.fivt.students.vishnevskiy.binder;

import ru.fizteh.fivt.binder.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.IdentityHashMap;
import java.lang.reflect.Field;
import javax.xml.stream.*;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;

public class MyBinder<T> implements Binder<T> {
    private Class<T> clazz;

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

        return (T)handler.getObject();
    }

    private IdentityHashMap<Object, Object> subObjects(Object object) throws IllegalAccessException {
        IdentityHashMap<Object, Object> objects = new IdentityHashMap<Object, Object>();
        if (object == null) {
            return objects;
        }

        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getAnnotation(DoNotBind.class) == null) {
                if (subObjects(field.get(object)).containsKey(field.get(object))) {
                    throw new IllegalStateException("The object has a circular reference");
                }
                objects.put(field.get(object), null);
            }
        }
        return objects;
    }

    private void writeXML(Object object, XMLStreamWriter writer) throws XMLStreamException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        if (clazz.isPrimitive() || clazz.equals(String.class) || clazz.isEnum()) {
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
            if (subObjects(value).containsKey(value)) {
                throw new IllegalStateException("The object has a circular reference");
            }
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(output);
            writeXML(value, writer);
        } catch (XMLStreamException e) {
            throw new IOException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Invalid object");
        }
    }
}