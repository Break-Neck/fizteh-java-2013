package ru.fizteh.fivt.students.chernigovsky.proxy;

import javax.xml.stream.*;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;

public class LoggingInvocationHandler implements InvocationHandler {
    private Writer writer;
    private Object implementation;

    public LoggingInvocationHandler(Writer newWriter, Object newImplementation) {
        writer = newWriter;
        implementation = newImplementation;
    }

    private void iterableLog(XMLStreamWriter xmlStreamWriter, Object arg, IdentityHashMap<Iterable, Boolean> identityHashMap) throws XMLStreamException {
        if (arg == null) {
            xmlStreamWriter.writeEmptyElement("null");
        } else if (arg instanceof Iterable) {
            if (identityHashMap.containsKey(arg)) {
                xmlStreamWriter.writeCharacters("cyclic");
                return;
            }

            identityHashMap.put((Iterable)arg, true);

            xmlStreamWriter.writeStartElement("list");
            for (Object object : (Iterable)arg) {
                xmlStreamWriter.writeStartElement("value");
                iterableLog(xmlStreamWriter, object, identityHashMap);
                xmlStreamWriter.writeEndElement();
            }
            xmlStreamWriter.writeEndElement();

        } else {
            xmlStreamWriter.writeCharacters(arg.toString());
        }
    }

    private String getLog(Method method, Object[] args, Object returnValue, Throwable thrown) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
        IdentityHashMap<Iterable, Boolean> identityHashMap = new IdentityHashMap<Iterable, Boolean>();

        xmlStreamWriter.writeStartElement("invoke");

        xmlStreamWriter.writeAttribute("timestamp", Long.toString(System.currentTimeMillis()));
        xmlStreamWriter.writeAttribute("class", implementation.getClass().getName());
        xmlStreamWriter.writeAttribute("name", method.getName());

        if (args == null || args.length == 0) {
            xmlStreamWriter.writeEmptyElement("arguments");
        } else {
            xmlStreamWriter.writeStartElement("arguments");

            for (Object arg : args) {
                xmlStreamWriter.writeStartElement("argument");
                iterableLog(xmlStreamWriter, arg, identityHashMap);
                xmlStreamWriter.writeEndElement();
            }

            xmlStreamWriter.writeEndElement();
        }

        identityHashMap.clear();

        if (thrown != null) {
            xmlStreamWriter.writeStartElement("thrown");
            xmlStreamWriter.writeCharacters(thrown.toString());
            xmlStreamWriter.writeEndElement();
        } else if (method.getReturnType() != void.class) {
            xmlStreamWriter.writeStartElement("return");
            if (returnValue == null) {
                xmlStreamWriter.writeEmptyElement("null");
            } else {
                xmlStreamWriter.writeCharacters(returnValue.toString());
            }
            xmlStreamWriter.writeEndElement();
        }

        xmlStreamWriter.writeEndElement();

        return stringWriter.toString() + System.lineSeparator();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!Object.class.equals(method.getDeclaringClass())) {
            Throwable thrown = null;
            Object returnValue = null;

            try {
                returnValue = method.invoke(implementation, args);
            } catch (InvocationTargetException ex) {
                thrown = ex.getTargetException();
            }

            writer.append(getLog(method, args, returnValue, thrown));

            if (thrown != null) {
                throw thrown;
            }

            return returnValue;
        }

        try {
            return method.invoke(implementation, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
