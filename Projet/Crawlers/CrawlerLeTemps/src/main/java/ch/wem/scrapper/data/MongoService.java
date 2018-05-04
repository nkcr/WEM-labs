package ch.wem.scrapper.data;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;

public class MongoService {

   private static MongoClient mongoClient;
   private static DBCollection collection;

   public MongoService(){
       if(mongoClient == null) {
           try {
               String conn = getConnectionString();
               MongoClientURI mongoClientURI = new MongoClientURI(conn);
               mongoClient = new MongoClient(mongoClientURI);
               collection = mongoClient.getDB("news").getCollection("articles");
           } catch (UnknownHostException e) {
               e.printStackTrace();
           }
       }
   }
   public void store(DBObject dbObject) {
       collection.save(dbObject);
   }

    public static String getConnectionString(){
        //String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        //String appConfigPath = rootPath + "app.properties";
        Properties appProps = new Properties();
        try {
            appProps.load(MongoService.class.getClass().getResourceAsStream("/app.properties"));
            final String ENV = System.getenv("ENV");
            System.out.println("Current ENV:" + ENV);
            if(ENV.equals("PROD")){
                return appProps.getProperty("mongo.connection.string");
            } else
                return appProps.getProperty("mongo.connection.string.local");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}


