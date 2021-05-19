
package MQTT;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class CloudToSQL extends Thread implements MqttCallback {
	private MqttClient mqttclient;
	private String cloud_server;
	private String cloud_topic;
	private char tipoDoSensor;
	private int idZona;
	private int idSensor;
	private double limiteInferior;
	private double limiteSuperior;
	private static Connection connectionLocalhost;
	private static Statement statementLocalhost;
	private static Connection connectionCloud;
	private static Statement statementCloud;
	private CheckSensorReadingTimeoutThread threadChecker;
	private ValidaMedicoes valida;
	private DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public CloudToSQL(int sensorID, String zonaID, String tipoSensor, double limiteInferior, double limiteSuperior,
			String cloud_server, String cloud_topic, String SQL_prof_uri, String SQL_profUser, String SQL_profPass,
			String SQL_uri, String SQL_User, String SQL_Pass, int timerCheckCloudProf, int timerCheckIfGetsMessages) {
		try {
			this.cloud_server = cloud_server;
			this.tipoDoSensor = tipoSensor.charAt(0);
			idZona = Integer.parseInt(zonaID);
			this.idSensor = sensorID;
			this.limiteInferior = limiteInferior;
			this.limiteSuperior = limiteSuperior;
			this.cloud_topic = cloud_topic + "_" + tipoSensor + idZona;
			System.out.println(this.cloud_topic);
			connectToSQL(SQL_uri, SQL_User, SQL_Pass, true);
			connectToSQL(SQL_prof_uri, SQL_profUser, SQL_profPass, false);
			new CheckProfessorCloudSensorThread(timerCheckCloudProf).start();
			threadChecker = new CheckSensorReadingTimeoutThread(timerCheckIfGetsMessages);
			valida = new ValidaMedicoes();
			threadChecker.start();
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println("Erro no connect");
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
		String[] data_medicao = message.split(" ");
		System.out.println("Data :" + data_medicao[0] + ", Valor_Medicao" + data_medicao[1]);
		String data1 = data_medicao[0].replace("T", " ");
		String data1_final = data1.replace("Z", "");
		char validacao;

		if (Double.parseDouble(data_medicao[1]) < limiteSuperior
				&& Double.parseDouble(data_medicao[1]) > limiteInferior) {
			validacao = valida.getValidacao(Double.parseDouble(data_medicao[1]));// i ou v 

		} else {
			validacao = 's';
		}
		System.out.println("Limite Superior : " + limiteSuperior + " , Limite Inferior : " + limiteInferior
				+ ", Valor : " + Double.parseDouble(data_medicao[1]) + " ,Validacao : " + validacao);
		String procedMedicaoInsert = "CALL `criar_medicao`('" + idSensor + "','" + data1_final + "','" + data_medicao[1]
				+ "','" + validacao + "');";
				
				
		try {
			statementLocalhost.executeUpdate(procedMedicaoInsert);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//metodo da interface
	public void messageArrived(String var1, MqttMessage message) throws Exception {
		threadChecker.interrupt();
		dealWithData(message.toString());

	}
	//metodo da interface
	public void connectionLost(Throwable var1) {
	}
	//metodo da interface
	public void deliveryComplete(IMqttDeliveryToken var1) {
	}
	
	
	private class CheckProfessorCloudSensorThread extends Thread {
		private int checkTime;

		public CheckProfessorCloudSensorThread(int timeCheck) {
			this.checkTime = timeCheck;
		}

		public void run() {
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
												+ result.getString(4) + " Limite Inferior : " + result.getString(3));
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
					//Depois para tirar o sysout.
					System.out.println("Algo me interrompeu enquanto dormia");
				}
			

		}
	}
	private class CheckSensorReadingTimeoutThread extends Thread{
		private int checkTime;
		
		public CheckSensorReadingTimeoutThread(int timeCheck) {
			this.checkTime = timeCheck;
		}
		
		public void run() {
			while (true)
				try {
					sleep(checkTime);
					valida.clear();
					String sqlQuery="";
					
						sqlQuery = "CALL `criar_alerta`(NULL, NULL, 'Alerta Sensor "+ tipoDoSensor + idZona+" sem registar medições', 'Não são recebidas medições há "
								+ checkTime / 1000 + " segundos.')";
					
						try {
							statementLocalhost.executeUpdate(sqlQuery);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

				

				} catch (InterruptedException e) {
					System.out.println("Recebeu Mensagem");
				}
		}
		
	}

	public static void main(String[] args) {

		// new CloudToSQL(3, "1", "T", 2.0, 50.0).start();
	}
}
