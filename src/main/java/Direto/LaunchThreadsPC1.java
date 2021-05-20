package Direto;

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
import com.mysql.cj.xdevapi.Result;

import MQTT.CloudToSQL;

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
	private String user_sql;
	private String pass_sql;
	private String user_sql_cloud;
	private String pass_sql_cloud;
	private String sql_uri_cloud;
	private int check_if_gets_message;
	private int check_sql_cloud;
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

			sql_uri = properties.getProperty("SQL") + properties.getProperty("ip_SQL")
					+ properties.getProperty("database_SQL");
			user_sql = properties.getProperty("user_SQL");
			pass_sql = properties.getProperty("pass_SQL");

			sql_uri_cloud = properties.getProperty("SQL_Cloud");
			user_sql_cloud = properties.getProperty("user_SQL_Cloud");
			pass_sql_cloud = properties.getProperty("pass_SQL_Cloud");

			check_sql_cloud = Integer.parseInt(properties.getProperty("check_SQL_Cloud"));
			check_if_gets_message = Integer.parseInt(properties.getProperty("check_if_gets_message"));

			connectionCloud = DriverManager.getConnection(properties.getProperty("SQL_Cloud"),
					properties.getProperty("user_SQL_Cloud"), properties.getProperty("pass_SQL_Cloud")); // ler
			statementCloud = connectionCloud.createStatement();
			String sqlGetSensor = "SELECT * FROM `sensor`";
			result = statementCloud.executeQuery(sqlGetSensor);
		} catch (SQLException e) {
			System.out.println("Erro com a ligação ao Professor SQL");
		} catch (Exception properties) {
			System.out.println("Error reading DirectMongoToSQL.ini file " + properties);
		}
		mongo_uri = getMongoURI();
	}

	public void execute() {
//		/* LIGAÇAO AO "culturas" DATABASE E BUSCA DAS COLLECTIONS */
//		System.out.println(mongo_uri);
//		MongoClient mongoClientLocal = new MongoClient(new MongoClientURI(mongo_uri));
//		MongoDatabase baseDados = mongoClientLocal.getDatabase(mongo_database);
//		MongoIterable<String> listCollections = baseDados.listCollectionNames();

		/* COMEÇA THREADS */
		startThreads( result);
	}

	private void startThreads( ResultSet result) {
		String[] collections = {"sensorh1","sensorl1","sensort1","sensorh2","sensorl2","sensort2"};
		int[] sensorID = { 1, 4, 2, 5, 3, 6 };
		for (int i =0 ; i!= collections.length ; i++) {
//			System.out.println(collections[i]);

			MongoToSQL thread;
			try {
				result.next();
//				System.out.println("Coleção : " + collections[i]);
//				System.out.println("IDSensor: " + sensorID + " ,Zona : " + result.getString(1) + " ,Sensor :"
//						+ result.getString(2) + " ,Limite inferior: " + result.getDouble(3) + " ,Limite Superior: "
//						+ result.getDouble(4));

				thread = new MongoToSQL(mongo_database, mongo_uri, collections[i], sql_uri, user_sql, pass_sql,
						sql_uri_cloud, user_sql_cloud, pass_sql_cloud, check_sql_cloud, check_if_gets_message,
						sensorID[i], result.getString(1), result.getString(2), result.getDouble(3),
						result.getDouble(4));

				threads.add(thread);
				thread.start();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		return address + "&readPreference=primary";
	}

	public static void main(String[] args) {
		new LaunchThreadsPC1().execute();
	}

}
