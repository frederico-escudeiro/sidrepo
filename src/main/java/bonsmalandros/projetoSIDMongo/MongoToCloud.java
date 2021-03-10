package bonsmalandros.projetoSIDMongo;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
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
    static MqttClient mqttclient;

    static String cloud_server = new String();
    static String cloud_topic = new String();

    static String mongo_user = new String();
    static String mongo_password = new String();

    static DBCollection table;
    static String mongo_replica = new String();
    static String mongo_address = new String();
    static String mongo_database = new String();
    static String mongo_collection = new String();
    static String mongo_criteria = new String();
    static String mongo_fieldquery = new String();
    static String mongo_fieldvalue = new String();
    static String delete_document = new String();
    static String loop_query = new String();
    static String create_backup = new String();
    static String backup_collection = new String();
    static String display_documents = new String();
    static String seconds_wait = new String();
    static String mongo_authentication = new String();

    public MongoToCloud(String cloudTopic,String collection){
        try {
        Properties properties = new Properties();
        properties.load(new FileInputStream("MongoToCloud.ini"));
        cloud_server = properties.getProperty("cloud_server");
        cloud_topic = cloudTopic;
        mongo_address = properties.getProperty("mongo_address");
        mongo_database = properties.getProperty("mongo_database");
        mongo_collection = collection;
        mongo_user = properties.getProperty("mongo_user");
        mongo_password = properties.getProperty("mongo_password");
        mongo_authentication = properties.getProperty("mongo_authentication");
        mongo_replica = properties.getProperty("mongo_replica");
        mongo_fieldquery = properties.getProperty("mongo_fieldquery");
        mongo_fieldvalue = properties.getProperty("mongo_fieldvalue");
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

    public static void main(String[] args) {


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

    public void connectCloud() {
        try {
            mqttclient = new MqttClient(cloud_server, "MongoToCloud" + this.getSaltString() + cloud_topic);
            mqttclient.connect();
            mqttclient.setCallback(this);
            mqttclient.subscribe(cloud_topic); //Não precisa de dar subscribe, é devido a Leitura
            System.out.println("Connection To Cloud Suceeded");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        //new String();
        //new String();
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

        MongoClient client = new MongoClient(new MongoClientURI(address));
        MongoDatabase database = client.getDatabase(mongo_database);
        System.out.println("Connection To Mongo Suceeded");
        MongoCollection collection = database.getCollection(mongo_collection);
        System.out.println("col " + mongo_collection);
        MongoCollection collectionBackup = database.getCollection(backup_collection);
        Document document = new Document();
        if (!mongo_fieldquery.equals("null")) {
            document.put(mongo_fieldquery, mongo_fieldvalue);
        }

        boolean isNotLooping = false;
        int loopNumber = 0;
        int idDocument = 0;



        while(!isNotLooping) {
            //System.out.println("loop number ....." + loopNumber);
            Date currentDate = new Date(System.currentTimeMillis());
            System.out.println(dateFormat.format(currentDate) + "\n");
            //this.writeSensor("{Loop:" + loopNumber + "}");
            FindIterable findIterable = collection.find(document);
            MongoCursor var14 = findIterable.iterator();
            int var15 = 1;
            MongoCursor mongoCursor = findIterable.projection(Projections.excludeId()).iterator();

            while(mongoCursor.hasNext()) {
                ++loopNumber;
                ++var15;
                ++idDocument;
                new Document();
                Document tempDocument = (Document)mongoCursor.next();
                String dadosJson = tempDocument.toJson();
                dadosJson = "{id:" + idDocument + ", doc:" + dadosJson + "}";
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

            if (delete_document.equals("true")) {
                if (!mongo_fieldquery.equals("null")) {
                    collection.deleteMany(Filters.eq(mongo_fieldquery, mongo_fieldvalue));
                }

                if (mongo_fieldquery.equals("null")) {
                    database.getCollection(mongo_collection).drop();
                }
            }

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
