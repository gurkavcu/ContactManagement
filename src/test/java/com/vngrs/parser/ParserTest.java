package com.vngrs.parser;

import com.vngrs.model.Contact;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mongodb.morphia.query.Query;


import java.io.File;
import java.io.FileNotFoundException;


public class ParserTest extends AbstractMongoDBTest {

    public void testEmptyParsing() throws FileNotFoundException {

        String fileUrl = getClass().getResource("/empty.xml").getFile();
        File emptyXml = new File(fileUrl);

        ContactParser.ParseResult result = ContactParser.parseContact(emptyXml);

        assertEquals(0, result.count);
    }

    public void testInitialParsing() throws FileNotFoundException {

        String fileUrl = getClass().getResource("/initial.xml").getFile();
        File initialXml = new File(fileUrl);

        ContactParser.ParseResult result = ContactParser.parseContact(initialXml);

        assertEquals(2 , result.count);
        Query bobQuery = getDatastore().createQuery(Contact.class).field("name").equal(getEqualityPattern("Bob"));
        Contact bob = (Contact) bobQuery.get();

        assertTrue("Bob Sponge".contentEquals(bob.getFullName()));

        Query patrickQuery = getDatastore().createQuery(Contact.class).field("name").equal(getEqualityPattern("Patrick"));
        Contact patrick = (Contact) patrickQuery.get();

        assertTrue("Patrick Star".contentEquals(patrick.getFullName()));
    }

    public void testDuplicate() throws FileNotFoundException {

        String fileUrl = getClass().getResource("/initial.xml").getFile();
        File initialXml = new File(fileUrl);

        ContactParser.parseContact(initialXml);

        fileUrl = getClass().getResource("/second.xml").getFile();
        File secondXml = new File(fileUrl);

        ContactParser.ParseResult result = ContactParser.parseContact(secondXml);

        assertEquals(2 , result.count);

        Query bobQuery = getDatastore().createQuery(Contact.class).field("name").equal(getEqualityPattern("Bob"));
        Contact bob = (Contact) bobQuery.get();

        assertTrue("Bob Sponge".contentEquals(bob.getFullName()));
        assertEquals(3, bob.getPhones().size());
        assertTrue(bob.getPhones().contains("+90 300 1111111"));
        assertTrue(bob.getPhones().contains("+90 300 2222222"));
        assertTrue(bob.getPhones().contains("+90 300 3333333"));


        Query patrickQuery = getDatastore().createQuery(Contact.class).field("name").equal(getEqualityPattern("Patrick"));
        Contact patrick = (Contact) patrickQuery.get();

        assertTrue("Patrick Star".contentEquals(patrick.getFullName()));
        assertEquals(1, patrick.getPhones().size());
        assertTrue(patrick.getPhones().contains("+90 400 1111111"));
    }

}
