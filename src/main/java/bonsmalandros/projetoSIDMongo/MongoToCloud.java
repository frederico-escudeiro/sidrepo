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
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MongoToCloud extends Thread implements MqttCallback {
    private MqttClient mqttclient;

    private String cloud_server;
    private String cloud_topic;
    private String client_name = "sid_2021_aadefj"; 

    private String mongo_user;
    private String mongo_password;
    private String mongo_replica;
    private String mongo_address;
    private String mongo_database;
    private String mongo_collection;
    private String mongo_authentication;

    // Tempo Datas
    // "%Y-%m-%d'T'%H:%M:%S'Z'"
    private DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private Date currentDate = new Date();
    private Date lateDate= new Date();
    private long timeDifMilliSeconds = 1000;			//delay na inserção e leitura de dados (1 segundo)

    // variaveis de tempo
    private long startTime = System.nanoTime();
    private long endTime = System.nanoTime();
    private long nanoToMilli = 1000000;

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
            mqttclient = new MqttClient(cloud_server, "MongoToCloud_" + this.client_name + cloud_topic);
            mqttclient.connect();
            mqttclient.setCallback(this);
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
        List<Document> listDocuments = new ArrayList<>();
        //TODO perguntar o porque de dividir e depois multiplicar pelo mesmo numero
        long time = ((new Date()).getTime() - timeDifMilliSeconds)/1000;
        time = time * 1000;
        currentDate = new Date(time);

        Bson filter = Filters.eq("Data" , df1.format(currentDate));
        collection.find(filter).into(listDocuments);
        if(!listDocuments.isEmpty()){
            writeSensor(listDocuments);
        }


        while(true) {

            lateDate = currentDate;
            time = ((new Date()).getTime() - timeDifMilliSeconds)/1000;
            time = time * 1000;
            currentDate = new Date(time);
            //late -> current
            Bson filterLow = Filters.gt("Data" , df1.format(lateDate));
            Bson filterUp = Filters.lte("Data",df1.format(currentDate));// para evitar o envio de duplicados
            Bson filterLowAndUp = Filters.and(filterLow,filterUp);
            collection.find(filterLowAndUp).into(listDocuments);
            if(!listDocuments.isEmpty()){
                writeSensor(listDocuments);
            }
            System.out.println("Intervalo: "+ lateDate +" -> "+currentDate);
            try {
                time = ((new Date()).getTime() - timeDifMilliSeconds)/1000;
                time = time * 1000;
                if((time - currentDate.getTime()) <= 500) {
                    sleep(1000);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }


    public void writeSensor(List<Document> listDocuments) {
        try {
            for(Document doc : listDocuments ){
                String var1 = doc.toJson();
                MqttMessage message = new MqttMessage();
                message.setQos(0);
                message.setPayload(var1.getBytes());
                mqttclient.publish(cloud_topic, message);
                System.out.println("Documento : " +var1 );
            }
            listDocuments.clear();

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
