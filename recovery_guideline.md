**GEONETWORK guideline**

How to create .war

To have a customized version of Geonetwork, it has been cloned a repo from GIT on the CSScat01 (10.8.244.29) machine. This repo is linked to the official repo of the core-geonetwork project.

In this way, it is possible to edit specific folders and to create a .war file that contains the customizations.

To create the .war file, it is necessary to

1. connect to the VM with root administrative rights
2. pen the folder /opt/core-geonetwork/
  1. cd /opt/core-geonetwork/
3. then enter the following commands:
  1. git submodule init
  2. git submodule update
  3. mvn install -DskipTests
4. this will create a .war file in the folder /opt/core-geonetwork/web/target
  1. /opt/core-geonetwork/web/target/geonetwork.war
5. It is possible to test this .war file running the following command from the folder /opt/core-geonetwork/:
  1. cd web; mvn -Djetty.port=9090 jetty:run
  2. open your browser and insert the following line in the panel:
[http://10.8.244.29:9090/geonetwork/srv/eng/catalog.search#/search?any=](http://10.8.244.29:9090/geonetwork/srv/eng/catalog.search#/search?any=)

In this way you can access a test version of Geonetwork to check if your edits have been correctly implemented.

1. If the customization is correct the next step is to save this .war file in the tomcat web app folder.

Run Geonetwork in Tomcat

Once the .war file is available, before deploying it, it is necessary to stop the Tomcat service:

[root@CSScat01 web]# service tomcat stop

Then move to the web app folder of Geonetwork

[root@CSScat01 web]# cd /var/lib/tomcat/webapps/

It is important to remove the existing .war file and the geonetwork folder already existing. A possible solution is to mv the geonetwork folder in another folder location to be sure that if some specific files are lost, they can be restored.

Then it is possible to copy or move the .war file created before in this folder and restart the Tomcat service:

[root@CSScat01 web]# service tomcat start

Once the deployment is finished, the new Geonetwork version is available here:

[http://edp-portal.eurac.edu/geonetwork/srv/eng/catalog.search#/search?any=](http://edp-portal.eurac.edu/geonetwork/srv/eng/catalog.search#/search?any=)

Geonetwork customization

As a rule of thumb, it is suggested to insert your customization in the folder of the source code under /opt/core-geonetwork because this will be directly reflected in the created .war file once it is deployed.

On the contrary, it is possible to customize the running geonetwork editing the folder in /var/lib/tomcat/webapps/geonetwork/

In the following lines, all the customization performed are listed.

- SAVE THE LOGO

To save a logo that can be used in the metadata, it must be copied here:

/var/lib/tomcat/webapps/geonetwork/WEB-INF/data/data/resources/images/harvesting/

or, better,

/opt/core-geonetwork/web/src/main/webapp/images/harvesting/

- ENABLE SYMBOLIC LINK

To enable symbolic link, it is necessary to create the following .xml file /mnt/CEPHFS\_CSS\_EDP\_RW/geonetwork/META-INF/context.xml

\&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?\&gt;

\&lt;Context path=&quot;/mnt/CEPHFS\_CSS\_EDP\_RW/geonetwork&quot; allowLinking=&quot;true&quot;\&gt;

\&lt;/Context\&gt;

Source: [https://isocra.com/2008/01/following-symbolic-links-in-tomcat/](https://isocra.com/2008/01/following-symbolic-links-in-tomcat/)

- SET THE INSPIRE THEME .RDF

The thesaurus file needs to be upload when a new geonetwork version is installed. It is useful for selecting the keywords for the INSPIRE theme. It can be done directly using the GUI in the Classification System section under Settings. Or it can be saved directly here:

/opt/core-geonetwork/web/src/test/resources/thesaurus/external/thesauri/theme/httpinspireeceuropaeutheme-theme.rdf

- SET THE CATEGORY ITEM

To set as visible the &quot;category&quot; item in the main search page of Geonetwork, it is necessary to edit the following file:

/opt/core-geonetwork/web/target/geonetwork/WEB-INF/config-summary.xml

and insert the following lines (in italic):

\&lt;summaryType name=&quot;details&quot; format=&quot;DIMENSION&quot;\&gt;

\&lt;item facet=&quot;type&quot; translator=&quot;codelist:gmd:MD\_ScopeCode&quot;/\&gt;

\&lt;item facet=&quot;mdActions&quot;/\&gt;

_\&lt;item facet=&quot;category&quot; max=&quot;99&quot; sortBy=&quot;value&quot;_

_translator=&quot;db:org.fao.geonet.repository.MetadataCategoryRepository:findOneByName&quot;/\&gt;_

For a better collection of information, it is necessary to comment the following line:

\&lt;item facet=&quot;type&quot; translator=&quot;codelist:gmd:MD\_ScopeCode&quot;/\&gt;

- SET ENGLISH AS DEFAULT LANGUAGE

To set English as default language it is necessary to edit the following file:

/opt/core-geonetwork/web/src/main/webResources/WEB-INF/config.properties

and save it in the following way:

language.default=eng

language.forceDefault=true

- VISIBLE MAP IN THE FULL VIEW

DELETE port 8080 in _setting -> catalog server_ to have a visible map in the full view

- DISABLE INSPIRE STRICT RULES

INSPIRE strict rules can be turned off from _admin -> metadata and template -> validation_ and ignore this level of validation.

Database configuration

Once the Geonetwork configuration is ready, it is important to connect it to the database where the data are stored. There are two files to be edited:

- /opt/core-geonetwork/web/src/main/webResources/WEB-INF/config-db/jdbc.properties

where all the information about the database connection is stored or to be inserted

- /opt/core-geonetwork/web/src/main/webResources/WEB-INF/config-node/srv.xml

where it is possible to select which database to be used.

JAVA Variable configuration file

To set an appropriate Tomcat configuration values, please use this file:

- /usr/share/tomcat/conf/tomcat.conf

For example, to customize the data directory it is important to add the following line:

CATALINA\_OPTS="-Dgeonetwork.dir=/mnt/CEPHFS_CSS_EDP_RW/data"
