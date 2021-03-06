package bonsmalandros.projetoSIDMongo;

import java.sql.*;
import java.util.Locale;

public class SQLDatabaseConnection {

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String connectionCloudURI = "jdbc:mysql://194.210.86.10/sid2021";


            //Têm de ter uma base de dados chamada "sid2021" no vosso localhost através do xampp com
            // os campos definidos no Excel que propõe a estrutura do excel

            String connectionLocalhostURI = "jdbc:mysql://localhost/sid2021";

            try (Connection connectionLocalhost = DriverManager.getConnection(connectionLocalhostURI, "root", null); //Conectar à Nuvem para ler
                 Connection connectionCloud = DriverManager.getConnection(connectionCloudURI, "aluno", "aluno"); //Conectar ao PC pessoal para escrever
                 Statement statementLocalhost = connectionLocalhost.createStatement();
                 Statement statementCloud = connectionCloud.createStatement()) {

                //Ler a tabela 'zona'
                String selectSqlCloud = "SELECT * FROM `zona`";
                ResultSet resultSetCLoud = statementCloud.executeQuery(selectSqlCloud);

                //criar tabela zona no localhost
                String createTable = "CREATE TABLE `sid2021`.`zona` ( `idZona` INT NOT NULL , `temperatura` DOUBLE NOT NULL , `humidade` DOUBLE NOT NULL, `luz` DOUBLE NOT NULL ) ENGINE = InnoDB;";
                statementLocalhost.executeUpdate(createTable);

                while (resultSetCLoud.next()) {
                    System.out.println(resultSetCLoud.getString(1) + " " + resultSetCLoud.getString(2) +
                            " " + resultSetCLoud.getString(3) + " " + resultSetCLoud.getString(4));

                    //Inserir na tabela 'zona' os valores
                    String selectSqlLocalhost = "INSERT INTO `zona`(`idzona`, `temperatura`, `humidade`, `luz`) VALUES ("+
                            Double.parseDouble(resultSetCLoud.getString(1)) + "," +
                            Double.parseDouble(resultSetCLoud.getString(2)) + "," +
                            Double.parseDouble(resultSetCLoud.getString(3)) + "," +
                            Double.parseDouble(resultSetCLoud.getString(4))+ ")";
                    statementLocalhost.executeUpdate(selectSqlLocalhost);
                }

                //ler a tabela 'sensor'
                selectSqlCloud = "SELECT * FROM `sensor`";
                resultSetCLoud = statementCloud.executeQuery(selectSqlCloud);

                //criar a tabela sensor no locahost

                createTable = "CREATE TABLE `sid2021`.`sensor` ( `tipoSensor` CHAR(1) NOT NULL , `idZona` INT NOT NULL , `limiteSup` DOUBLE NOT NULL , `limiteInf` DOUBLE NOT NULL ) ENGINE = InnoDB;";
                statementLocalhost.executeUpdate(createTable);

                while(resultSetCLoud.next()){
                    System.out.println(resultSetCLoud.getString(1) + " " + resultSetCLoud.getString(2) +
                            " " + resultSetCLoud.getString(3) + " " + resultSetCLoud.getString(4) + " " + resultSetCLoud.getString((5)));

                    //Inserir os valores na tabela 'sensor'

                    String selectSqlLocalhost = "INSERT INTO `sensor` (`tipoSensor`, `idZona`, `limiteSup`, `limiteInf`) VALUES ('"+
                            resultSetCLoud.getString(2) +"', '" +
                            Integer.parseInt(resultSetCLoud.getString(5)) +"', '" +
                            Double.parseDouble(resultSetCLoud.getString(4)) +"', '" +
                            Double.parseDouble(resultSetCLoud.getString(3))+"');";
                    statementLocalhost.executeUpdate(selectSqlLocalhost);

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}