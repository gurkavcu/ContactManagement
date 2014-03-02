# Contact Management Application
This is a simple commnadline application for managing contacts.

## Features

It has 2 core feature :

* Search existing contacts
  
  For given user name we search through existing contacts and return matching contact information.
  Search ignores case sensitivity. Searching Foo | foo | fOf al returns same result.

* Bulk import from xml file
  
  For given file path we parse xml file and write collected contacts to the mongodb.

##  Bulk Importing

  First of all we have to decide a xml parsing strategy. There are two widely known choice :

    * DOM

      * Pros : We can random access elemets and validate the xml schema.      
      * Cons : All data has to be fully loaded into memory. IO Blocking

    * SAX (Push Parser) 

      * Pros : This is an sequantial , event driven strategy and only the small parts of data has to be loaded in memory.
      * Cons : We cant validate xml schema.
      
  Eventhough our program requirements perfectly match with DOM strategy i choosed SAX here.
 
  First we dont know the file size. If user wants to import gigabytes of data we cant say them no. We need to accept and adept gracefully. Second , dom strategy  is an io blocking way. We need to wait until it's fully loaded. It doesnt scale well. By using SAX we can distribute and process them concurrently.

  For smaller files merging contacts in memory and writing to the database is more efficient. At the start of parsing if the file size is smaller than a threshold value we collect contacts and merge them in memory. Then we save them concurrently.

  If its not small enough to manage in memory we skip this process and save them concurrently.You can set this threshold value by giving -size arguments to program. By default its set to 20 MB.

**Sample import file**
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<contacts>
  <contact>
    <name>Foo</name>
    <lastName>Bar</lastName>
    <phone>+90 xxx xxxxxxx</phone>
  </contact>
</contacts>
```

## Requirements
*   JDK6+
*   Maven3+ 
*   MongoDB 2.4+


## Getting started
Inside the base folder (where the pom.xml file is located) run the following commands :
    
    mvn install
    mvn exec:java

You can also set file size threshold value by giving -size argument :

    mvn exec:java -Dexec.args="-size 1048576"

## MongoDb Configuration

You can change mongodb configuration by editing mongodb.properties file. By default program tries to connect localhost : 27017. If you have more than one node you can define in configuration.