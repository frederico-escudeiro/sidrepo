package bonsmalandros.projetoSIDMongo;

import java.sql.*;

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
                String addPrimaryKey = "ALTER TABLE `zona` ADD PRIMARY KEY(`idZona`);";
                statementLocalhost.executeUpdate(createTable);
                statementLocalhost.executeUpdate(addPrimaryKey);
                //criar a tabela sensor no locahost
                createTable = "CREATE TABLE `sid2021`.`sensor` (`idSensor` INT NOT NULL,`tipoSensor` CHAR(1) NOT NULL , `idZona` INT NOT NULL , `limiteSup` DOUBLE NOT NULL , `limiteInf` DOUBLE NOT NULL ) ENGINE = InnoDB;";
                addPrimaryKey = "ALTER TABLE `sensor` ADD PRIMARY KEY( `idSensor`);";
                statementLocalhost.executeUpdate(createTable);
                statementLocalhost.executeUpdate(addPrimaryKey);
                //criar a tabela medicao
                createTable = "CREATE TABLE `sid2021`.`medicao` ( `idMedicao` INT NOT NULL ,  `tipoSensor` CHAR(1) NOT NULL ,  `idZona` INT NOT NULL ,  `tempo` TIMESTAMP NOT NULL ,  `valorMedicao` DOUBLE NOT NULL ) ENGINE = InnoDB;";
                addPrimaryKey = "ALTER TABLE `medicao` ADD PRIMARY KEY( `idMedicao`);";
                statementLocalhost.executeUpdate(createTable);
                statementLocalhost.executeUpdate(addPrimaryKey);
                //criar a tabela utilizador
                createTable = "CREATE TABLE `sid2021`.`utilizador` ( `idUtilizador` INT NOT NULL ,  `nomeUtilizador` VARCHAR(100) NOT NULL ,  `adminApp` BOOLEAN NOT NULL ,  `email` VARCHAR(50) NOT NULL ) ENGINE = InnoDB;";
                addPrimaryKey = "ALTER TABLE `utilizador` ADD PRIMARY KEY( `idUtilizador`);";
                statementLocalhost.executeUpdate(createTable);
                statementLocalhost.executeUpdate(addPrimaryKey);
                //criar a tabela cultura
                createTable = "CREATE TABLE `sid2021`.`cultura` ( `idCultura` INT NOT NULL ,  `nomeCultura` VARCHAR(50) NOT NULL ,  `idUtilizador` INT NOT NULL ,  `idZona` INT NOT NULL ,  `lumLimSup` DOUBLE NOT NULL ,  `lumLimInf` DOUBLE NOT NULL ,  `tempLimSup` DOUBLE NOT NULL ,  `tempLimInf` DOUBLE NOT NULL ,  `humLimSup` DOUBLE NOT NULL ,  `humLimInf` DOUBLE NOT NULL ) ENGINE = InnoDB;";
                addPrimaryKey = "ALTER TABLE `cultura` ADD PRIMARY KEY( `idCultura`);";
                statementLocalhost.executeUpdate(createTable);
                statementLocalhost.executeUpdate(addPrimaryKey);
                //criar a tabela alerta
                createTable = "CREATE TABLE `sid2021`.`alerta` ( `idAlerta` INT NOT NULL ,  `idCultura` INT NOT NULL ,  `idMedicao` INT NOT NULL ,  `tipoAlerta` VARCHAR(50) NOT NULL ,  `mensagem` VARCHAR(200) NOT NULL ,  `lumLimSupAlerta` DOUBLE NOT NULL ,  `lumLimInfAlerta` DOUBLE NOT NULL ,  `tempLimSupAlerta` DOUBLE NOT NULL ,  `tempLimInfAlerta` DOUBLE NOT NULL ,  `humLimSupAlerta` DOUBLE NOT NULL ,  `humLimInfAlerta` DOUBLE NOT NULL ,  `intervaloMinimoAvisos` TIME NOT NULL ) ENGINE = InnoDB;";
                addPrimaryKey = "ALTER TABLE `alerta` ADD PRIMARY KEY( `idAlerta`);";
                statementLocalhost.executeUpdate(createTable);
                statementLocalhost.executeUpdate(addPrimaryKey);

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
                int idSensor = 0;
                while(resultSetCLoud.next()){
                    System.out.println(resultSetCLoud.getString(1) + " " + resultSetCLoud.getString(2) +
                            " " + resultSetCLoud.getString(3) + " " + resultSetCLoud.getString(4) + " " + resultSetCLoud.getString((5)));

                    //Inserir os valores na tabela 'sensor'

                    String selectSqlLocalhost = "INSERT INTO `sensor` (`idSensor`, `tipoSensor`, `idZona`, `limiteSup`, `limiteInf`) VALUES ('"+
                            idSensor +"', '" +
                            resultSetCLoud.getString(2) +"', '" +
                            Integer.parseInt(resultSetCLoud.getString(5)) +"', '" +
                            Double.parseDouble(resultSetCLoud.getString(4)) +"', '" +
                            Double.parseDouble(resultSetCLoud.getString(3))+"');";
                    statementLocalhost.executeUpdate(selectSqlLocalhost);
                    idSensor++;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}