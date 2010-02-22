/**
 * 
 */
package edu.bu;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * An {@link ContentHandler} that transforms twitter's user timeline XML format
 * into incident reports.
 * 
 * @author dml
 * 
 */
public class IncidentHandler implements ContentHandler {
	private static final String TEXT = "text";
	private static final String STATUS = "status";
	private static final String STATUSES = "statuses";
	private static final String USER = "user";
	private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();
	private static final Set<String> STATUS_ELEMENTS = new HashSet<String>(); 
	static {
		STATUS_ELEMENTS.add(TEXT);
		STATUS_ELEMENTS.add("created_at");
	}
	private static final Set<String> USER_ELEMENTS = new HashSet<String>();
	static {
		USER_ELEMENTS.add("id");
		USER_ELEMENTS.add("screen_name");
	}
	private final OutputStreamFactory outputFactory;
	private final Map<String, XMLStreamWriter> outputs = new HashMap<String, XMLStreamWriter>();
	private final Pattern hashTagPattern = Pattern.compile("(#\\w+)");
	
	private String currentElement = null;
	private Set<String> relevantElements = STATUS_ELEMENTS;
	private Map<String, String> incident = new HashMap<String, String>();

	public IncidentHandler(OutputStreamFactory outputFactory) {
		this.outputFactory = outputFactory;
	}

	@Override
	public void characters(char[] characters, int start, int length) throws SAXException {
		if(currentElement == null) {
			// filter out unimportant characters
			return;
		}
		addToElement(currentElement, new String(Arrays.copyOfRange(characters,
				start, start + length)));
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			for(XMLStreamWriter writer : outputs.values()) {
				writer.writeEndElement();
				writer.writeEndDocument();
				writer.close();
			}
		} catch (XMLStreamException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qname)
			throws SAXException {
		currentElement = null;
		if(STATUS.equals(localName)) {
			Matcher matcher = hashTagPattern.matcher(incident.get(TEXT));
			try {
				for(int i = 1; matcher.find(); ++i) {
					XMLStreamWriter writer = getXmlStreamWriter(matcher.group());
					writer.writeStartElement(STATUS);
					for(Entry<String, String> entry: incident.entrySet()) {
						writer.writeStartElement(entry.getKey());
						writer.writeCharacters(entry.getValue());
						writer.writeEndElement();
					}
					writer.writeEndElement();
				}
			} catch (XMLStreamException e) {
				throw new SAXException(e);
			}
			incident = new HashMap<String, String>();
			return;
		}
		if(USER.equals(localName)) {
			relevantElements = STATUS_ELEMENTS;
			return;
		}
	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDocumentLocator(Locator arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void startElement(String uri, String localName, String qname,
			Attributes attrs) throws SAXException {
		if (relevantElements.contains(localName)) {
			// entering a relevant element
			currentElement = localName;
			return;
		}
		if (USER.equals(localName)) {
			relevantElements = USER_ELEMENTS;
			return;
		}
	}

	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub

	}
	
	private void addToElement(String name, String text) {
		StringBuilder result;
		if(!incident.containsKey(name)) {
			result = new StringBuilder("");
		} else {
			result = new StringBuilder(incident.get(name));
		}
		incident.put(name, result.append(text).toString());
	}
	
	private XMLStreamWriter getXmlStreamWriter(String tag) throws XMLStreamException {
		if(outputs.containsKey(tag)) {
			return outputs.get(tag);
		}
		XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(outputFactory.open(tag));
		writer.writeStartDocument();
		writer.writeStartElement(STATUSES);
		outputs.put(tag, writer);
		return writer;
	}

}
