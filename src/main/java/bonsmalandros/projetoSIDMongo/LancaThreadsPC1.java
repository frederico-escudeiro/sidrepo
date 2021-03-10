package bonsmalandros.projetoSIDMongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.eclipse.paho.client.mqttv3.*;
import java.util.Random;

public class LancaThreadsPC1 implements MqttCallback{

    LancaThreadsPC1(String topicos){
        try {

            String tempTopic = "temp_sidproj";
            String tempServer = "tcp://broker.mqtt-dashboard.com:1883";

            int randomNum = (new Random()).nextInt(100000);
            MqttClient mqttclient = new MqttClient(tempServer, "CloudToSQL_" + randomNum + "_" + tempTopic);
            mqttclient.connect();
            mqttclient.setCallback(this);
            mqttclient.subscribe(tempTopic);

            MqttMessage message = new MqttMessage();
            message.setPayload(topicos.getBytes());
            mqttclient.publish(tempTopic, message);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void startThreads(Iterable <String> listCollections){
        String topic;
        try {
            for(String collection: listCollections){
                topic = "sid_g4_aaedfj_" + collection.substring(6);
                System.out.println(topic);
                MongoToCloud m2c = new MongoToCloud(collection,topic);
                m2c.connectToBroker();
                m2c.start();
                m2c.join();
            }
        }catch(InterruptedException e){
            System.out.println("Interrompidas as Threads");
        }
    }


    //TODO getSaltString pode ser retirado.
    protected static String getSaltString() {
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

    public static void main(String[] args) {

        /* LIGAÇAO AO "culturas" DATABASE E BUSCA DAS COLLECTIONS*/
        String uri_local = "mongodb://dreamteam:dreamteam@PC1:27010,PC1:27011,PC1:27012/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        MongoClient mongoClientLocal = new MongoClient(new MongoClientURI(uri_local));
        MongoDatabase baseDados = mongoClientLocal.getDatabase("culturas");
        MongoIterable<String> listCollections = baseDados.listCollectionNames();

        /* OBTÉM A STRING COM OS TOPICOS */
        String topicos = "";
        for(String c : listCollections){
            topicos += (c.substring(6) + ",");
        }

        //ENVIA PARA O PC2 E LANÇA THREADS
        new LancaThreadsPC1(topicos).startThreads(listCollections);
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
