package bonsmalandros.projetoSIDMongo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class LeituraMedicoes extends Thread{
	// Cliente Nuvem
	private MongoClient mongoClientNuvem;
	private String uriNuvem = "mongodb://aluno:aluno@194.210.86.10:27017/?authSource=admin&readPreference=primary";
	private MongoDatabase mongoDbNuvem;
	private String db_name_nuvem = "sid2021";
	private MongoCollection mongoColNuvem;
	private String col_name_nuvem = "sensort1";
	
	// Cliente Local
	private MongoClient mongoClientLocal;
	private String uri_local = "mongodb://dreamteam:dreamteam@PC1:27010,PC1:27011,PC1:27012/?authSource=admin&readPreference=primary";
	private MongoDatabase mongoDbLocal;
	private String db_name_local = "culturas";
	private MongoCollection mongoColLocal;
	private String col_name_local = "sensort1";
	
	// Tempo Datas
	// "%Y-%m-%d'T'%H:%M:%S'Z'"
	private DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private Date currentDate = new Date();
	private Date lateDate= new Date();
	private long timeDifMilliSeconds = 1000;			//delay na inserção e leitura de dados (1 segundo)
	private long timeKeepMilliSeconds = 1000*60*60*12;	//tempo que os dados vão estar guardados (12 horas)
	private long numberOfDocsRemoved = 200;
	
	// Variáveis do tempo de execução de tarefas
	private long startTime = System.nanoTime();
	private long endTime = System.nanoTime();
	private long nanoToMilli = 1000000;

	// Mensagens temporais
	String headerMessage;
	
	String cleanDatabaseMessage;
	String countDatabaseMessage;
	String numOfDeletesMessage;
	
	String buildResultListMessage;
	String writeDatabaseMessage;
	String readDatabaseMessage;
	String numOfReadsMessage;
	String intervalDataMessage;
	
	String dataDistributionMessage;
	String sLastDate;
	// Mensagens
	
	public LeituraMedicoes(String uriNuvem,String db_name_nuvem,String col_name_nuvem,String uri_local,String db_name_local,String col_name_local,long timeDifMilliSeconds, long timeKeepMilliSeconds, long numberOfDocsRemoved) {
		super();
		//Nuvem
		this.uriNuvem = uriNuvem;
		this.db_name_nuvem = db_name_nuvem;
		this.col_name_nuvem = col_name_nuvem;
		
		//Local
		this.uri_local = uri_local;
		this.db_name_local = db_name_local;
		this.col_name_local = col_name_local;
		
		this.timeDifMilliSeconds = timeDifMilliSeconds;
		this.timeKeepMilliSeconds = timeKeepMilliSeconds;
		this.numberOfDocsRemoved = numberOfDocsRemoved;
		
		headerMessage = String.format("Thread coleccao da nuvem: %s\nColeccao local: %s\n", col_name_nuvem, col_name_local);
		
		// Dados da nuvem
    	ConnectionString connectionStringNuvem = new ConnectionString(uriNuvem);
		System.out.println(uriNuvem);
    	mongoClientNuvem = MongoClients.create(connectionStringNuvem);
    	mongoDbNuvem = mongoClientNuvem.getDatabase(db_name_nuvem);
    	mongoColNuvem = mongoDbNuvem.getCollection(col_name_nuvem);
    	
    	// Dados local
    	ConnectionString connectionStringLocal = new ConnectionString(uri_local);
   	 	mongoClientLocal= MongoClients.create(connectionStringLocal);
   	 	mongoDbLocal = mongoClientLocal.getDatabase(db_name_local);
   	 	mongoColLocal = mongoDbLocal.getCollection(col_name_local);
   	 	
   	 	

   	 	// Delete All documents from collection Using blank BasicDBObjec
   	 	//BasicDBObject document = new BasicDBObject();
   	 	//mongoColLocal.deleteMany(document);
  	 	
   	 	this.recoverData();
   	 	
   	 	/*
   	 	// Limpar a memória antiga
   	 	startTime = System.nanoTime();
   	 	long timeMillis = (new Date()).getTime() - timeKeepMilliSeconds;
    	Bson queryFilter_clean = Filters.lt("Tempo",new Date(timeMillis));
    	mongoColLocal.deleteMany(queryFilter_clean);
    	endTime = System.nanoTime();
    	cleanDatabaseMessage = this.getMessageExecutionTask(this.startTime,this.endTime,"Limpeza Dados local");
    	*/
   	 	this.deleteLocal();
   	 	
    	System.out.print(headerMessage + countDatabaseMessage  + numOfDeletesMessage + cleanDatabaseMessage + "\n");
    	
    	
	}
	
	private String getMessageDataDistrib(Map<String,Integer> messageMap) {
		String message = "Data distribution:\n";
		for(Map.Entry m:messageMap.entrySet()){  
			message = message + m.getKey()+": "+m.getValue()+"\n";  
		}
		return message;
	}
	
	private String getMessageExecutionTask(long startTime,long endTime,String task) {
		return String.format("%s : %d ms\n", task, (endTime - startTime)/nanoToMilli);
	}
	
	/*
	// Função que escreve na base de dados local
	private void writeLocal(List<Document> results) {
		
		numOfReadsMessage = String.format("Numero de documentos lidos: %d \n", results.size());
		
		startTime = System.nanoTime();
		List<Document> dados = new ArrayList();
		if(results.size() != 0) {
			for(int i = 0; i != results.size(); i++) {
				Document res_t1 = results.get(i);
				try {
					//Date fromDate = df1.parse(res_t1.getString("Data"));
					Date fromDate = res_t1.getDate("Data");
					List<String> medicoes = res_t1.getList("Medicoes", String.class);
					for(int j = 0; j!= medicoes.size(); j++) {
						Document doc = new Document();
						doc.append("Tempo", new Date(fromDate.getTime()))
							.append("Medicao", Double.parseDouble(medicoes.get(j)));
						dados.add(doc);
        		}
				} catch (Exception e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				
				}
			}
		}
		endTime = System.nanoTime();
		buildResultListMessage = this.getMessageExecutionTask(this.startTime,this.endTime,"Modificacao dos dados");

		startTime = System.nanoTime();
		if(results.size() != 0) {
			mongoColLocal.insertMany(dados);
		}
		endTime = System.nanoTime();
		writeDatabaseMessage = this.getMessageExecutionTask(this.startTime,this.endTime,"Escrita dos dados");
		
		dados.clear();
	}
	*/
	
	// Função que escreve na base de dados local
		private void writeLocal(List<Document> results) {
			
			numOfReadsMessage = String.format("Numero de documentos lidos: %d \n", results.size());
			
			startTime = System.nanoTime();
			List<Document> dados = new ArrayList();
			Map<String,Integer> countData = new HashMap<String,Integer>();
			if(results.size() != 0) {
				for(int i = 0; i != results.size(); i++) {
					Document res_t1 = results.get(i);
					try {
						//Date fromDate = df1.parse(res_t1.getString("Data"));
							String fromDate = res_t1.getString("Data");
							if(countData.containsKey(fromDate)) {
								countData.put(fromDate, countData.get(fromDate)+1);
							}
							else {
								countData.put(fromDate, 1);
							}
							Document doc = new Document();
							doc.append("Tempo", df1.parse(fromDate))
								.append("Medicao", Double.parseDouble(res_t1.getString("Medicao")));
							dados.add(doc);
	        		
					} catch (Exception e) {
					// TODO Auto-generated catch block
						e.printStackTrace();
					
					}
				}
			}
			endTime = System.nanoTime();
			buildResultListMessage = this.getMessageExecutionTask(this.startTime,this.endTime,"Modificacao dos dados");

			startTime = System.nanoTime();
			if(results.size() != 0) {
				mongoColLocal.insertMany(dados);
			}
			endTime = System.nanoTime();
			writeDatabaseMessage = this.getMessageExecutionTask(this.startTime,this.endTime,"Escrita dos dados");
			dataDistributionMessage = this.getMessageDataDistrib(countData);
			dados.clear();
			countData.clear();
		}
		
	public void recoverData() {
		Date dateBegin;
		Date dateEnd;
		Document  result;
		Bson sortOrder = Filters.eq("Tempo", -1L);
		result = (Document) mongoColLocal.find().sort(sortOrder).limit(1).iterator().tryNext();
		
		if(result != null) {
			
			Date lastDate = result.getDate("Tempo");
			sLastDate = "Ultima Data: " + df1.format(lastDate) + "\n";
			System.out.println(lastDate);
		
			long t1 = (new Date()).getTime() - timeDifMilliSeconds - timeKeepMilliSeconds;
			long tlast = lastDate.getTime();
		
			if(tlast < t1)
				dateBegin = new Date(t1);
			else
				dateBegin = new Date(tlast);
		}else {
			long t1 = new Date().getTime() - timeDifMilliSeconds - timeKeepMilliSeconds;
			dateBegin = new Date(t1);
			sLastDate = "Ultima Data: -- (Coleção vazia) \n";
		}
		
		long t0 = (new Date()).getTime() - timeDifMilliSeconds;
		dateEnd = new Date(t0);
		
		List<Document> results = new ArrayList<>();
		Bson queryFilterLower = Filters.gt("Data",df1.format(dateBegin));
		Bson queryFilterUpper = Filters.lte("Data",df1.format(dateEnd));
		Bson queryFilterTogether = Filters.and(queryFilterLower,queryFilterUpper);
		mongoColNuvem.find(queryFilterTogether).into(results);
		lateDate=dateEnd;
		this.writeLocal(results);
	}
		
	
	
	public void deleteLocal() {
		// Limpar a memória antiga
   	 	startTime = System.nanoTime();
   	 	long timeMillis = (new Date()).getTime() - timeKeepMilliSeconds - timeDifMilliSeconds;
    	Bson queryFilter_clean = Filters.lt("Tempo",new Date(timeMillis));
    	long numDocs = mongoColLocal.count(queryFilter_clean);
    	endTime = System.nanoTime();
    	countDatabaseMessage = this.getMessageExecutionTask(this.startTime,this.endTime,"Pesquisa de dados antigos");
    	numOfDeletesMessage = "Numero de documentos a remover: " + 0 + "\n";
    	startTime = System.nanoTime();
    	if(numDocs>this.numberOfDocsRemoved) {
    		mongoColLocal.deleteMany(queryFilter_clean);
    		numOfDeletesMessage = "Numero de documentos a remover: " + numDocs + "\n";
    	}
    	endTime = System.nanoTime();
    	cleanDatabaseMessage = this.getMessageExecutionTask(this.startTime,this.endTime,"Limpeza Dados local");
	}
	
	public void run() {
		boolean cond = true;
		
		long time = (new Date()).getTime() - timeDifMilliSeconds;
		currentDate = new Date(time);
		intervalDataMessage = "Interval: " + df1.format(lateDate) + " -- " + df1.format(currentDate) + "\n";
	
		List<Document>  results = new ArrayList<>();
		
		startTime = System.nanoTime();
		
		
		
		
		/*
		Bson queryFilterTogether = Filters.gte("$expr", Arrays.asList(Filters.eq("$dateFromString", Filters.eq("dateString", "$Data")), 
				currentDate));
		//mongoColNuvem.find(queryFilterTogether).into(results);
		*/
		
		Bson queryFilterLower = Filters.gt("Data",df1.format(lateDate));
		Bson queryFilterUpper = Filters.lte("Data",df1.format(currentDate));
		Bson queryFilterTogether = Filters.and(queryFilterLower,queryFilterUpper);
		mongoColNuvem.find(queryFilterTogether).into(results);
		
		/*
    	Bson queryFilter = Filters.eq("Data",df1.format(currentDate));
    	mongoColNuvem.find(queryFilter).into(results);
    	*/
		
    	/*
    	//Codigo com match sugerido pelo professor
		Bson matchStage = Aggregates.match(Filters.eq("Data",currentDate));
		
		Bson projectStage =new Document("$project", 
			    new Document("Data", 
			    	    new Document("$dateFromString", 
			    	    new Document("dateString", "$_id.Data")))
			    	            .append("Medicoes", 1L));
		
		Bson groupStage = new Document("$group", 
			    new Document("_id", 
			    	    new Document("Data", "$Data"))
			    	            .append("Medicoes", 
			    	    new Document("$push", "$Medicao")));
		AggregateIterable<Document> iterable = mongoColNuvem.aggregate(Arrays.asList(groupStage,projectStage,matchStage)).allowDiskUse(true);
		iterable.into(results);
		*/
	
		
    	endTime = System.nanoTime();
    	readDatabaseMessage = this.getMessageExecutionTask(this.startTime,this.endTime,"Leitura dos dados");
    	
    	if(results!=null) {
    		writeLocal(results);
    	}
		while(cond) {
			deleteLocal();
			System.out.println(headerMessage + intervalDataMessage + readDatabaseMessage + numOfReadsMessage + buildResultListMessage + writeDatabaseMessage + countDatabaseMessage + dataDistributionMessage + numOfDeletesMessage + cleanDatabaseMessage );
			lateDate = currentDate;
			
			time = (new Date()).getTime() - timeDifMilliSeconds;
			currentDate = new Date(time);
			intervalDataMessage = "Interval: " + df1.format(lateDate) + " -- " + df1.format(currentDate) + "\n";
			startTime = System.nanoTime();
			
			results.clear();
			
			queryFilterLower = Filters.gt("Data",df1.format(lateDate));
			queryFilterUpper = Filters.lte("Data",df1.format(currentDate));
			queryFilterTogether = Filters.and(queryFilterLower,queryFilterUpper);
			
			/*
			queryFilterTogether =Filters.and(Filters.gte("$expr", Arrays.asList(Filters.eq("$dateFromString", Filters.eq("dateString", "$Data")), 
					lateDate)), Filters.lt("$expr", Arrays.asList(Filters.eq("$dateFromString", Filters.eq("dateString", "$Data")), 
							currentDate)));
			*/
			
			mongoColNuvem.find(queryFilterTogether).into(results);
			
			
			/*
			//Codigo com match sugerido pelo professor
			matchStage = Aggregates.match(Filters.and(Filters.gt("Data",lateDate),Filters.lte("Data", currentDate)));
			
			projectStage =new Document("$project", 
				    new Document("Data", 
				    	    new Document("$dateFromString", 
				    	    new Document("dateString", "$_id.Data")))
				    	            .append("Medicoes", 1L));
			
			groupStage = new Document("$group", 
				    new Document("_id", 
				    	    new Document("Data", "$Data"))
				    	            .append("Medicoes", 
				    	    new Document("$push", "$Medicao")));

			iterable = mongoColNuvem.aggregate(Arrays.asList(groupStage,projectStage,matchStage)).allowDiskUse(true);
			iterable.into(results);
			*/
			
			endTime = System.nanoTime();
			readDatabaseMessage = this.getMessageExecutionTask(this.startTime,this.endTime,"Leitura dos dados");
			if(results!=null) {
				writeLocal(results);
			}
			try {
				time = (new Date()).getTime() - timeDifMilliSeconds;
				if((time - currentDate.getTime()) <= 1000) {
					sleep(1001-(time - currentDate.getTime()));
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
