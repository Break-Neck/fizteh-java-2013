package ru.fizteh.fivt.students.kinanAlsarmini.binder;

import ru.fizteh.fivt.binder.Name;
import ru.fizteh.fivt.binder.DoNotBind;

import java.util.Stack;
import org.xml.sax.Attributes;

import java.lang.reflect.Field;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MyHandler extends DefaultHandler {
    private int counter;
    private Class<?> baseClass;
    private Stack<Object> objectStack;
    private Stack<Field> fieldStack;
    private Object deserializedObject;
    private StringBuilder tagContent;

    public MyHandler(Class<?> clazz) {
        counter = 0;
        baseClass = clazz;
        objectStack = new Stack<Object>();
        fieldStack = new Stack<Field>();
    }

    public Object getDeserialization() {
        return deserializedObject;
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(DoNotBind.class) != null) {
                continue;
            }

            String currentFieldName = field.getName();
            Name name = field.getAnnotation(Name.class);
            if (name != null) {
                currentFieldName = name.value();
            }

            if (fieldName.equals(currentFieldName)) {
                return field;
            }
        }

        return null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            counter++;

            if (counter % 2 == 0) {
                Field field = getField(objectStack.peek().getClass(), qName);
                if (field == null) {
                    throw new IllegalArgumentException("Invalid xml: no such field");
                }

                field.setAccessible(true);
                fieldStack.push(field);

                tagContent = new StringBuilder();
            } else {
                if (counter == 1) {
                    objectStack.push(baseClass.newInstance());
                } else {
                    objectStack.push(fieldStack.peek().getType().newInstance());
                }
            }
        } catch (IllegalAccessException | InstantiationException e)  {
            throw new IllegalArgumentException("Invalid object for deserialization");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            counter--;

            if (counter % 2 == 0) {
                Object currentObject = objectStack.pop();
                if (counter == 0) {
                    deserializedObject = currentObject;
                } else {
                    fieldStack.peek().setAccessible(true);
                    fieldStack.peek().set(objectStack.peek(), currentObject);
                }
            } else {
                Class<?> currentType = fieldStack.peek().getType();

                fieldStack.peek().setAccessible(true);

                if (currentType.isEnum()) {
                    String name = tagContent.toString();
                    for (Object object : currentType.getEnumConstants()) {
                        if (name.equals(object.toString())) {
                            fieldStack.peek().set(objectStack.peek(), object);
                        }
                    }
                }
                if (currentType.equals(String.class)) {
                    fieldStack.peek().set(objectStack.peek(), tagContent.toString());
                }
                if (currentType.equals(Boolean.class) || currentType.equals(boolean.class)) {
                    fieldStack.peek().set(objectStack.peek(), Boolean.parseBoolean(tagContent.toString()));
                }
                if (currentType.equals(Character.class) || currentType.equals(char.class)) {
                    fieldStack.peek().set(objectStack.peek(), tagContent.toString().charAt(0));
                }
                if (currentType.equals(Short.class) || currentType.equals(byte.class)) {
                    fieldStack.peek().set(objectStack.peek(), Short.parseShort(tagContent.toString()));
                }
                if (currentType.equals(Integer.class) || currentType.equals(int.class)) {
                    fieldStack.peek().set(objectStack.peek(), Integer.parseInt(tagContent.toString()));
                }
                if (currentType.equals(Long.class) || currentType.equals(long.class)) {
                    fieldStack.peek().set(objectStack.peek(), Long.parseLong(tagContent.toString()));
                }
                if (currentType.equals(Float.class) || currentType.equals(float.class)) {
                    fieldStack.peek().set(objectStack.peek(), Float.parseFloat(tagContent.toString()));
                }
                if (currentType.equals(Double.class) || currentType.equals(double.class)) {
                    fieldStack.peek().set(objectStack.peek(), Double.parseDouble(tagContent.toString()));
                }

                tagContent = null;

                fieldStack.pop();
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Invalid object for deserialization");
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        tagContent.append(new String(ch, start, length));
    }
}
