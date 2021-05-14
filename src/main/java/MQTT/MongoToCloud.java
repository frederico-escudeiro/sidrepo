package MQTT;

import java.util.Arrays;
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
	// private DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS ZZ");
	private Date currentDate = new Date();
	private Date lateDate = new Date();
	private long timeDifMilliSeconds = 6000; // delay na inserção e leitura de dados (1 segundo)

	public MongoToCloud(String collection) {
		try {

			Properties properties = new Properties();
			properties.load(new FileInputStream("MongoToCloud.ini"));
			cloud_server = properties.getProperty("cloud_server");
			cloud_topic = properties.getProperty("cloud_topic") + "_" + collection.substring(6).toUpperCase();
			System.out.println(cloud_topic);
			client_name = properties.getProperty("client_name");
			mongo_address = properties.getProperty("mongo_address");
			mongo_database = properties.getProperty("mongo_database");
			mongo_collection = collection;
			mongo_user = properties.getProperty("mongo_user");
			mongo_password = properties.getProperty("mongo_password");
			mongo_authentication = properties.getProperty("mongo_authentication");
			mongo_replica = properties.getProperty("mongo_replica");
			connectToBroker();
		} catch (Exception properties) {
			System.out.println("Error reading MongoToCloud.ini file " + properties);
		}

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
		// df1.setTimeZone(TimeZone.getTimeZone("UTC"));
		// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss
		// z");
		MongoClient client = new MongoClient(new MongoClientURI(getMongoAdress()));
		MongoDatabase database = client.getDatabase(mongo_database);
		System.out.println("Connection To Mongo Suceeded");
		MongoCollection collection = database.getCollection(mongo_collection);
		List<Document> listDocuments = new ArrayList<>();
		// TODO perguntar o porque de dividir e depois multiplicar pelo mesmo numero
		long time = ((new Date()).getTime() - timeDifMilliSeconds) / 1000;
		time = time * 1000;
		// long time = ((new Date()).getTime() - timeDifMilliSeconds);
		currentDate = new Date(time);

		Bson filter = Filters.eq("Tempo", currentDate);
		collection.find(filter).into(listDocuments);
		if (!listDocuments.isEmpty()) {
			writeSensor(listDocuments);
		}

		while (true) {
//{$and:[{"Tempo":{$gt:ISODate("2021-05-13T15:33:31.000+00:00")}},{"Tempo":{$lte:ISODate("2021-05-13T15:33:33.000+00:00")}}]}
			long lateDate = currentDate.getTime();
			time = ((new Date()).getTime() - timeDifMilliSeconds) / 1000;
			time = time * 1000;
			// time = ((new Date()).getTime() - timeDifMilliSeconds);
			currentDate = new Date(time);
			long currentTime = currentDate.getTime();
//			long numero = 1620920013000L;
//			System.out.println("Numero : "+ numero);
			System.out.println("Late Date -> " + lateDate + " CurrentDate ->" + currentTime);

//			Bson filterBson = and(Arrays.asList(gt("Tempo", 
//			        new java.util.Date(1620920011000L)), lte("Tempo", 
//			                new java.util.Date(1620920013000L))));
//			Bson filterBson =  lte("Tempo", 
//			                new java.util.Date(currentTime));
			// late -> current
			Bson filterLow = Filters.gt("Tempo", new Date(lateDate));
			Bson filterUp = Filters.lte("Tempo", new Date(currentTime));// para evitar o envio de duplicados
			Bson filterLowAndUp = Filters.and(filterLow, filterUp);
			collection.find(filterLowAndUp).into(listDocuments);
			// collection.find().into(listDocuments);
			if (!listDocuments.isEmpty()) {
				System.out.println("Found one");
				writeSensor(listDocuments);
			}
			// System.out.println(cloud_topic + ": Intervalo: " + lateDate + " -> " +
			// currentDate);
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
				mqttclient.publish(cloud_topic, message);
				System.out.println("Documento : " + docToString);
			}
			listDocuments.clear();

		} catch (MqttException var3) {
			var3.printStackTrace();
		}

	}

	private String dealWithDoc(Document doc) {

		Date date = doc.getDate("Tempo");
		String dateToString = df1.format(date);
		System.out.println(dateToString + " " + doc.getDouble("Medicao"));
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

	public static void main(String[] args) {
		MongoToCloud mongoToCloud = new MongoToCloud("sensort1");
		mongoToCloud.start();
		MongoToCloud mongoToCloud2 = new MongoToCloud("sensort2");
		mongoToCloud2.start();
		MongoToCloud mongoToCloud3 = new MongoToCloud("sensorh1");
		mongoToCloud3.start();
		MongoToCloud mongoToCloud4 = new MongoToCloud("sensorh2");
		mongoToCloud4.start();
		MongoToCloud mongoToCloud5 = new MongoToCloud("sensorl1");
		mongoToCloud5.start();
		MongoToCloud mongoToCloud6 = new MongoToCloud("sensorl2");
		mongoToCloud6.start();
		
	}
}
