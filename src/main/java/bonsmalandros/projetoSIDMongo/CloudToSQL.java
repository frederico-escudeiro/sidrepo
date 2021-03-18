package bonsmalandros.projetoSIDMongo;


import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Callable;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class CloudToSQL extends Thread implements MqttCallback {
    private MqttClient mqttclient;
    private String cloud_server;
    private String cloud_topic;
    private String serverSQL; //URI SQL
    private char tipoDoSensor;
    private int idZona;
    private int idSensor;
    private static Connection connectionLocalhost;
    private static Statement statementLocalhost;



    public CloudToSQL(String topic,int idSensor) {
        try {

            Properties properties = new Properties();
            properties.load(new FileInputStream("CloudToSQL.ini"));
            cloud_server = properties.getProperty("cloud_server");
            cloud_topic = topic;
            tipoDoSensor = cloud_topic.substring(14).charAt(0);
            idZona = Integer.parseInt(cloud_topic.substring(15));
            this.idSensor = idSensor;
            connectToSQL();
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

    /*{
        "_id": {
        "$oid": "6036c771967bf6108c5b7ca9"
    },
        "Zona": "Z1",
            "Sensor": "H1",
            "Data": "2021-02-24 at 21:38:56 GMT",
            "Medicao": "24.61639494871795"
    }*/

    void connectToSQL() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String connectionLocalhostURI = "jdbc:mysql://localhost/sid2021";
        connectionLocalhost = DriverManager.getConnection(connectionLocalhostURI, "root", null); //Conectar ao PC pessoal para escrever
        statementLocalhost = connectionLocalhost.createStatement();
    }

    void dealWithData(String message) throws SQLException {
        String[] data_medicao = message.split("(\\{\"Tempo\": \\{\"\\$date\": \")|(\"\\}, \"Medicao\": )|(\\})" );
        System.out.println("Deal with Data: " + data_medicao[1] + " " + data_medicao[2]);
        String data1 = data_medicao[1].replace("T", " ");
        String data1_final = data1.replace("Z", "");
        String procedMedicaoInsert ="CALL `create_medicao`('"+ idSensor +"','"+data1_final+"','"+ data_medicao[2]+"');";
        //String procedMedicaoInsert ="CALL `create_medicao`('3', '2021-03-11 16:29:47', '6');";
        statementLocalhost.executeUpdate(procedMedicaoInsert);
  }

    public void messageArrived(String var1, MqttMessage message) throws Exception {
        System.out.println(cloud_topic + "Entrei " + message.toString());
        //Deal With Data
        dealWithData(message.toString());


    }

    public void connectionLost(Throwable var1) {
    }

    public void deliveryComplete(IMqttDeliveryToken var1) {
    }
}
