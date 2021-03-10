package bonsmalandros.projetoSIDMongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class LancaThreadsPC1 {

    public static String getSaltString() {
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

    public static String toList(MongoIterable<String> lista){
        String oneStringTopic = "";
        for (String collection : lista) {
            oneStringTopic += "sid_g4_aaedfj_"+collection.substring(6)+ ",";
        }

            return oneStringTopic;

    }

    public static void main(String[] args) {
        String cloud_server = "tcp://broker.mqtt-dashboard.com:1883";
        String cloud_topic = "sid_g4_aaedfj_collNames";

        String uri_local = "mongodb://dreamteam:dreamteam@PC1:27010,PC1:27011,PC1:27012/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        MongoClient mongoClientLocal = new MongoClient(new MongoClientURI(uri_local));

        MongoDatabase baseDados = mongoClientLocal.getDatabase("culturas");

        MongoIterable<String> listCollections = baseDados.listCollectionNames();

        try {
            MqttClient mqttclient = new MqttClient(cloud_server, "MongoToCloud" + getSaltString() + cloud_topic);
            mqttclient.connect();
            mqttclient.subscribe(cloud_topic); //Não precisa de dar subscribe, é devido a Leitura
            System.out.println("Connection To Cloud Suceeded");
            String message = toList(listCollections);
            MqttMessage messageMQTT = new MqttMessage();
            messageMQTT.setPayload(message.getBytes());
            mqttclient.publish(cloud_topic, messageMQTT);

        } catch (MqttException var3) {
            var3.printStackTrace();
        }




        try {
            for(String collection: listCollections){
                    String topic = "sid_g4_aaedfj_"+collection.substring(6);
                    MongoToCloud m2c = new MongoToCloud(topic,collection);
                    System.out.println(topic);
                    m2c.connectCloud();
                    m2c.start();
                    m2c.join();
                }
        }catch(InterruptedException e){
            System.out.println("Interrompidas as Threads");
        }

        }

    }
