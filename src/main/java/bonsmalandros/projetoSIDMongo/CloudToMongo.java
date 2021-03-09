package bonsmalandros.projetoSIDMongo;


import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.util.JSON;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Random;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class CloudToMongo implements MqttCallback {
    MqttClient mqttclient;
    static MongoClient mongoClient;
    static DB db;
    static DBCollection mongocol;
    static String mongo_user = new String();
    static String mongo_password = new String();
    static String mongo_address = new String();
    static String cloud_server = new String();
    static String cloud_topic = new String();
    static String mongo_host = new String();
    static String mongo_replica = new String();
    static String mongo_database = new String();
    static String mongo_collection = new String();
    static String display_documents = new String();
    static String mongo_authentication = new String();



    public CloudToMongo() {
    }

    public static void main(String[] var0) {

        try {
            Properties var1 = new Properties();
            var1.load(new FileInputStream("CloudToSQL.ini"));
            mongo_address = var1.getProperty("mongo_address");
            mongo_user = var1.getProperty("mongo_user");
            mongo_password = var1.getProperty("mongo_password");
            mongo_replica = var1.getProperty("mongo_replica");
            cloud_server = var1.getProperty("cloud_server");
            cloud_topic = var1.getProperty("cloud_topic");
            mongo_host = var1.getProperty("mongo_host");
            mongo_database = var1.getProperty("mongo_database");
            mongo_authentication = var1.getProperty("mongo_authentication");
            mongo_collection = var1.getProperty("mongo_collection");
            display_documents = var1.getProperty("display_documents");
        } catch (Exception e) {
            System.out.println("Error reading CloudToMongo.ini file " + e);
        }

        (new CloudToMongo()).connectCloud();
        //(new CloudToMongo()).connectSQL();
    }

    public void connectCloud() {
        try {
            int randomNum = (new Random()).nextInt(100000);
            /*                                  broker,                              clientId                            */
            this.mqttclient = new MqttClient(cloud_server, "CloudToMongo_" + String.valueOf(randomNum) + "_" + cloud_topic);
            this.mqttclient.connect();
            this.mqttclient.setCallback(this);
            this.mqttclient.subscribe(cloud_topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connectSQL() {
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
        db = (DB) client.getDatabase(mongo_database);
        mongocol = db.getCollection(mongo_collection);
    }

    public void messageArrived(String var1, MqttMessage message) throws Exception {
        try {
            //DBObject var3 = (DBObject)JSON.parse(var2.toString());
            //mongocol.insert(new DBObject[]{var3});

            //if (display_documents.equals("true")) {
                System.out.println(message.toString());
                /* Tratamento da mensagem*/


            //}
        } catch (Exception e) {
            System.out.println("Exception " + e);
        }

    }

    public void connectionLost(Throwable var1) {
    }

    public void deliveryComplete(IMqttDeliveryToken var1) {
    }
}
