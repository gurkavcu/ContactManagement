package com.vngrs.model;

import com.mongodb.DBObject;
import com.vngrs.util.MongoDB;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Contact mapper class
 */

@Entity (value="contacts",noClassnameStored = true)
public class Contact {

    @Id
    private ObjectId id;

    @Indexed
    private String name;

    private String lastName;

    private Set<String> phones;

    /**
     *  Special flag that specify this contact is the terminator object.
     *  This can be use for threading purposes.
     */
    @Transient
    private boolean eof = false;

    public Contact() {}

    public Contact(String name, String lastName, Set<String> phones) {
        this.name = name;
        this.lastName = lastName;
        this.phones = phones;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastname) {
        this.lastName = lastname;
    }

    public Set<String> getPhones() {
        return phones;
    }

    public void setPhones(Set<String> phones) {
        this.phones = phones;
    }

    public void addPhones(Set<String> phones) {
        this.phones.addAll(phones);
    }

    public boolean isEof() {
        return eof;
    }

    public void setEof(boolean eof) {
        this.eof = eof;
    }

    public String getFullName() {
        return name + " " + lastName;
    }
    /**
     * Creates new contact if there is no matching name + lastname record.
     * If there is an existing record update only phone numbers.
     *
     * @return true if success
     */
    public boolean upsert() {

        Datastore ds = MongoDB.instance().getDatastore();
        Query<Contact> contactQuery = createContactQuery(this);
        UpdateOperations<Contact> updatePhones = ds.createUpdateOperations(Contact.class);

        // Third argument false indicates that this is an $addToSet operation
        updatePhones.addAll("phones", new ArrayList<Object>(phones),false);

        return !ds.update(contactQuery,updatePhones,true).getHadError();
    }

    public static Query<Contact> createContactQuery(Contact contact) {
        return MongoDB.instance().getDatastore().createQuery(Contact.class)
                .field("name").equal(contact.name)
                .field("lastName").equal(contact.lastName);
    }

    public static List<Contact> findContact(String name) {
        return MongoDB.instance().getDatastore().find(Contact.class).field("name").equal(Pattern.compile("^"+name+"$", Pattern.CASE_INSENSITIVE)).asList();
    }

    public static Contact getTerminator() {
        Contact contact = new Contact();
        contact.setEof(true);
        return contact;
    }


    @Override
    public String toString() {
        return MongoDB.instance().getMorphia().toDBObject(this).toString();
    }

    public static String toString(List<Contact> list) {
        Morphia morphia = MongoDB.instance().getMorphia();
        List<DBObject> d = new ArrayList<DBObject>();
        for(Contact c:list) {
            d.add(morphia.toDBObject(c));
        }
        return d.toString();
    }
}
