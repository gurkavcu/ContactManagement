package com.vngrs;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mongodb.util.Util;
import com.vngrs.model.Contact;
import com.vngrs.util.CommandLineOptions;
import com.vngrs.util.MongoDB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;
import org.mongodb.morphia.Morphia;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;


public class App {

    private static final Logger LOG = LogManager.getLogger(App.class.getName());

    static {
        MongoDB.instance();
    }

    public static void main(String args[]) {

        CommandLineOptions options = new CommandLineOptions();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            LOG.error("Invalid parameter : {} ",e.toString());
            LOG.error("Sample parameter usage : -i bulkFile.xml -s username\n");
            parser.printUsage(System.err);
            return;
        }

        options.menuLoop();
    }

}
