package bonsmalandros.projetoSIDMongo;


import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Random;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class CloudToSQL extends Thread implements MqttCallback {
    MqttClient mqttclient;
    static String cloud_server = new String();
    static String cloud_topic = new String();


    public CloudToSQL(String topic) {
        try {
            Properties var1 = new Properties();
            var1.load(new FileInputStream("CloudToSQL.ini"));
            cloud_server = var1.getProperty("cloud_server");
            cloud_topic = topic;
        } catch (Exception e) {
            System.out.println("Error reading CloudToSQL.ini file " + e);
        }
    }


    public void run() {
        try {
            int randomNum = (new Random()).nextInt(100000);
            /*                                  broker,                              clientId                            */
            this.mqttclient = new MqttClient(cloud_server, "CloudToSQL_" + String.valueOf(randomNum) + "_" + cloud_topic);
            this.mqttclient.connect();
            this.mqttclient.setCallback(this);
            this.mqttclient.subscribe(cloud_topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connectSQL() {
        //Connect To SQL database
    }

    public void messageArrived(String var1, MqttMessage message) throws Exception {
        System.out.println(message.toString());
        //Deal With Data
        //Insert into SQL Database

    }

    public void connectionLost(Throwable var1) {
    }

    public void deliveryComplete(IMqttDeliveryToken var1) {
    }
}
