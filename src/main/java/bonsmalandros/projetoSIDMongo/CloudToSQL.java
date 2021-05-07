package bonsmalandros.projetoSIDMongo;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.mongodb.internal.connection.Time;

public class CloudToSQL extends Thread implements MqttCallback {
    private MqttClient mqttclient;
    private String cloud_server;
    private String cloud_topic;
    private String serverSQL; // URI SQL
    private char tipoDoSensor;
    private int idZona;
    private int idSensor;
    private double limiteInferior;
    private double limiteSuperior;
    private static Connection connectionLocalhost;
    private static Statement statementLocalhost;
    private static Connection connectionCloud;
    private static Statement statementCloud;

    public CloudToSQL(int sensorID, String zonaID, String tipoSensor, double limiteInferior, double limiteSuperior) {
        try {

            Properties properties = new Properties();
            properties.load(new FileInputStream("CloudToSQL.ini"));
            cloud_server = properties.getProperty("cloud_server");
            this.tipoDoSensor = tipoSensor.charAt(0);
            idZona = Integer.parseInt(zonaID);
            this.idSensor = sensorID;
            this.limiteInferior = limiteInferior;
            this.limiteSuperior = limiteSuperior;
            cloud_topic = properties.getProperty("cloud_topic") + "_" + tipoSensor + idZona;
            System.out.println(cloud_topic);
            connectToSQL(properties.getProperty("SQL"), properties.getProperty("user_SQL"),
                    properties.getProperty("pass_SQL"), true);
            connectToSQL(properties.getProperty("SQL_Cloud"), properties.getProperty("user_SQL_Cloud"),
                    properties.getProperty("pass_SQL_Cloud"), false);
            new CheckerThread(Integer.parseInt(properties.getProperty("check_SQL_Cloud")), true).start();
        } catch (Exception e) {
            System.out.println("Error reading CloudToSQL.ini file ");
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            int randomNum = (new Random()).nextInt(100000);
            /* broker, clientId */
            this.mqttclient = new MqttClient(cloud_server,
                    "CloudToSQL_" + String.valueOf(randomNum) + "_" + cloud_topic);
            this.mqttclient.connect();
            this.mqttclient.setCallback(this);
            this.mqttclient.subscribe(cloud_topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /*
     * { "_id": { "$oid": "6036c771967bf6108c5b7ca9" }, "Zona": "Z1", "Sensor":
     * "H1", "Data": "2021-02-24 at 21:38:56 GMT", "Medicao": "24.61639494871795" }
     */

    void connectToSQL(String SQLDataBaseURI, String user, String pwd, boolean isLocal)
            throws SQLException, ClassNotFoundException {
        if (isLocal) {
            connectionLocalhost = DriverManager.getConnection(SQLDataBaseURI, user, pwd);
            statementLocalhost = connectionLocalhost.createStatement();
        } else {
            connectionCloud = DriverManager.getConnection(SQLDataBaseURI, user, pwd);
            statementCloud = connectionCloud.createStatement();

        }
    }

    void dealWithData(String message) {
        System.out.println(message);
        // String[] data_medicao = message.split("(\\{\"Tempo\": \\{\"\\$date\":
        // \")|(\"\\}, \"Medicao\": )|(\\})");

        // TEREMOS QUE MUDAR ISTO PARA ADAPTAR PARA O NOSSO PROBLEMA
        String data_medicao_1 = message.replace("{Zona: \"Z1\", Sensor: \"T1\", Data: \"", "");
        String data_medicao_2 = data_medicao_1.replace("\", Medicao: \"", " ");
        String data_medicao_3 = data_medicao_2.replace("\" }", "");
        String[] data_medicao = data_medicao_3.split(" ");

        System.out.println("Deal with Data: " + data_medicao[0] + " " + data_medicao[1]);
        String data1 = data_medicao[0].replace("T", " ");
        String data1_final = data1.replace("Z", "");
        char validacao;
        if (Double.parseDouble(data_medicao[1]) < limiteSuperior
                && Double.parseDouble(data_medicao[1]) > limiteInferior) {
            validacao = 'v';
        } else {
            validacao = 'i';
        }
        String procedMedicaoInsert = "CALL `criar_medicao`('" + idSensor + "','" + data1_final + "','" + data_medicao[1]
                + "','" + validacao + "');";
        System.out.println(procedMedicaoInsert);
        try {
            statementLocalhost.executeUpdate(procedMedicaoInsert);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void messageArrived(String var1, MqttMessage message) throws Exception {
        System.out.println(cloud_topic + ": Entrei " + message.toString());
        dealWithData(message.toString());

    }

    public void connectionLost(Throwable var1) {
    }

    public void deliveryComplete(IMqttDeliveryToken var1) {
    }

    private class CheckerThread extends Thread {
        private int checkTime;
        private boolean isCheckSQL;

        public CheckerThread(int timeCheck, boolean isCheckSQL) {
            this.checkTime = timeCheck;
            this.isCheckSQL = isCheckSQL;
        }

        public void run() {
            if (isCheckSQL) {
                try {

                    String sqlQuery = "SELECT * FROM `sensor` WHERE tipo = '" + String.valueOf(tipoDoSensor)
                            + "' and idzona = " + idZona;
                    while (true) {
                        try {
                            ResultSet result = statementCloud.executeQuery(sqlQuery);
                            while (result.next()) {
                                double limSup = result.getDouble(4);
                                double limInf = result.getDouble(3);
                                System.out.println(
                                        String.valueOf(tipoDoSensor).toUpperCase() + idSensor + ": Limite Superior : "
                                                + result.getString(3) + " Limite Inferior : " + result.getString(4));
                                if (limSup != limiteSuperior) {
                                    limiteSuperior = limSup;
                                }
                                if (limInf != limiteInferior) {
                                    limiteInferior = limInf;
                                }
                                // TODO Store procedure para alterar a zona e o tipo caso mude
                            }
                            sleep(checkTime);

                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("Erro na query na thread " + tipoDoSensor + " e zona " + idZona);
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("Algo me interrompeu enquanto dormia");
                }
            }else {
                while(true)
                    try {
//					CALL `criar_alerta`(NULL, NULL, 'Alerta Valor de Medição Fora dos Limites do Sensor', 'Foi registada uma medição com um valor que ultrapassa os limites de hardware do sensor.');

                        sleep(checkTime);
                    }catch (InterruptedException e) {
                        System.out.println("");
                    }
            }

        }
    }

	public static void main(String[] args) {
  		new CloudToSQL(3,"1","T",2.0,50.0).start();
    }
}