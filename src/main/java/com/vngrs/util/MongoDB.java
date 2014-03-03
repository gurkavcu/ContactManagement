package com.vngrs.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.vngrs.model.Contact;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class MongoDB {

    private static final Logger LOG = LogManager.getLogger(MongoDB.class.getName());

    private static MongoDB INSTANCE;

    public static final String DB_NAME = "contact_management";

    private final Datastore datastore;

    private Morphia morphia;

    /**
     * Load configuration file of MongoDB and create a datastore from that.
     */
    private MongoDB() {
        try {
            Properties mongoProperties = loadProperties();
            List<ServerAddress> servers = getServerAddresses(mongoProperties);
            MongoClientOptions clientOptions = loadClientOptions(mongoProperties);
            MongoClient mongoClient = new MongoClient(servers,clientOptions);
            morphia = new Morphia();
            datastore = morphia.mapPackage(Contact.class.getPackage().getName())
                               .createDatastore(mongoClient, DB_NAME);
            datastore.ensureIndexes();
            LOG.debug("Connection to database {} initialized",DB_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MongoDB", e);
        }
    }

    private MongoDB(Morphia morphia, Datastore datastore) {
        this.morphia = morphia;
        this.datastore = datastore;
    }

    /**
     * Because the creation of MongoDB object costly we are using singleton pattern here.
     *
     * @return single instance of MongoDB
     */
    public static MongoDB instance() {
        if(INSTANCE == null) {
            INSTANCE = new MongoDB();
        }
        return INSTANCE;
    }

    public static MongoDB setInstance(Morphia morphia, Datastore datastore) {
        if(INSTANCE == null) {
            INSTANCE = new MongoDB(morphia,datastore);
        }
        return INSTANCE;
    }

    /**
     * <p>Parse the server addresses from properties file.</p>
     * <p>If there is no server address in the configuration file use default server => 127.0.0.1:27017.</p>
     * Sample configuration  : node[id]=host:port
     *
     * @param properties loaded mongodb properties
     * @return  List of mongod servers in the same replica set or a list of mongos servers in the same sharded cluster.
     */
    public static List<ServerAddress> getServerAddresses(Properties properties){
        String  host;
        Integer port;
        ArrayList<ServerAddress> serverList = new ArrayList<ServerAddress>();
        try {
            for(int i = 1 ; i < 21 ; i++) {
                String node = properties.getProperty("node"+i);
                if(node != null && !node.isEmpty()) {
                    int portIdx = node.indexOf( ":" );
                    if(portIdx > 0) {
                        port = Integer.parseInt( node.substring( portIdx + 1 ).trim() );
                        host = node.substring( 0 , portIdx ).trim();
                        serverList.add(new ServerAddress(host,port));
                    }
                }
                else break;
            }
            if(serverList.size() == 0)
               serverList.add(new ServerAddress("127.0.0.1",27017));
        }
        catch (UnknownHostException ex) {
            LOG.error(ex.toString());
            throw new RuntimeException(ex);
        }
        return serverList;
    }

    /**
     * Load mongodb configuration
     *
     * @return  mongodb properties
     */
    public static Properties loadProperties() {
        Properties prop =  new Properties();
        try {
            prop.load(MongoDB.class.getResourceAsStream("/mongodb.properties"));
        } catch (IOException e) {
            throw new RuntimeException("MongoDB properties file cannot be loaded");
        };
        return prop;
    }

    /**
     * Build MongoClientOptions from the given settings.
     *
     * @param prop
     * @return
     */
    public static MongoClientOptions loadClientOptions(Properties prop) {

        MongoClientOptions.Builder builder = MongoClientOptions.builder();

        HashMap<String,Method> methodMap = new HashMap<String, Method>();
        for(Method method: MongoClientOptions.Builder.class.getDeclaredMethods()) {
            methodMap.put(method.getName(),method);
        }

        for(String key : prop.stringPropertyNames()) {
            if(methodMap.get(key) != null)
                try {
                    if(key.equalsIgnoreCase("writeConcern")) {
                        builder.writeConcern(WriteConcern.valueOf(prop.getProperty(key)));
                    }
                    else if(key.equalsIgnoreCase("maxAutoConnectRetryTime")) {
                        methodMap.get(key).invoke(builder,Long.parseLong(prop.getProperty(key)));
                    }
                    else {
                        methodMap.get(key).invoke(builder,Integer.parseInt(prop.getProperty(key)));
                    }
                }   catch (Exception ex) {
                    LOG.error(ex.toString());
                }
        }

        return builder.build();
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public Morphia getMorphia() {
        return morphia;
    }
}