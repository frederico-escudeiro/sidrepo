package bonsmalandros.projetoSIDMongo;

import java.io.FileInputStream;
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

public class DirectMongoToSQL extends Thread {
	private String mongo_collection;
	private String mongo_database;
	private String mongo_uri;
	
	/* TODO FALTA ADICIONAR OS PARAMETROS SQL */
	public DirectMongoToSQL(String collection, String mongo_database, String mongo_uri) {
		mongo_collection = collection;
		this.mongo_database = mongo_database;
		this.mongo_uri = mongo_uri;
		
	}

	
	public void run() {
		
	}
}
