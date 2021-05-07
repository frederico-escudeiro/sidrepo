package bonsmalandros.projetoSIDMongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.Random;

public class LancaThreadsPC2 implements MqttCallback {
    private String tempTopic = "temp_sidproj_aadefj";
    private boolean isFirstMessage = true;

    LancaThreadsPC2(){
        try {
            int randomNum = (new Random()).nextInt(100000);
            String tempServer = "tcp://broker.mqtt-dashboard.com:1883";
            MqttClient mqttclient = new MqttClient(tempServer, "CloudToSQL_" + String.valueOf(randomNum) + "_" + tempTopic);
            mqttclient.connect();
            mqttclient.setCallback(this);
            mqttclient.subscribe(tempTopic);
        } catch (MqttException e) {
            e.printStackTrace();
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

        new LancaThreadsPC2();
    }


    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        //LANÇA-SE AQUI PQ SE SABE QUE NAO CHEGAM MAIS MENSAGENS
            System.out.println(mqttMessage.toString());
            lancaThreads(mqttMessage.toString());
    }
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
    public void lancaThreads(String s){

        String [] collections = s.split(",");
        HashMap<String,Integer> map = new HashMap<>();

        map.put(collections[3],1); //h1
        map.put(collections[5],2); //l1
        map.put(collections[4],3); //t1
        map.put(collections[1],4); //h2
        map.put(collections[2],5); //l2
        map.put(collections[0],6); //t2


        /*try {
            for (String collection : collections) {
                System.out.println("sid_g4_aaedfj_" + collection);                                                //E
                //CloudToSQL c2s = new CloudToSQL("sid_g4_aaedfj_" + collection,map.get(collection));
                //c2s.start();
                //c2s.join();
            }
        }catch(InterruptedException e){
            System.out.println("Interrompidas as Threads");
        }*/
    }
}