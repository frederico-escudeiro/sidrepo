package bonsmalandros.projetoSIDMongo;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.*;

import java.util.ArrayList;
import java.util.Set;

public class LancaThreads {



    public static void main(String[] args) {

        String uri_local = "mongodb://dreamteam:dreamteam@PC1:27010,PC1:27011,PC1:27012/?authSource=admin&readPreference=primary";

        MongoClient mongoClientLocal= new MongoClient(new MongoClientURI(uri_local));

        MongoDatabase baseDados = mongoClientLocal.getDatabase("culturas");

        MongoIterable<String> lista =  baseDados.listCollectionNames();

        for(String a :lista) {

            System.out.println(a.substring(6));

        }

    }

}
