package bonsmalandros.projetoSIDMongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;
import java.util.Random;

public class LancaThreadsPC2 implements MqttCallback {

    public LancaThreadsPC2 (){

    }


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
    public void connectCloud(){
        String cloud_server = "tcp://broker.mqtt-dashboard.com:1883";
        String cloud_topic = "sid_g4_aaedfj_collNames";
        try {
            MqttClient mqttclient = new MqttClient(cloud_server, "MongoToCloud" + getSaltString() + cloud_topic);
            mqttclient.connect();
            mqttclient.setCallback(this);
            mqttclient.subscribe(cloud_topic); //Não precisa de dar subscribe, é devido a Leitura
            System.out.println("Connection To Cloud Suceeded");

        } catch (MqttException var3) {
            var3.printStackTrace();
        }
    }

    public static void main(String[] args) {

        LancaThreadsPC2 lt = new LancaThreadsPC2();
        lt.connectCloud();

    }


    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        System.out.println(mqttMessage.toString());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void lancaThreads(String mensagem){
        try{
        String [] topics = mensagem.split(",");
        for (int i = 0 ; i!=topics.length ; i++) {
            if (i != topics.length - 1) {
                CloudToSQL thread = new CloudToSQL(topics[i]);
                thread.run();
                thread.join();

            }
        }
        }catch(InterruptedException e){
                System.out.println("Interrompidas as Threads");
            }

        }
    }

