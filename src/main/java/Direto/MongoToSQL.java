package Direto;

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

import MQTT.ValidaMedicoes;

public class MongoToSQL extends Thread {
	// MONGO
	private String mongo_collection;
	private String mongo_database;
	private String mongo_uri;
	// SQL
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
	private CheckSensorReadingTimeoutThread threadChecker;

	/* TODO FALTA ADICIONAR OS PARAMETROS SQL */
	public MongoToSQL(String mongo_database, String mongo_uri, String mongo_collection, String sql_uri, String user_sql,
			String pass_sql, String sql_uri_cloud, String user_sql_cloud, String pass_sql_cloud, int check_sql_cloud,
			int check_if_gets_message, int sensorID, String zonaID, String tipoSensor, double limiteInferior,
			double limiteSuperior) {
		this.mongo_database = mongo_database;
		this.mongo_uri = mongo_uri;
		this.mongo_collection = mongo_collection;
		this.sensorID = sensorID;
		this.zonaID = zonaID;
		this.tipoSensor = tipoSensor;
		this.limiteInferior = limiteInferior;
		this.limiteSuperior = limiteSuperior;
		// Thread para ver sql prof

		valida = new ValidaMedicoes();
		try {
			System.out
					.println("SQL_Cloud_Uri: " + sql_uri_cloud + " , user_cloud: " + user_sql_cloud + ", pass_cloud : "
							+ pass_sql_cloud + ", SQL_Uri : " + sql_uri + " , user : " + user_sql + "pass" + pass_sql);
			connectToSQL(sql_uri_cloud, user_sql_cloud, pass_sql_cloud, false);
			connectToSQL(sql_uri, user_sql, pass_sql, true);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new CheckProfessorCloudSensorThread(check_sql_cloud).start();
		// Thread para checkar se recebe mensagens
		threadChecker = new CheckSensorReadingTimeoutThread(check_if_gets_message);
		threadChecker.start();
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

//	public void run() {
//		// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss
//		// z");
//		MongoClient client = new MongoClient(new MongoClientURI(mongo_uri));
//		MongoDatabase database = client.getDatabase(mongo_database);
//		System.out.println("Connection To Mongo Suceeded");
//		MongoCollection collection = database.getCollection(mongo_collection);
//		List<Document> listDocuments = new ArrayList<>();
//		long time = ((new Date()).getTime() - timeDifMilliSeconds) / 1000;
//		time = time * 1000;
//		currentDate = new Date(time);
//
//		Bson filter = Filters.eq("Tempo", currentDate);
//		collection.find(filter).into(listDocuments);
//		if (!listDocuments.isEmpty()) {
//			threadChecker.interrupt();
//			dealWithDataToSQL(listDocuments);
//		}
//
//		while (true) {
//
//			lateDate = currentDate;
//			time = ((new Date()).getTime() - timeDifMilliSeconds) / 1000;
//			time = time * 1000;
//			currentDate = new Date(time);
//			// late -> current
//			Bson filterLow = Filters.gt("Tempo", lateDate);
//			Bson filterUp = Filters.lte("Tempo", currentDate);// para evitar o envio de duplicados
//			Bson filterLowAndUp = Filters.and(filterLow, filterUp);
//			collection.find(filterLowAndUp).into(listDocuments);
//			if (!listDocuments.isEmpty()) {
//				threadChecker.interrupt();
//				dealWithDataToSQL(listDocuments);
//			}
//			System.out.println("Sensor " + zonaID + tipoSensor + ": Intervalo: " + lateDate + " -> " + currentDate);
//			try {
//				time = ((new Date()).getTime() - timeDifMilliSeconds) / 1000;
//				time = time * 1000;
//				if ((time - currentDate.getTime()) <= 500) {
//					sleep(1000);
//				}
//
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//
//		}
//
//	}
	public void run() {
		// df1.setTimeZone(TimeZone.getTimeZone("UTC"));
		// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss
		// z");
		MongoClient client = new MongoClient(new MongoClientURI(mongo_uri));
		MongoDatabase database = client.getDatabase(mongo_database);
		System.out.println("Connection To Mongo Suceeded");
		MongoCollection<Document> collection = database.getCollection(mongo_collection);
		List<Document> listDocuments = new ArrayList<>();
		// TODO perguntar o porque de dividir e depois multiplicar pelo mesmo numero
		long time = ((new Date()).getTime() - timeDifMilliSeconds) / 1000;
		time = time * 1000;
		// long time = ((new Date()).getTime() - timeDifMilliSeconds);
		currentDate = new Date(time);

		Bson filter = Filters.eq("Tempo", currentDate);
		collection.find(filter).into(listDocuments);
		if (!listDocuments.isEmpty()) {
			dealWithDataToSQL(listDocuments);
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
				dealWithDataToSQL(listDocuments);
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

	private void dealWithDataToSQL(List<Document> listDocuments) {
		for (Document document : listDocuments) {
			String docToString = dealWithDoc(document);
			String[] data_medicao = docToString.split(" ");
			String data1 = data_medicao[0].replace("T", " ");
			String data1_final = data1.replace("Z", "");
			// Limites Sensor
			char validacao;
			if (Double.parseDouble(data_medicao[1]) < limiteSuperior
					&& Double.parseDouble(data_medicao[1]) > limiteInferior) {
				validacao = valida.getValidacao(Double.parseDouble(data_medicao[1]));
				;
			} else {
				validacao = 's';
			}
			System.out.println("Limite Superior : " + limiteSuperior + " , Limite Inferior : " + limiteInferior
					+ ", Valor : " + Double.parseDouble(data_medicao[1]) + " ,Validacao : " + validacao);
			String procedMedicaoInsert = "CALL `criar_medicao`('" + sensorID + "','" + data1_final + "','"
					+ data_medicao[1] + "','" + validacao + "');";
			try {
				statementLocalhost.executeUpdate(procedMedicaoInsert);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		listDocuments.clear();

	}

	private String dealWithDoc(Document doc) {

		Date date = doc.getDate("Tempo");
		String dateToString = df1.format(date);
		System.out.println(dateToString + " " + doc.getDouble("Medicao"));
		return dateToString + " " + doc.getDouble("Medicao");
	}

	private class CheckProfessorCloudSensorThread extends Thread {
		private int checkTime;

		public CheckProfessorCloudSensorThread(int timeCheck) {
			this.checkTime = timeCheck;
		}

		public void run() {
			try {

				String sqlQuery = "SELECT * FROM `sensor` WHERE tipo = '" + String.valueOf(tipoSensor)
						+ "' and idzona = " + zonaID;
				System.out.println(sqlQuery);
				while (true) {
					try {
						ResultSet result = statementCloud.executeQuery(sqlQuery);
						while (result.next()) {
							double limSup = result.getDouble(4);
							double limInf = result.getDouble(3);
//							System.out.println(
//									String.valueOf(tipoSensor).toUpperCase() + sensorID + ": Limite Superior : "
//											+ result.getString(4) + " Limite Inferior : " + result.getString(3));
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
				// Depois para tirar o sysout.
				System.out.println("Algo me interrompeu enquanto dormia");
			}

		}
	}

	private class CheckSensorReadingTimeoutThread extends Thread {
		private int checkTime;

		public CheckSensorReadingTimeoutThread(int timeCheck) {
			this.checkTime = timeCheck;
		}

		public void run() {
			while (true)
				try {
					sleep(checkTime);
					valida.clear();
					String sqlQuery = "CALL `criar_alerta`(NULL, NULL, 'Alerta Sensor sem registar medições', 'Não são recebidas medições há "
							+ checkTime / 1000 + " segundos.')";
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
