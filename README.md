# Contact Management Application
This is a simple commnadline application for managing contacts.

## Features

It has 2 core feature :

* Search existing contacts
  
  For given user name we search through existing contacts and return matching contact information.
  Search ignores case sensitivity. Searching Foo | foo | fOf al returns same result.

* Bulk import from xml file

  First of all we have to decide a xml parsing strategy. There are two widely known choice :

    * DOM

      All data has be loaded fully in memory.     
      We can random access elemets and can validate the xml schema.

    * SAX (Push Parser) 

      This is an sequantial , event driven strategy and only the small parts of data has to be loaded in memory.

  Eventhough our program requirements perfectly match with DOM strategy i choosed SAX here.
 
  First we dont know the file size. If users wants to import gigabytes of data we cant say them no. We need to accept and adept gracefully. Second , dom strategy  is an io blocking way. We need to wait until it's fully loaded. It doesnt scale well. By using SAX we can distribute and process them concurrently.

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
Inside the base folder (where the pom.xml file is located) run the following commands 
    
    mvn install
    mvn exec:java

You can change mongodb configuration by editing mongodb.properties file.