package bonsmalandros.projetoSIDMongo.direct;

import java.io.FileInputStream;
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

public class DirectToSQL extends Thread {
	private String mongo_collection;
	private String mongo_database;
	private String mongo_uri;

	// Tempo Datas
    // "%Y-%m-%d'T'%H:%M:%S'Z'"
    private DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private Date currentDate = new Date();
    private Date lateDate= new Date();
    private long timeDifMilliSeconds = 1000;			//delay na inserção e leitura de dados (1 segundo)

	
	/* TODO ADICIONAR OS PARAMETROS SQL */
	public DirectToSQL(String collection, String mongo_database, String mongo_uri) {
		mongo_collection = collection;
		this.mongo_database = mongo_database;
		this.mongo_uri = mongo_uri;
		
	}

	
	public void run() {
		 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
	        MongoClient client = new MongoClient(new MongoClientURI(mongo_uri));
	        MongoDatabase database = client.getDatabase(mongo_database);
	        System.out.println("Connection To Mongo Suceeded");
	        MongoCollection collection = database.getCollection(mongo_collection);
	        List<Document> listDocuments = new ArrayList<>();
	        long time = ((new Date()).getTime() - timeDifMilliSeconds)/1000;
	        time = time * 1000;
	        currentDate = new Date(time);

	        Bson filter = Filters.eq("Data" , df1.format(currentDate));
	        collection.find(filter).into(listDocuments);
	        if(!listDocuments.isEmpty()){
	            // TODO writeSensor(listDocuments);
	        }


	        while(true) {

	            lateDate = currentDate;
	            time = ((new Date()).getTime() - timeDifMilliSeconds)/1000;
	            time = time * 1000;
	            currentDate = new Date(time);
	            //late -> current
	            Bson filterLow = Filters.gt("Data" , df1.format(lateDate));
	            Bson filterUp = Filters.lte("Data",df1.format(currentDate));// para evitar o envio de duplicados
	            Bson filterLowAndUp = Filters.and(filterLow,filterUp);
	            collection.find(filterLowAndUp).into(listDocuments);
	            if(!listDocuments.isEmpty()){
	                //writeSensor(listDocuments);
	            }
	            System.out.println(/*cloud_topic  + */ ": Intervalo: "+ lateDate +" -> "+currentDate);
	            try {
	                time = ((new Date()).getTime() - timeDifMilliSeconds)/1000;
	                time = time * 1000;
	                if((time - currentDate.getTime()) <= 500) {
	                    sleep(1000);
	                }

	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }

	        }

	}
}
