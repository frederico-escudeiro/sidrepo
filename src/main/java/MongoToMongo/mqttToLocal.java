package MongoToMongo;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class mqttToLocal extends Thread implements MqttCallback {

	private String dataBase;
	private String mongoURI;
	private String collection;
	private MqttClient mqttclient;
	private MongoClient mongoClientLocal;
	private MongoDatabase mongoDbLocal;
	private MongoCollection<Document> mongoColLocal;
	private DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public mqttToLocal(String mongoURI, String dataBase, String collection) {
		this.mongoURI = mongoURI;
		this.dataBase = dataBase;
		this.collection = collection;
		connectToLocal();

	}
	
	private void connectToLocal() {
		ConnectionString connectionStringLocal = new ConnectionString(mongoURI);
   	 	mongoClientLocal= MongoClients.create(connectionStringLocal);
   	 	mongoDbLocal = mongoClientLocal.getDatabase(dataBase);
   	 	mongoColLocal = mongoDbLocal.getCollection(collection);
	}

	public void run() {
		try {
			mqttclient = new MqttClient("tcp://broker.mqtt-dashboard.com:1883",
					"MQTT_to_Local" + "sid_g4_aaedfj_" + collection);
			mqttclient.connect();
			mqttclient.setCallback(this);
			System.out.println("Connection To Cloud Suceeded");
			this.mqttclient.subscribe("sid_g4_aaedfj_" + collection);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable cause) {

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		dealWithData(message.toString());
	}

	private void dealWithData(String message) {
		String data_medicao_1 = message.replace("{Zona: \"Z1\", Sensor: \"T1\", Data: \"", "");
		String data_medicao_2 = data_medicao_1.replace("\", Medicao: \"", " ");
		String data_medicao_3 = data_medicao_2.replace("\" }", "");
		String[] data_medicao = data_medicao_3.split(" ");
		
		//System.out.println("Data: "+ data_medicao[0]+", Medicao: "+ data_medicao[1]);
		Document doc = new Document();
		try {
			
		    df1.setTimeZone(TimeZone.getTimeZone("UTC"));
		    Date gmtTime = df1.parse(data_medicao[0]);
			doc.append("Tempo", gmtTime)
				.append("Medicao", Double.parseDouble(data_medicao[1]));
			System.out.println("Data: "+ df1.parse(data_medicao[0])+", Medicao: "+ Double.parseDouble(data_medicao[1]));
		} catch (NumberFormatException | ParseException e) {
			System.out.println("Erro na passagem de String para Date");
			e.printStackTrace();
		}
		mongoColLocal.insertOne(doc);
		
		String format = "yyyy/MM/dd HH:mm:ss";
	    SimpleDateFormat sdf = new SimpleDateFormat(format);

	    // Convert Local Time to UTC (Works Fine)
	    

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {

	}

}
