package bonsmalandros.projetoSIDMongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import java.util.Collection;
import java.util.List;

public class LancaThreads {

    public static void main(String[] args) {

        String uri_local = "mongodb://dreamteam:dreamteam@PC1:27010,PC1:27011,PC1:27012/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        MongoClient mongoClientLocal = new MongoClient(new MongoClientURI(uri_local));

        MongoDatabase baseDados = mongoClientLocal.getDatabase("culturas");

        MongoIterable<String> listCollections = baseDados.listCollectionNames();

        try {
            for(String a: listCollections){

                    MongoToCloud m2c = new MongoToCloud();
                    //cloud_topic += (i+1);
                    m2c.connectCloud();
                    m2c.start();
                    m2c.join();
                }
        }catch(InterruptedException e){
            System.out.println("Interrompidas as Threads");
        }

        }

    }
