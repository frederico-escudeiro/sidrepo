package bonsmalandros.projetoSIDMongo;

import java.sql.*;

public class SQLDatabaseConnection {

    private static Connection connectionLocalhost;
    private static Connection connectionCloud;
    private static Statement statementLocalhost;
    private static Statement statementCloud;
    private static final String dbName = "sid2021";

    private static boolean databaseExists() throws SQLException {
        ResultSet resultSet = connectionLocalhost.getMetaData().getCatalogs();
        while (resultSet.next()) {
            String databaseName = resultSet.getString(1);
            if(databaseName.equals(dbName)){
                return true;
            }
        }
        resultSet.close();
        return false;
    }

    private static boolean hasTable(String nomeTabela) throws SQLException {
        ResultSet rs = statementLocalhost.executeQuery("Show tables");
        while(rs.next()) {
            String table = rs.getString(1);
            if(table.equals(nomeTabela)){
                return true;
            }
        }
        rs.close();
        return false;
    }

    private static void createTabelaZona() throws SQLException {
        String createTable = "CREATE TABLE `sid2021`.`zona` ( `idZona` INT NOT NULL , `temperatura` DOUBLE , `humidade` DOUBLE , `luz` DOUBLE ) ENGINE = InnoDB;";
        String addPrimaryKey = "ALTER TABLE `zona` ADD PRIMARY KEY(`idZona`);";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addPrimaryKey);
    }

    private static void createTabelaSensor() throws SQLException {
        String createTable = "CREATE TABLE `sid2021`.`sensor` (`idSensor` INT NOT NULL,`tipoSensor` CHAR(1) NOT NULL , `idZona` INT , `limiteSup` DOUBLE NOT NULL , `limiteInf` DOUBLE NOT NULL ) ENGINE = InnoDB;";
        String addPrimaryKey = "ALTER TABLE `sensor` ADD PRIMARY KEY( `idSensor`);";
        String addForeignKey = "ALTER TABLE `sensor` ADD  CONSTRAINT `sensor-zona` FOREIGN KEY (`idZona`) REFERENCES `zona`(`idZona`) ON DELETE SET NULL ON UPDATE CASCADE;";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addPrimaryKey);
        statementLocalhost.executeUpdate(addForeignKey);
    }

    private static void createTabelaMedicao() throws SQLException {
        String createTable = "CREATE TABLE `sid2021`.`medicao` ( `idMedicao` INT NOT NULL ,  `idSensor` INT ,  `tempo` TIMESTAMP NOT NULL ,  `valorMedicao` DOUBLE NOT NULL ) ENGINE = InnoDB;";
        String addPrimaryKey = "ALTER TABLE `medicao` ADD PRIMARY KEY( `idMedicao`);";
        String addForeignKey = "ALTER TABLE `medicao` ADD  CONSTRAINT `medicao-sensor` FOREIGN KEY (`idSensor`) REFERENCES `sensor`(`idSensor`) ON DELETE NO ACTION ON UPDATE CASCADE;";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addPrimaryKey);
        statementLocalhost.executeUpdate(addForeignKey);
    }

    private static void createTabelaUtilizador() throws SQLException {
        String createTable = "CREATE TABLE `sid2021`.`utilizador` ( `idUtilizador` INT NOT NULL ,  `nomeUtilizador` VARCHAR(100) NOT NULL ,  `adminApp` BOOLEAN NOT NULL ,  `email` VARCHAR(50) ) ENGINE = InnoDB;";
        String addPrimaryKey = "ALTER TABLE `utilizador` ADD PRIMARY KEY( `idUtilizador`);";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addPrimaryKey);
    }

    private static void createTabelaAlerta() throws SQLException {
        String createTable = "CREATE TABLE `sid2021`.`alerta` ( `idAlerta` INT NOT NULL ,  `idCultura` INT ,  `idMedicao` INT ,  `tipoAlerta` VARCHAR(50) NOT NULL ,  `mensagem` VARCHAR(200) NOT NULL ,  `lumLimSupAlerta` DOUBLE NOT NULL ,  `lumLimInfAlerta` DOUBLE NOT NULL ,  `tempLimSupAlerta` DOUBLE NOT NULL ,  `tempLimInfAlerta` DOUBLE NOT NULL ,  `humLimSupAlerta` DOUBLE NOT NULL ,  `humLimInfAlerta` DOUBLE NOT NULL ,  `intervaloMinimoAvisos` TIME NOT NULL ) ENGINE = InnoDB;";
        String addPrimaryKey = "ALTER TABLE `alerta` ADD PRIMARY KEY( `idAlerta`);";
        String addForeignKey = "ALTER TABLE `alerta` ADD  CONSTRAINT `alerta-cultura` FOREIGN KEY (`idCultura`) REFERENCES `cultura`(`idCultura`) ON DELETE NO ACTION ON UPDATE CASCADE;";
        String addForeignKey2 = "ALTER TABLE `alerta` ADD  CONSTRAINT `alerta-medicao` FOREIGN KEY (`idMedicao`) REFERENCES `medicao`(`idMedicao`) ON DELETE NO ACTION ON UPDATE CASCADE;";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addPrimaryKey);
        statementLocalhost.executeUpdate(addForeignKey);
        statementLocalhost.executeUpdate(addForeignKey2);
    }

    private static void createTabelaCultura() throws SQLException {
        String createTable = "CREATE TABLE `sid2021`.`cultura` ( `idCultura` INT NOT NULL ,  `nomeCultura` VARCHAR(50) NOT NULL ,  `idUtilizador` INT ,  `idZona` INT NOT NULL ,  `lumLimSup` DOUBLE NOT NULL ,  `lumLimInf` DOUBLE NOT NULL ,  `tempLimSup` DOUBLE NOT NULL ,  `tempLimInf` DOUBLE NOT NULL ,  `humLimSup` DOUBLE NOT NULL ,  `humLimInf` DOUBLE NOT NULL ) ENGINE = InnoDB;";
        String addPrimaryKey = "ALTER TABLE `cultura` ADD PRIMARY KEY( `idCultura`);";
        String addForeignKey = "ALTER TABLE `cultura` ADD  CONSTRAINT `cultura-utilizador` FOREIGN KEY (`idUtilizador`) REFERENCES `utilizador`(`idUtilizador`) ON DELETE SET NULL ON UPDATE CASCADE;";
        String addForeignKey2 = "ALTER TABLE `cultura` ADD  CONSTRAINT `cultura-zona` FOREIGN KEY (`idZona`) REFERENCES `zona`(`idZona`) ON DELETE CASCADE ON UPDATE CASCADE;";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addPrimaryKey);
        statementLocalhost.executeUpdate(addForeignKey);
        statementLocalhost.executeUpdate(addForeignKey2);
    }


    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String connectionCloudURI = "jdbc:mysql://194.210.86.10/sid2021";


            //Têm de ter uma base de dados chamada "sid2021" no vosso localhost através do xampp com
            // os campos definidos no Excel que propõe a estrutura do excel

            String connectionLocalhostURI = "jdbc:mysql://localhost/";

            connectionLocalhost = DriverManager.getConnection(connectionLocalhostURI, "root", null); //Conectar ao PC pessoal para escrever
            connectionCloud = DriverManager.getConnection(connectionCloudURI, "aluno", "aluno"); //Conectar à Nuvem para ler
            statementLocalhost = connectionLocalhost.createStatement();
            statementCloud = connectionCloud.createStatement();

            if(!databaseExists()){
                //criar bd
                String createBD = "CREATE DATABASE " + dbName;
                statementLocalhost.executeUpdate(createBD);
            }

            connectionLocalhost = DriverManager.getConnection(connectionLocalhostURI + dbName, "root", null);
            statementLocalhost = connectionLocalhost.createStatement();

            String createTable;
            String addPrimaryKey;
            //criar tabela zona no localhost
            if(!hasTable("zona")){
                createTabelaZona();
            }
            //criar a tabela sensor no locahost
            if(!hasTable("sensor")){
                createTabelaSensor();
            }
            //criar a tabela medicao
            if(!hasTable("medicao")){
                createTabelaMedicao();
            }
            //criar a tabela utilizador
            if(!hasTable("utilizador")){

                createTabelaUtilizador();
            }
            //criar a tabela cultura
            if(!hasTable( "cultura")){
                createTabelaCultura();
            }
            //criar a tabela alerta
            if(!hasTable( "alerta")){
                createTabelaAlerta();
            }

            //Ler a tabela 'zona'
            String selectSqlCloud = "SELECT * FROM `zona`";
            ResultSet resultSetCLoud = statementCloud.executeQuery(selectSqlCloud);

            while (resultSetCLoud.next()) {
                //Inserir na tabela 'zona' os valores
                String selectSqlLocalhost = "INSERT INTO `zona`(`idzona`, `temperatura`, `humidade`, `luz`) VALUES (" +
                        Double.parseDouble(resultSetCLoud.getString(1)) + "," +
                        Double.parseDouble(resultSetCLoud.getString(2)) + "," +
                        Double.parseDouble(resultSetCLoud.getString(3)) + "," +
                        Double.parseDouble(resultSetCLoud.getString(4)) + ")"+
                        "ON DUPLICATE KEY UPDATE `temperatura`=VALUES(`temperatura`), `humidade`=VALUES(`humidade`), " +
                        "`luz`=VALUES(`luz`)";
                statementLocalhost.executeUpdate(selectSqlLocalhost);
            }


            //ler a tabela 'sensor'
            selectSqlCloud = "SELECT * FROM `sensor`";
            resultSetCLoud = statementCloud.executeQuery(selectSqlCloud);
            int idSensor = 0;
            while (resultSetCLoud.next()) {
                //Inserir os valores na tabela 'sensor'
                String selectSqlLocalhost = "INSERT INTO `sensor` (`idSensor`, `tipoSensor`, `idZona`, `limiteSup`, `limiteInf`) VALUES ('" +
                        idSensor + "', '" +
                        resultSetCLoud.getString(2) + "', '" +
                        Integer.parseInt(resultSetCLoud.getString(5)) + "', '" +
                        Double.parseDouble(resultSetCLoud.getString(4)) + "', '" +
                        Double.parseDouble(resultSetCLoud.getString(3)) + "') " +
                        "ON DUPLICATE KEY UPDATE `tipoSensor`=VALUES(`tipoSensor`), `idZona`=VALUES(`idZona`), " +
                        "`limiteSup`=VALUES(`limiteSup`), `limiteInf`=VALUES(`limiteInf`)";
                statementLocalhost.executeUpdate(selectSqlLocalhost);
                idSensor++;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }
}

/*
INSERT INTO `cultura` (`idCultura`, `nomeCultura`, `idUtilizador`, `idZona`, `lumLimSup`, `lumLimInf`, `tempLimSup`, `tempLimInf`, `humLimSup`, `humLimInf`) VALUES ('0', 'banana', NULL, '1', '10', '0', '10', '0', '10', '0');

INSERT INTO `medicao` (`idMedicao`, `idSensor`, `tempo`, `valorMedicao`) VALUES ('0', '0', current_timestamp(), '2');

INSERT INTO `alerta` (`idAlerta`, `idCultura`, `idMedicao`, `tipoAlerta`, `mensagem`, `lumLimSupAlerta`, `lumLimInfAlerta`, `tempLimSupAlerta`, `tempLimInfAlerta`, `humLimSupAlerta`, `humLimInfAlerta`, `intervaloMinimoAvisos`) VALUES ('2', '0', '0', 'PERIGO', 'O André está com ganas de comer blica', '20', '10', '20', '10', '20', '10', '10');



 */