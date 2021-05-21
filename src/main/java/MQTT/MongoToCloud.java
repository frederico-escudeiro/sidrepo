package MQTT;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.lte;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.QueryBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import java.io.FileInputStream;
import java.lang.invoke.StringConcatFactory;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
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
	private String client_name;

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
	private DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private Date currentDate = new Date();
	private Date lateDate = new Date();
	private long timeDifMilliSeconds = 3000; // delay na inserção e leitura de dados (1 segundo)

	public MongoToCloud(String collection, String cloud_server, String cloud_topic, String client_name,
			String mongo_address, String mongo_database, String mongo_user, String mongo_password,
			String mongo_authentication, String mongo_replica) {

		this.cloud_server = cloud_server;
		this.cloud_topic = cloud_topic + "_" + collection.substring(6).toUpperCase();
		System.out.println(this.cloud_topic);
		this.client_name = client_name;
		this.mongo_address = mongo_address;
		this.mongo_database = mongo_database;
		this.mongo_collection = collection;
		this.mongo_user = mongo_user;
		this.mongo_password = mongo_password;
		this.mongo_authentication = mongo_authentication;
		this.mongo_replica = mongo_replica;
		connectToBroker();

	}

	protected String getSaltString() {
		String var1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder var2 = new StringBuilder();
		Random var3 = new Random();

		while (var2.length() < 18) {
			int var4 = (int) (var3.nextFloat() * (float) var1.length());
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

	public String getMongoAdress() {

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
		MongoClient client = new MongoClient(new MongoClientURI(getMongoAdress()));
		MongoDatabase database = client.getDatabase(mongo_database);
		System.out.println("Connection To Mongo Suceeded");
		MongoCollection<Document> collection = database.getCollection(mongo_collection);
		List<Document> listDocuments = new ArrayList<>();
		// TODO perguntar o porque de dividir e depois multiplicar pelo mesmo numero
		long time = ((new Date()).getTime() - timeDifMilliSeconds) / 1000;
		time = time * 1000;
		currentDate = new Date(time);
		Bson filter = Filters.eq("Tempo", currentDate);
		collection.find(filter).into(listDocuments);

		if (!listDocuments.isEmpty()) {
			writeSensor(listDocuments);
		}

		while (true) {
			long lateDate = currentDate.getTime();
			time = ((new Date()).getTime() - timeDifMilliSeconds) / 1000;
			time = time * 1000;
			currentDate = new Date(time);
			long currentTime = currentDate.getTime();

			// APAGAR DAQUI
			long timeDate3 = new Date().getTime();
			Date dateTimeDate3 = new Date(timeDate3);
			System.out.println(cloud_topic
					+ ": Data em que foi iniciada a query de pesquisa na base de dados mongo local : " + sdf.format(dateTimeDate3));
			// APAGAR ATE AQUI

			// APAGAR DAQUI
			System.out.println(cloud_topic + ": Intervalo de procura na base de dados mongo local : "
					+ sdf.format(new Date(lateDate)) + " -> " + sdf.format(new Date(currentTime)));
			// APAGAR ATE AQUI

			Bson filterLow = Filters.gt("Tempo", new Date(lateDate));
			Bson filterUp = Filters.lte("Tempo", new Date(currentTime));// para evitar o envio de duplicados
			Bson filterLowAndUp = Filters.and(filterLow, filterUp);
			collection.find(filterLowAndUp).into(listDocuments);

			// APAGAR DAQUI
			long timeDate4 = new Date().getTime();
			Date dateTimeDate4 = new Date(timeDate4);
			System.out.println(cloud_topic
					+ ": Data em que foi acabada a query de pesquisa na base de dados mongo local : " + sdf.format(dateTimeDate4));
			// APAGAR ATE AQUI
			if (!listDocuments.isEmpty()) {
				writeSensor(listDocuments);
			}
			try {
				time = ((new Date()).getTime() - timeDifMilliSeconds) / 1000;
				time = time * 1000;
				if ((time - currentDate.getTime()) <= 500) {
					sleep(1000);
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	public void writeSensor(List<Document> listDocuments) {
		try {
			for (Document doc : listDocuments) {
				String docToString = dealWithDoc(doc);
				MqttMessage message = new MqttMessage();
				message.setQos(0);
				message.setPayload(docToString.getBytes());
				long timeDate = new Date().getTime();
				Date dateTimeDate = new Date(timeDate);
				System.out.println(cloud_topic + ": Documento : " + docToString);
				System.out.println(cloud_topic + ": Data em que foi enviada para o MQTT : " + sdf.format(dateTimeDate));
				mqttclient.publish(cloud_topic, message);

			}
			listDocuments.clear();

		} catch (MqttException var3) {
			var3.printStackTrace();
		}

	}

	private String dealWithDoc(Document doc) {

		Date date = doc.getDate("Tempo");
		String dateToString = df1.format(date);
		return dateToString + " " + doc.getDouble("Medicao");
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
