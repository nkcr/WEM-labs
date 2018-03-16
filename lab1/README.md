# Web Mining LAB1

Christe, Kocher, Castella

The goal of this lab is to crawl the [local.ch](https://local.ch) online directory and 
to index the results in Solr.  


## Start a Solr instance using Docker

* run the script *docker.start.sh* (localted in the **/infra** folder)
* run the script *docker.stop.sh* to kill the container

One core named **localch** will be created in Solr


## Start the crawler

```bash
mvn clean install
java -jar target/webmininglab1-1.0-SNAPSHOT-jar-with-dependencies.jar
```

