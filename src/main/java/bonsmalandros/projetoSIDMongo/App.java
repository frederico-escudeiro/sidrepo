package bonsmalandros.projetoSIDMongo;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
	/*----------------------------------------------------------------------------------------*/
	// Cliente Nuvem
	private static MongoClient mongoClientNuvem;
	//mongo -u aluno -p aluno  -authenticationDatabase admin --authenticationMechanism SCRAM-SHA-1 194.210.86.10 
	//mongodb://aluno:aluno@194.210.86.10:27017/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false
	private static String uri = "mongodb://aluno:aluno@194.210.86.10:27017/?authSource=admin&readPreference=primary";
	
	// base de dados sid2021
	private static MongoDatabase mongoDbNuvem;
	private static String db_name = "sid2021";
	
	// collecoes sensor
	private static MongoCollection mongoColNuvemH1;
	private static String col_name_h1 = "sensorh1";
	
	private static MongoCollection mongoColNuvemH2;
	private static String col_name_h2 = "sensorh2";
	
	private static MongoCollection mongoColNuvemL1;
	private static String col_name_l1 = "sensorl1";
	
	private static MongoCollection mongoColNuvemL2;
	private static String col_name_l2 = "sensorl2";
	
	private static MongoCollection mongoColNuvemT1;
	private static String col_name_t1 = "sensort1";
	
	private static MongoCollection mongoColNuvemT2;
	private static String col_name_t2 = "sensort2";
	
	/*----------------------------------------------------------------------------------------*/
	// Cliente Local
	private static MongoClient mongoClientLocal;
	//mongo -u aluno -p aluno  -authenticationDatabase admin --authenticationMechanism SCRAM-SHA-1 194.210.86.10 
	//mongodb://dreamteam:dreamteam@PC1:27000/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false
	private static String uri_local = "mongodb://dreamteam:dreamteam@PC1:27000/?authSource=admin&readPreference=primary";
	
	// base de dados sid2021
	private static MongoDatabase mongoDbLocal;
	private static String db_name_local = "culturas";
	
	// collecao sensor
	private static MongoCollection mongoColLocalDados;
	private static String col_name_local = "dados";
		
	/*----------------------------------------------------------------------------------------*/
	// Tempo 2021-02-24 at 21:38:56 GMT
	private static DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'");
	private static Date currentDate = new Date();
	private static Date lateDate = new Date();
	private static long timeDifMilliSeconds = 1000;		//1000*60*60*35;
		
    public static void main( String[] args )
    {
    	
    	// Data    	
    	long lateTimeMillis = currentDate.getTime() - timeDifMilliSeconds;
    	lateDate.setTime(lateTimeMillis);
    	String lateTime = df1.format(lateDate);
    	
    	// Dados da nuvem
    	ConnectionString connectionString = new ConnectionString(uri);
    	
    	mongoClientNuvem = MongoClients.create(connectionString);
    	
    	mongoDbNuvem = mongoClientNuvem.getDatabase(db_name);
    	
    	mongoColNuvemH1 = mongoDbNuvem.getCollection(col_name_h1);
    	mongoColNuvemH2 = mongoDbNuvem.getCollection(col_name_h2);
    	mongoColNuvemL1 = mongoDbNuvem.getCollection(col_name_l1);
    	mongoColNuvemL2 = mongoDbNuvem.getCollection(col_name_l2);
    	mongoColNuvemT1 = mongoDbNuvem.getCollection(col_name_t1);
    	mongoColNuvemT2 = mongoDbNuvem.getCollection(col_name_t2);
    	
    	// Dados local
    	ConnectionString connectionStringLocal = new ConnectionString(uri_local);
    	
    	 mongoClientLocal= MongoClients.create(connectionStringLocal);
    	
    	mongoDbLocal = mongoClientLocal.getDatabase(db_name_local);
    	
    	mongoColLocalDados= mongoDbLocal.getCollection(col_name_local);
    	
    	// Pesquisas
    	Bson queryFilter = Filters.gte("Data",lateTime);
    	
    	//List<Document> results_h1 = new ArrayList();
    	//mongoColNuvemH1.find(queryFilter).into(results_h1);
    	
    	//List<Document> results_h2 = new ArrayList();
    	//mongoColNuvemH2.find(queryFilter).into(results_h2);
    	
    	//List<Document> results_l1 = new ArrayList();
    	//mongoColNuvemL1.find(queryFilter).into(results_l1);
    	
    	//List<Document> results_l2 = new ArrayList();
    	//mongoColNuvemL2.find(queryFilter).into(results_l2);
    	
    	List<Document> results_t1 = new ArrayList();
    	mongoColNuvemT1.find(queryFilter).into(results_t1);
    	
    	//List<Document> results_t2 = new ArrayList();
    	//mongoColNuvemT2.find(queryFilter).into(results_t2);
    	
    	List<Document> dados_t1 = new ArrayList();
    	//mongoColNuvemT1.find(queryFilter).into(results_t1);
    	
    	//.append("Tempo", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'").parse(res_t1.getString("Data")))
    	for(int i = 0; i != results_t1.size(); i++) {
    		Document res_t1 = results_t1.get(i);
    		Document doc = new Document();
    		try {
    			/*
    			// Data
    			String sData = res_t1.getString("Data");
    			String[] spData = sData.split("'T'");
    			
    			// Calendario
    			String dataCalendario = spData[0];
    			String[] spCalendario = dataCalendario.split("-");
    			String anos = spCalendario[0];
    			String meses = spCalendario[1];
    			String dias = spCalendario[2];
    			
    			// Horario		
    			String dataHorario = spData[1];
    			String dataHorarioLimpo = sData.split("'Z'")[0];
    			String[] spHorario = dataHorarioLimpo.split(":");
    			String horas = spHorario[0];
    			String minutos = spHorario[1];
    			String segundos = spHorario[2];
    			String milisegundos = spHorario[3] + "0";
    			*/
				doc.append("Zona", "1")
				   .append("Tipo","T")
				   .append("Tempo", res_t1.getString("Data"))
				   .append("Medicao", Double.parseDouble(res_t1.getString("Medicao")));
				dados_t1.add(doc);
    		
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
    	}
    	
    	mongoColLocalDados.insertMany(dados_t1);
    	
    	// Prints
    	System.out.println("Data: " + lateTime);
    	
    	//System.out.println("H1: " + results_h1.size());
    	//System.out.println("H2: " + results_h2.size());
    	//System.out.println("L1: " + results_l1.size());
    	//System.out.println("L2: " + results_l2.size());
    	System.out.println("T1: " + results_t1.size());
    	//System.out.println("T2: " + results_t2.size());
    	
        System.out.println("Docs in T1: " + mongoColNuvemT1.countDocuments());
    }
}
