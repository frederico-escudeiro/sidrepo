package bonsmalandros.projetoSIDMongoDirect;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

public class LaunchThreadsPC2 {

	private List<MongoToDirect> threads = new ArrayList<>();
	// TODO Insert SQL atributes

	public LaunchThreadsPC2() {
		try {
			// TODO COLOCAR PROPRIEDADES DO SQL.
			Properties properties = new Properties();
			properties.load(new FileInputStream("DirectToSQL.ini"));

		} catch (Exception properties) {
			System.out.println("Error reading DirectToSQL.ini file " + properties);
		}
	}

	public void execute() {
		/* LIGAÇAO AO "sid2021" DATABASE */
		
		/* COMEÇA THREADS */
		//startThreads(listCollections);
	}

	private void startThreads(Iterable<String> listCollections) {
	}

	

	public static void main(String[] args) {
		new LaunchThreadsPC2().execute();
	}

}
