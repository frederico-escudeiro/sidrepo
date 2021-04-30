package bonsmalandros.projetoSIDMongo;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

public class LaunchThreads {

	private List<DirectMongoToSQL> threads = new ArrayList<>();
	private String mongo_address;
	private String mongo_database;
	private String mongo_user;
	private String mongo_password;
	private String mongo_authentication;
	private String mongo_replica;
	private String mongo_uri;

	public LaunchThreads() {
		try {
			// TODO COLOCAR PROPRIEDADES DO SQL. Razão: Todas as threads partilham esta info
			// => Rapidez ao iniciar.
			Properties properties = new Properties();
			properties.load(new FileInputStream("DirectMongoToSQL.ini"));

			mongo_address = properties.getProperty("mongo_address");
			mongo_database = properties.getProperty("mongo_database");
			mongo_user = properties.getProperty("mongo_user");
			mongo_password = properties.getProperty("mongo_password");
			mongo_authentication = properties.getProperty("mongo_authentication");
			mongo_replica = properties.getProperty("mongo_replica");
		} catch (Exception properties) {
			System.out.println("Error reading DirectMongoToSQL.ini file " + properties);
		}
		mongo_uri = getMongoURI();
	}

	public void execute() {
		/* LIGAÇAO AO "culturas" DATABASE E BUSCA DAS COLLECTIONS */
		MongoClient mongoClientLocal = new MongoClient(new MongoClientURI(mongo_uri));
		MongoDatabase baseDados = mongoClientLocal.getDatabase(mongo_database);
		MongoIterable<String> listCollections = baseDados.listCollectionNames();

		/* COMEÇA THREADS */
		startThreads(listCollections);
	}

	private void startThreads(Iterable<String> listCollections) {
		for (String collection : listCollections) {
			DirectMongoToSQL thread = new DirectMongoToSQL(collection.substring(6), mongo_database, mongo_uri); // Exemplo
																												// de
																												// formato
																												// collection
																												// -> h1
			threads.add(thread);
			thread.run();
		}
	}

	private String getMongoURI() {

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

	public static void main(String[] args) {
		new LaunchThreads().execute();
	}

}
