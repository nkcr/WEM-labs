package ch.mse.hesso.webmining.data;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import java.net.UnknownHostException;

public class MongoService {

    private static MongoClient mongoClient;
    private static DBCollection collection;

    private static final String DOCKER_IP_ADDRESS = "192.168.99.100";

    public MongoService(){
        if(mongoClient == null) {
            try {
                mongoClient = new MongoClient(DOCKER_IP_ADDRESS);
                collection = mongoClient.getDB("news").getCollection("crawler");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public void store(DBObject dbObject) {
        collection.insert(dbObject);
    }

}
