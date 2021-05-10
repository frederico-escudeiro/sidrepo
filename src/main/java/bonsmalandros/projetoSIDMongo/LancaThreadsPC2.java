package bonsmalandros.projetoSIDMongo;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

public class LancaThreadsPC2 {
	private static Connection connectionCloud;
	private static Statement statementCloud;

	LancaThreadsPC2() {
		try {
			Properties properties = new Properties();
			
			properties.load(new FileInputStream("CloudToSQL.ini"));
			connectionCloud = DriverManager.getConnection(properties.getProperty("SQL_Cloud"), properties.getProperty("user_SQL_Cloud"), properties.getProperty("pass_SQL_Cloud"));																					// ler
			statementCloud = connectionCloud.createStatement();
			String sqlGetSensor = "SELECT * FROM `sensor`";
			ResultSet result = statementCloud.executeQuery(sqlGetSensor);
			int sensorID = 1 ; 
			while (result.next()) {
				System.out.println("IDSensor: "+sensorID+" ,Zona : " + result.getString(1) + " ,Sensor :" + result.getString(2)+" ,Limite inferior: "+result.getDouble(3)+" ,Limite Superior: "+result.getDouble(4));
				CloudToSQL cloudToSQL = new CloudToSQL(sensorID,result.getString(1),result.getString(2),result.getDouble(3),result.getDouble(4));
				//cloudToSQL.start();
				sensorID = sensorID+1;
			}
		} catch (SQLException e) {
			System.out.println("Erro com a ligação ao Professor SQL");
		}catch (IOException e) {
			System.out.println("Erro com o ficheiro CloudToSQL.ini");
		}
	}

	public static void main(String[] args) {
		new LancaThreadsPC2();
	}
}
