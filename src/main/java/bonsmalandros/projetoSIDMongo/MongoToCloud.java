package bonsmalandros.projetoSIDMongo;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MongoToCloud extends Thread implements MqttCallback {
    private MqttClient mqttclient;

    private String cloud_server;
    private String cloud_topic;

    private String mongo_user;
    private String mongo_password;
    private String mongo_replica;
    private String mongo_address;
    private String mongo_database;
    private String mongo_collection;
    //String mongo_fieldquery;
    //String mongo_fieldvalue;
    private String delete_document;
    private String loop_query;
    private String create_backup;
    private String backup_collection;
    private String display_documents;
    private String seconds_wait;
    private String mongo_authentication;

    public MongoToCloud(String collection, String topic){
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("MongoToCloud.ini"));
            cloud_server = properties.getProperty("cloud_server");
            cloud_topic = topic;
            mongo_address = properties.getProperty("mongo_address");
            mongo_database = properties.getProperty("mongo_database");
            mongo_collection = collection;
            mongo_user = properties.getProperty("mongo_user");
            mongo_password = properties.getProperty("mongo_password");
            mongo_authentication = properties.getProperty("mongo_authentication");
            mongo_replica = properties.getProperty("mongo_replica");
            //mongo_fieldquery = properties.getProperty("mongo_fieldquery");
            //mongo_fieldvalue = properties.getProperty("mongo_fieldvalue");
            delete_document = properties.getProperty("delete_document");
            create_backup = properties.getProperty("create_backup");
            backup_collection = properties.getProperty("backup_collection");
            seconds_wait = properties.getProperty("delay");
            loop_query = properties.getProperty("loop_query");
            display_documents = properties.getProperty("display_documents");

        } catch (Exception properties) {
            System.out.println("Error reading MongoToCloud.ini file " + properties);
        }
    }

    protected String getSaltString() {
        String var1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder var2 = new StringBuilder();
        Random var3 = new Random();

        while(var2.length() < 18) {
            int var4 = (int)(var3.nextFloat() * (float)var1.length());
            var2.append(var1.charAt(var4));
        }

        String var5 = var2.toString();
        return var5;
    }

    public void connectToBroker() {
        try {
            mqttclient = new MqttClient(cloud_server, "MongoToCloud" + this.getSaltString() + cloud_topic);
            mqttclient.connect();
            mqttclient.setCallback(this);
            //mqttclient.subscribe(cloud_topic); //Não precisa de dar subscribe, é devido a Leitura
            System.out.println("Connection To Cloud Suceeded");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public String getMongoAdress(){

        String address = "mongodb://";
        if (mongo_authentication.equals("true")) {
            address = address + mongo_user + ":" + mongo_password + "@";
        }

        address = address + mongo_address;
        if (!mongo_replica.equals("false")) {
            if (mongo_authentication.equals("true")) {
                address = address + "/?replicaSet=" + mongo_replica + "&authSource=admin";
            } else {
                address = address + "/?replicaSet=" + mongo_replica;
            }
        } else if (mongo_authentication.equals("true")) {
            address = address + "/?authSource=admin";
        }
        return address;
    }

    public void run() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        MongoClient client = new MongoClient(new MongoClientURI(getMongoAdress()));
        MongoDatabase database = client.getDatabase(mongo_database);
        System.out.println("Connection To Mongo Suceeded");
        MongoCollection collection = database.getCollection(mongo_collection);
        //MongoCollection collectionBackup = database.getCollection(backup_collection);
        Document document = new Document();

        /*if (!mongo_fieldquery.equals("null")) {
            document.put(mongo_fieldquery, mongo_fieldvalue);
        }*/

        boolean isNotLooping = false;
        int idDocument = 0;
        while(!isNotLooping) {
            Date currentDate = new Date(System.currentTimeMillis());
            System.out.println(dateFormat.format(currentDate) + "\n");
            FindIterable findIterable = collection.find(document);
            MongoCursor mongoCursor = findIterable.projection(Projections.excludeId()).iterator();

            //PROCURAR E ENVIAR DOCUMENTOS
            while(mongoCursor.hasNext()) {
                ++idDocument;
                Document tempDocument = (Document)mongoCursor.next();
                String dadosJson = tempDocument.toJson();
                if (display_documents.equals("true")) {
                    System.out.println(dadosJson);
                }

                /* TODO (poderá ter problemas na inserção).
                if (create_backup.equals("true")) {
                    collectionBackup.insertOne(tempDocument);
                }
                */

                this.writeSensor(dadosJson);

                if (!seconds_wait.equals("0")) {
                    try {
                        Thread.sleep((long)Integer.parseInt(seconds_wait));
                    } catch (Exception e1) {
                    }
                }
            }

           /* if (delete_document.equals("true")) {
                if (!mongo_fieldquery.equals("null")) {
                    collection.deleteMany(Filters.eq(mongo_fieldquery, mongo_fieldvalue));
                }

                if (mongo_fieldquery.equals("null")) {
                    database.getCollection(mongo_collection).drop();
                }
            } */

            if (!loop_query.equals("true")) {
                isNotLooping = true;
            }
        }

    }


    public void writeSensor(String var1) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(var1.getBytes());
            mqttclient.publish(cloud_topic, message);
        } catch (MqttException var3) {
            var3.printStackTrace();
        }

    }

    @Override
    public void connectionLost(Throwable throwable) {
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
}
