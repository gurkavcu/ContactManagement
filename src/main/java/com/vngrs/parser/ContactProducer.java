package com.vngrs.parser;

import com.vngrs.model.Contact;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Produces contact and stores them in an ArrayBlockingQueue.
 */
public class ContactProducer implements Runnable {

    private final File xml;

    // This queue will consume by ContactConsumer threads
    private final BlockingQueue<Contact> queue;

    private static final Logger LOG = LogManager.getLogger(ContactProducer.class.getName());

    public ContactProducer(BlockingQueue<Contact> queue, File xml) {
        this.queue = queue;
        this.xml = xml;
    }

    /**
     *  Inner class that responsible for xml parsing.
     *  Extracts the contact information and puts it on a BlockingQueue.
     */
    public class ContactHandler extends DefaultHandler {

        private Contact contact;

        private String content;

        public ContactHandler() {
            super();
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase("contact")) {
                contact = new Contact();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            content = new String(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equalsIgnoreCase("contact")) {
                try {
                    queue.put(contact);
                } catch (InterruptedException e) {
                    LOG.error("  Contact producer thread is interrupted while putting on queue : {} : {} ",e.toString(), contact.toString());
                }
            } else if (qName.equalsIgnoreCase("name")) {
                contact.setName(content);
            } else if (qName.equalsIgnoreCase("lastName")) {
                contact.setLastName(content);
            } else if (qName.equalsIgnoreCase("phone")) {
                contact.setPhones(new HashSet<String>(Arrays.asList(content)));
            }
        }

        @Override
        public void endDocument() throws SAXException {
            try {
                /* Putting a terminator contact sends all consumer threads a finishing signal */
                queue.put(Contact.getTerminator());
            } catch (InterruptedException e) {
                LOG.error("Thread exception : {}", e.toString());
            }
        }
    }

    /**
     * Parse the xml file using SAXParser.
     */
    private void parse() throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(xml, new ContactHandler());
    }

    public void run() {
        try {
            parse();
        } catch (Exception ex) {
            LOG.error("  Invalid xml file : {} ", ex.toString());
            try {
                queue.put(Contact.getTerminator());
            } catch (InterruptedException e) {
                LOG.error("Thread exception : {}", e.toString());
            }
        }
    }
}

