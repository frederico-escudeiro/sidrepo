package MQTT;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.eclipse.paho.client.mqttv3.*;

import java.io.FileInputStream;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class LancaThreadsPC1 {
	private List<MongoToCloud> listThreads = new ArrayList<>();

	LancaThreadsPC1() {
		String uri_local = "mongodb://dreamteam:dreamteam@PC1:27010,PC1:27011,PC1:27012/?authSource=admin&readPreference=primary";
		MongoClient mongoClientLocal = new MongoClient(new MongoClientURI(uri_local));
		MongoDatabase baseDados = mongoClientLocal.getDatabase("culturas");
		MongoIterable<String> listCollections = baseDados.listCollectionNames();
		startThreads(listCollections);
	}

	public void startThreads(Iterable<String> listCollections) {
		try {

			Properties properties = new Properties();
			properties.load(new FileInputStream("MongoToCloud.ini"));
			String cloud_server = properties.getProperty("cloud_server");
			String cloud_topic = properties.getProperty("cloud_topic") ;
			System.out.println(cloud_topic);
			String client_name = properties.getProperty("client_name");
			String mongo_address = properties.getProperty("mongo_address");
			String mongo_database = properties.getProperty("mongo_database");
			String mongo_user = properties.getProperty("mongo_user");
			String mongo_password = properties.getProperty("mongo_password");
			String mongo_authentication = properties.getProperty("mongo_authentication");
			String mongo_replica = properties.getProperty("mongo_replica");
			for (String collection : listCollections) {
				System.out.println(collection);
				MongoToCloud m2c = new MongoToCloud(collection, cloud_server, cloud_topic, client_name, mongo_address,
						mongo_database, mongo_user, mongo_password, mongo_authentication, mongo_replica);
				m2c.start();
				listThreads.add(m2c);
			}
		} catch (Exception properties) {
			System.out.println("Error reading MongoToCloud.ini file " + properties);
		}

	}

	public static void main(String[] args) {
		new LancaThreadsPC1();
	}

}
