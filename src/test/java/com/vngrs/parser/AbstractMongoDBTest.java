package com.vngrs.parser;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.vngrs.model.Contact;
import com.vngrs.util.MongoDB;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;
import junit.framework.TestCase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public abstract class AbstractMongoDBTest extends TestCase {

    public static final String DB_NAME = "contact_management";
    private MongodExecutable _mongodExe;
    private MongodProcess _mongod;
    private MongoClient _mongoClient;
    private Morphia _morphia;
    private Datastore _datastore;


    @Override
    protected void setUp() throws Exception {

        Logger logger = Logger.getLogger(getClass().getName());
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaultsWithLogger(Command.MongoD, logger)
                .processOutput(ProcessOutput.getDefaultInstanceSilent()).build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
        _mongodExe = runtime.prepare(new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(12345, Network.localhostIsIPv6()))
            .build());

        _mongod = _mongodExe.start();

        super.setUp();

        _mongoClient = new MongoClient("localhost", 12345);
        _morphia = new Morphia();
        _datastore = _morphia.mapPackage(Contact.class.getPackage().getName())
                .createDatastore(_mongoClient, DB_NAME);
        _datastore.ensureIndexes();

        MongoDB.setInstance(_morphia,_datastore);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        _mongod.stop();
        _mongodExe.stop();
    }

    public Datastore getDatastore() {
        return _datastore;
    }

    public Mongo getMongoClient() {
        return _mongoClient;
    }

    public static Pattern getEqualityPattern(String content) {
        return Pattern.compile("^" + content + "$");
    }

}