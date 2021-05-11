package MongoToMongo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

/**
 * Hello world!
 *
 */
public class App 
{
		
    public static void main( String[] args )
    {
    	Properties var1 = new Properties();
        try {
			var1.load(new FileInputStream("MongoToMongo.ini"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        //Nuvem
        String uriNuvem = var1.getProperty("uriNuvem");
        String db_name_nuvem = var1.getProperty("db_name_nuvem");
        String col_name_nuvem_t1 = var1.getProperty("col_name_nuvem_t1");
        String col_name_nuvem_t2 = var1.getProperty("col_name_nuvem_t2");
        String col_name_nuvem_l1 = var1.getProperty("col_name_nuvem_l1");
        String col_name_nuvem_l2 = var1.getProperty("col_name_nuvem_l2");
        String col_name_nuvem_h1 = var1.getProperty("col_name_nuvem_h1");
        String col_name_nuvem_h2 = var1.getProperty("col_name_nuvem_h2");

        //Local
        String uri_local = var1.getProperty("uri_local");
        String db_name_local = var1.getProperty("db_name_local");
        String col_name_local_t1 = var1.getProperty("col_name_local_t1");
        String col_name_local_t2 = var1.getProperty("col_name_local_t2");
        String col_name_local_l1 = var1.getProperty("col_name_local_l1");
        String col_name_local_l2 = var1.getProperty("col_name_local_l2");
        String col_name_local_h1 = var1.getProperty("col_name_local_h1");
        String col_name_local_h2 = var1.getProperty("col_name_local_h2");

        //Temporais
        String data_format = var1.getProperty("data_format");
        long timeDifSeconds = Long.parseLong(var1.getProperty("timeDifSeconds"));
        long timeKeepDays = Long.parseLong(var1.getProperty("timeKeepDays"));
        long timeKeepHours = Long.parseLong(var1.getProperty("timeKeepHours"));
        long timeKeepMinutes = Long.parseLong(var1.getProperty("timeKeepMinutes"));
        long timeKeepSeconds = Long.parseLong(var1.getProperty("timeKeepSeconds"));
        long timeKeepMilliSeconds = Long.parseLong(var1.getProperty("timeKeepMilliSeconds"));
        
        long timeKeep = timeKeepDays*24*60*60*1000+timeKeepHours*60*60*1000+timeKeepMinutes*60*1000+timeKeepSeconds*1000+timeKeepMilliSeconds;
        long timeDifMilliSeconds = timeDifSeconds*1000;
        		
        //Ficheiros
        long thresholdOfDocsRemoved = Long.parseLong(var1.getProperty("thresholdOfDocsRemoved"));
        
//        LeituraMedicoes t1 = new LeituraMedicoes(uriNuvem,db_name_nuvem,col_name_nuvem_t1,uri_local,db_name_local,col_name_local_t1,timeDifMilliSeconds,timeKeep,thresholdOfDocsRemoved);
//        LeituraMedicoes t2 = new LeituraMedicoes(uriNuvem,db_name_nuvem,col_name_nuvem_t2,uri_local,db_name_local,col_name_local_t2,timeDifMilliSeconds,timeKeep,thresholdOfDocsRemoved);
//        LeituraMedicoes l1 = new LeituraMedicoes(uriNuvem,db_name_nuvem,col_name_nuvem_l1,uri_local,db_name_local,col_name_local_l1,timeDifMilliSeconds,timeKeep,thresholdOfDocsRemoved);
//        LeituraMedicoes l2 = new LeituraMedicoes(uriNuvem,db_name_nuvem,col_name_nuvem_l2,uri_local,db_name_local,col_name_local_l2,timeDifMilliSeconds,timeKeep,thresholdOfDocsRemoved);
//        LeituraMedicoes h1 = new LeituraMedicoes(uriNuvem,db_name_nuvem,col_name_nuvem_h1,uri_local,db_name_local,col_name_local_h1,timeDifMilliSeconds,timeKeep,thresholdOfDocsRemoved);
//        LeituraMedicoes h2 = new LeituraMedicoes(uriNuvem,db_name_nuvem,col_name_nuvem_h2,uri_local,db_name_local,col_name_local_h2,timeDifMilliSeconds,timeKeep,thresholdOfDocsRemoved);
//        
//      t1.start();
//    	t2.start();
//    	l1.start();
//    	l2.start();
//    	h1.start();
//    	h2.start();
        
        
        mqttToLocal testeLocal = new mqttToLocal(uri_local, db_name_local, col_name_local_t1);
        mqttToLocal testeLocal_1 = new mqttToLocal(uri_local, db_name_local, col_name_local_t2);
        mqttToLocal testeLocal_2 = new mqttToLocal(uri_local, db_name_local, col_name_local_h1);
        mqttToLocal testeLocal_3 = new mqttToLocal(uri_local, db_name_local, col_name_local_h2);
        mqttToLocal testeLocal_4 = new mqttToLocal(uri_local, db_name_local, col_name_local_l1);
        mqttToLocal testeLocal_5 = new mqttToLocal(uri_local, db_name_local, col_name_local_l2);
        
        testeLocal.start();
        testeLocal_1.start();
        testeLocal_2.start();
        testeLocal_3.start();
        testeLocal_4.start();
        testeLocal_5.start();
    }
}