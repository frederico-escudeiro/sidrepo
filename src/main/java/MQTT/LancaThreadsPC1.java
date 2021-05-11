package MQTT;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.eclipse.paho.client.mqttv3.*;

import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.List;
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
		for (String collection : listCollections) {
			System.out.println(collection);
			MongoToCloud m2c = new MongoToCloud(collection);
			listThreads.add(m2c);
			// m2c.start();
		}
	}

	public static void main(String[] args) {
		new LancaThreadsPC1();
	}

}
