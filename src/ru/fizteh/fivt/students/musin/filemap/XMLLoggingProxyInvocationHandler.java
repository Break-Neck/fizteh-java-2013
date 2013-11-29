package ru.fizteh.fivt.students.musin.filemap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;

public class XMLLoggingProxyInvocationHandler implements InvocationHandler {

    Object target;
    XMLStreamWriter writer;
    Writer initialWriter;
    IdentityHashMap<Object, Boolean> identityHashMap;

    public XMLLoggingProxyInvocationHandler(Object target, Writer writer) throws XMLStreamException {
        this.target = target;
        this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        this.initialWriter = writer;
        this.identityHashMap = new IdentityHashMap<>();
    }

    void writeIterable(Iterable object) throws XMLStreamException {
        if (identityHashMap.get(object) != null) {
            writer.writeCharacters("cyclic");
        } else {
            identityHashMap.put(object, true);
            writer.writeStartElement("list");
            for (Object value : object) {
                writer.writeStartElement("value");
                writeObject(value);
                writer.writeEndElement();
            }
            writer.writeEndElement();
            identityHashMap.remove(object);
        }
    }

    void writeObject(Object object) throws XMLStreamException {
        if (object == null) {
            writer.writeEmptyElement("null");
        } else if (object instanceof Iterable) {
            writeIterable((Iterable) object);
        } else {
            writer.writeCharacters(object.toString());
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        Throwable error = null;
        try {
            result = method.invoke(target, args);
        } catch (InvocationTargetException e) {
            error = e.getTargetException();
        } catch (Throwable e) {
            //Proxy not allowed to throw exception
        }
        if (method.getDeclaringClass() != Object.class) {
            try {
                writer.writeStartElement("invoke");
                writer.writeAttribute("timestamp", Long.valueOf(System.currentTimeMillis()).toString());
                writer.writeAttribute("class", target.getClass().getName());
                writer.writeAttribute("name", method.getName());
                if (args != null && args.length != 0) {
                    writer.writeStartElement("arguments");
                    for (int i = 0; i < args.length; i++) {
                        writer.writeStartElement("argument");
                        writeObject(args[i]);
                        writer.writeEndElement();
                    }
                    writer.writeEndElement();
                } else {
                    writer.writeEmptyElement("arguments");
                }
                if (method.getReturnType() != void.class && error == null) {
                    writer.writeStartElement("return");
                    writeObject(result);
                    writer.writeEndElement();
                } else if (error != null) {
                    writer.writeStartElement("thrown");
                    writer.writeCharacters(error.toString());
                    writer.writeEndElement();
                }
                writer.writeEndElement();
                writer.flush();
                initialWriter.write("\n");
                if (error != null) {
                    throw error;
                }
            } catch (XMLStreamException e) {
                //Proxy not allowed to throw exceptions
            }
        }
        return result;
    }
}
