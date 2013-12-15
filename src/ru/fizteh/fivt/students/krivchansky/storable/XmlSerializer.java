package src.ru.fizteh.fivt.students.krivchansky.storable;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XmlSerializer {
	StringWriter strWriter = new StringWriter();
	XMLStreamWriter writer = null;
	
	public XmlSerializer() throws IOException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		try {
            writer = factory.createXMLStreamWriter(strWriter);
            writer.writeStartElement("row");
        } catch (XMLStreamException e) {
            throw new IOException("error while serializing: " + e.getMessage());
        }
    }

    public void write(Object value) throws IOException, ParseException {
        try {
            writer.writeStartElement("col");
            if (value == null) {
                writer.writeStartElement("null");
                writer.writeEndElement();
            } else {
                writer.writeCharacters(value.toString());
            }
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException("error while serializing: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        try {
            writer.writeEndElement();
            writer.flush();
        } catch (XMLStreamException e) {
            throw new IOException("error while serializing: " + e.getMessage());
        }
    }

    public String getRepresentation() {
        return strWriter.getBuffer().toString();
    }
}
