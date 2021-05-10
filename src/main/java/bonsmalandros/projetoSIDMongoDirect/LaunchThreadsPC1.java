package bonsmalandros.projetoSIDMongoDirect;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

import bonsmalandros.projetoSIDMongo.CloudToSQL;

public class LaunchThreadsPC1 {

	private List<MongoToSQL> threads = new ArrayList<>();
	private String mongo_address;
	private String mongo_database;
	private String mongo_user;
	private String mongo_password;
	private String mongo_authentication;
	private String mongo_replica;
	private String mongo_uri;
	private String serverSQL; // URI SQL
	private char tipoDoSensor;
	private int idZona;
	private int idSensor;
	private double limiteInferior;
	private double limiteSuperior;
	private String sql_uri;
	private static Connection connectionLocalhost;
	private static Statement statementLocalhost;
	private static Connection connectionCloud;
	private static Statement statementCloud;
	private static ResultSet result;
	
	public LaunchThreadsPC1() {
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
			connectionCloud = DriverManager.getConnection(properties.getProperty("SQL_Cloud"), properties.getProperty("user_SQL_Cloud"), properties.getProperty("pass_SQL_Cloud"));																					// ler
			statementCloud = connectionCloud.createStatement();
			String sqlGetSensor = "SELECT * FROM `sensor`";
			result = statementCloud.executeQuery(sqlGetSensor);
			sql_uri = properties.getProperty("SQL") +properties.getProperty("ip_SQL") + properties.getProperty("database_SQL");
		} catch (SQLException e) {
			System.out.println("Erro com a ligação ao Professor SQL");
		}catch (IOException e) {
			System.out.println("Erro com o ficheiro CloudToSQL.ini");
		}catch (Exception properties) {
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
		startThreads(listCollections, result);
	}

	private void startThreads(Iterable<String> listCollections, ResultSet result) {
		int sensorID = 1; 
		for (String collection : listCollections) {
			
		
			try {
				System.out.println("IDSensor: "+sensorID+" ,Zona : " + result.getString(1) + " ,Sensor :" + result.getString(2)+" ,Limite inferior: "+result.getDouble(3)+" ,Limite Superior: "+result.getDouble(4));
		
			
			sensorID = sensorID+1;
			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MongoToSQL thread = new MongoToSQL(collection, mongo_database, mongo_uri, sensorID,result.getString(1),result.getString(2),result.getDouble(3),result.getDouble(4));
			threads.add(thread);
			thread.start();
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
		new LaunchThreadsPC1().execute();
	}

}
