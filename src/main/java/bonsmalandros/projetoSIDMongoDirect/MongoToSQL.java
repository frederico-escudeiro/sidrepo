package bonsmalandros.projetoSIDMongoDirect;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import bonsmalandros.projetoSIDMongo.ValidaMedicoes;

public class MongoToSQL extends Thread {
	//MONGO
	private String mongo_collection;
	private String mongo_database;
	private String mongo_uri;
	//SQL
	private Connection connectionLocalhost;
	private Statement statementLocalhost;
	private Connection connectionCloud;
	private Statement statementCloud;
	// Tempo Datas
	// "%Y-%m-%d'T'%H:%M:%S'Z'"
	private DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private Date currentDate = new Date();
	private Date lateDate = new Date();
	private long timeDifMilliSeconds = 1000; // delay na inserção e leitura de dados (1 segundo)
	private String tipoSensor;
	private String zonaID;
	private int sensorID;
	private double limiteInferior;
	private double limiteSuperior;
	private ValidaMedicoes valida;
	private CheckerThread threadChecker;
	
	

	/* TODO FALTA ADICIONAR OS PARAMETROS SQL */
	public MongoToSQL(String mongo_database, String mongo_uri, String sql_uri, String user_sql,
			String pass_sql,String sql_uri_cloud,String user_sql_cloud,String pass_sql_cloud,
			int check_sql_cloud,int check_if_gets_message, int sensorID, String zonaID, 
			String tipoSensor,double limiteInferior, double limiteSuperior) {
		this.mongo_database = mongo_database;
		this.mongo_uri = mongo_uri;
		this.sensorID = sensorID;
		this.zonaID = zonaID;
		this.tipoSensor = tipoSensor;
		this.limiteInferior = limiteInferior;
		this.limiteSuperior = limiteSuperior;
		//Thread para ver sql prof
		new CheckerThread(check_sql_cloud, true).start();
		//Thread para checkar se recebe mensagens
		threadChecker = new CheckerThread(check_if_gets_message, false);
		valida = new ValidaMedicoes();
		try {
			connectToSQL(sql_uri_cloud, user_sql_cloud, pass_sql_cloud, false);
			connectToSQL(sql_uri, user_sql, pass_sql, true);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



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

	public void run() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		MongoClient client = new MongoClient(new MongoClientURI(mongo_uri));
		MongoDatabase database = client.getDatabase(mongo_database);
		System.out.println("Connection To Mongo Suceeded");
		MongoCollection collection = database.getCollection(mongo_collection);
		List<Document> listDocuments = new ArrayList<>();
		long time = ((new Date()).getTime() - timeDifMilliSeconds) / 1000;
		time = time * 1000;
		currentDate = new Date(time);

		Bson filter = Filters.eq("Data", df1.format(currentDate));
		collection.find(filter).into(listDocuments);
		if (!listDocuments.isEmpty()) {
			// TODO writeSensor(listDocuments);
		}

		while (true) {

			lateDate = currentDate;
			time = ((new Date()).getTime() - timeDifMilliSeconds) / 1000;
			time = time * 1000;
			currentDate = new Date(time);
			// late -> current
			Bson filterLow = Filters.gt("Data", df1.format(lateDate));
			Bson filterUp = Filters.lte("Data", df1.format(currentDate));// para evitar o envio de duplicados
			Bson filterLowAndUp = Filters.and(filterLow, filterUp);
			collection.find(filterLowAndUp).into(listDocuments);
			if (!listDocuments.isEmpty()) {
				;
			}
			System.out.println(/* cloud_topic + */ ": Intervalo: " + lateDate + " -> " + currentDate);
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
	private class CheckerThread extends Thread {
		private int checkTime;
		private boolean isCheckSQL;

		public CheckerThread(int timeCheck, boolean isCheckSQL) {
			this.checkTime = timeCheck;
			this.isCheckSQL = isCheckSQL;
		}

		public void run() {
			if (isCheckSQL) {
				try {

					String sqlQuery = "SELECT * FROM `sensor` WHERE tipo = '" + String.valueOf(tipoSensor)
							+ "' and idzona = " + zonaID;
					while (true) {
						try {
							ResultSet result = statementCloud.executeQuery(sqlQuery);
							while (result.next()) {
								double limSup = result.getDouble(4);
								double limInf = result.getDouble(3);
								System.out.println(
										String.valueOf(tipoSensor).toUpperCase() + sensorID + ": Limite Superior : "
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
							System.out.println("Erro na query na thread " + tipoSensor + " e zona " + zonaID);
						}
					}
				} catch (InterruptedException e) {
					System.out.println("Algo me interrompeu enquanto dormia");
				}
			} else {
				while (true)
					try {
						sleep(checkTime);
						valida.clear();
						String sqlQuery = "CALL `criar_alerta`(NULL, NULL, 'Alerta Sensor sem registar medições', 'Não são recebidas medições há "+checkTime/1000+" segundos.')";
						try {
							statementLocalhost.executeUpdate(sqlQuery);
							
						} catch (SQLException e) {
							System.out.println("erro na querySQL");
						}
						
					} catch (InterruptedException e) {
						System.out.println("Recebeu Mensagem");
					}
			}

		}
	}
}
