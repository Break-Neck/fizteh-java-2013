package ru.fizteh.fivt.students.krivchansky.storable;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import ru.fizteh.fivt.storage.structured.ColumnFormatException;

public class XmlDeserializer {
	String representation;
	XMLStreamReader reader;
	
	public XmlDeserializer(String representation) throws ParseException {
		this.representation = representation;
		 try {
	            reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(representation));
	            if (!reader.hasNext()) {
	            	throw new ParseException("xml presentation is empty", 0);
	            }
	            int type = reader.next();
	            if (type != XMLStreamConstants.START_ELEMENT) {
	            	throw new ParseException("incorrect xml", 0);
	            }
	            if (!reader.getName().getLocalPart().equals("row")) {
	            	throw new ParseException("incorrect xml", 0);
	            }
	            
		 } catch (XMLStreamException e) {
			 throw new ParseException("error in deserializer: " + e.getMessage(), 0);
		 }
	}
	
	public Object getNext(Class<?> typeExpected) throws ColumnFormatException, ParseException {
		Object val = null;
		try {
			int type = reader.next();
			if(type != XMLStreamConstants.START_ELEMENT || !reader.getName().getLocalPart().equals("col")) {
				throw new ParseException ("incorrect xml", 0);
			}
			type = reader.next();
			if (type == XMLStreamConstants.CHARACTERS) {
				val = StoreableTypes.parseByClass(reader.getText(), typeExpected);
			} else {
				if (!reader.getName().getLocalPart().equals("null")) {
                    throw new ParseException("incorrect xml", 0);
                }
				val = null;
				type = reader.next();
				if (type != XMLStreamConstants.END_ELEMENT) {
					throw new ParseException("incorrect xml", 0);
				}
			}
			type = reader.next();
			if (type != XMLStreamConstants.END_ELEMENT) {
				throw new ParseException("incorrect xml", 0);
			}
		} catch (XMLStreamException e) {
			throw new ParseException("error in deserializer: " + e.getMessage(), 0);
		}
		return val;
	}
	
	public void close() throws ParseException, IOException {
		try {
            int type = reader.next();
            if (type != XMLStreamConstants.END_ELEMENT && type != XMLStreamConstants.END_DOCUMENT) {
                throw new ParseException("incorrect xml", 0);
            }
        } catch (XMLStreamException e) {
            throw new IOException("error while deserializing: " + e.getMessage());
        }
	}
}
