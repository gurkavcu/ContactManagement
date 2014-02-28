package com.vngrs.util;

import com.vngrs.model.Contact;
import com.vngrs.parser.ContactParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.Option;
import java.io.*;
import java.util.List;
import java.util.Scanner;


public class CommandLineOptions {

    @Option(name="-s",usage="find the contacts whose name's matches this")
    public String userName;

    @Option(name="-i",usage="bulk insert this file")
    public File bulkFile;

    private static final Logger LOG = LogManager.getLogger(CommandLineOptions.class.getName());

    private String readLine(String format, Object... args) throws IOException {
        if (System.console() != null) {
            return System.console().readLine(format, args);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
        return reader.readLine();
    }

    public int showMainMenu() {
        int option = 0;
        System.out.println("");
        System.out.println("[1].Search Contact");
        System.out.println("[2].Import Contacts");
        System.out.println("[3].Exit\n");
        System.out.print("  Select Option : ");
        option = new Scanner(System.in).nextInt();
        return option;
    }

    public String showSearchMenu() {
        String name = "";
        System.out.println("");
        System.out.print("  Please enter contact name : ");
        try {
            name = readLine("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }

    public String showImportMenu() {
        String path = "";
        System.out.println("");
        System.out.print("  Please enter the file path : ");
        try {
            path = readLine("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public void searchContact(String name) {
        List<Contact> contactList = Contact.findContact(name);
        if(contactList.size() > 0) {
            System.out.println(String.format("\n  Found %d contact => \n",contactList.size()));
            System.out.println("  "+Contact.toString(contactList));
        }
        else {
            System.out.println("\n  No matching records found");
        }
    }

    public void importFile(String path) {
        try {
            System.out.println("\n  Importing contacts ...");
            ContactParser.ParseResult result = ContactParser.parseContact(new File(path));
            System.out.println(String.format("  %d contact imported to mongodb - Took %d milliseconds",result.count,result.time));
        } catch (FileNotFoundException e) {
            System.out.println("\n1  File not found");
        }
    }

    public void menuLoop() {
        try {
            while( true ) {
                switch (showMainMenu()) {
                    case 1  : {
                                String name = showSearchMenu();
                                if(name != null && !name.isEmpty()) {
                                    searchContact(name.trim());
                                }
                                break;
                              }
                    case 2  : {
                                String path = showImportMenu();
                                if(path != null && !path.isEmpty()) {
                                    importFile(path);
                                }
                                break;
                               }
                    case 3  : return;
                    default : continue;
                }
            }
        }
        catch(Exception ex) {
            menuLoop();
        }
    }

}
