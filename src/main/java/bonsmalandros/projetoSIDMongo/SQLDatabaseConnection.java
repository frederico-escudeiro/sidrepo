package bonsmalandros.projetoSIDMongo;

import java.sql.*;

public class SQLDatabaseConnection {

    private static Connection connectionLocalhost;
    private static Connection connectionCloud;
    private static Statement statementLocalhost;
    private static Statement statementCloud;
    private static final String dbName = "sidCiencia";

    private static boolean databaseExists() throws SQLException {
        ResultSet resultSet = connectionLocalhost.getMetaData().getCatalogs();
        while (resultSet.next()) {
            String databaseName = resultSet.getString(1);
            if(databaseName.equals(dbName.toLowerCase())){
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
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`zona` ( `idZona` INT NOT NULL , `temperatura` DOUBLE , `humidade` DOUBLE , `luz` DOUBLE ) ENGINE = InnoDB;";
        String addPrimaryKey = "ALTER TABLE `zona` ADD PRIMARY KEY(`idZona`);";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addPrimaryKey);
    }

    private static void createTabelaSensor() throws SQLException {
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`sensor` (`idSensor` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,`tipoSensor` CHAR(1) NOT NULL , `idZona` INT, `limiteSup` DOUBLE NOT NULL , `limiteInf` DOUBLE NOT NULL ) ENGINE = InnoDB;";
        String addForeignKey = "ALTER TABLE `sensor` ADD  CONSTRAINT `sensor-zona` FOREIGN KEY (`idZona`) REFERENCES `zona`(`idZona`) ON DELETE SET NULL ON UPDATE CASCADE;";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addForeignKey);
    }

    private static void createTabelaMedicao() throws SQLException {
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`medicao` ( `idMedicao` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  `idSensor` INT ,  `tempo` TIMESTAMP NOT NULL ,  `valorMedicao` DOUBLE NOT NULL ) ENGINE = InnoDB;";
        String addForeignKey = "ALTER TABLE `medicao` ADD  CONSTRAINT `medicao-sensor` FOREIGN KEY (`idSensor`) REFERENCES `sensor`(`idSensor`) ON DELETE CASCADE ON UPDATE CASCADE;";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addForeignKey);
    }

    private static void createTabelaUtilizador() throws SQLException {
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`utilizador` ( `idUtilizador` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  `nomeUtilizador` VARCHAR(100) NOT NULL ,  `adminApp` BOOLEAN NOT NULL ,  `email` VARCHAR(50) ) ENGINE = InnoDB;";
        statementLocalhost.executeUpdate(createTable);
    }

    private static void createTabelaAlerta() throws SQLException {
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`alerta` ( `idAlerta` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  `idCultura` INT ,  `idMedicao` INT ,  `tipoAlerta` VARCHAR(50) NOT NULL ,  `mensagem` VARCHAR(200) NOT NULL,  `intervaloMinimoAvisos` TIME NOT NULL ) ENGINE = InnoDB;";
        String addForeignKey = "ALTER TABLE `alerta` ADD  CONSTRAINT `alerta-cultura` FOREIGN KEY (`idCultura`) REFERENCES `cultura`(`idCultura`) ON DELETE CASCADE ON UPDATE CASCADE;";
        String addForeignKey2 = "ALTER TABLE `alerta` ADD  CONSTRAINT `alerta-medicao` FOREIGN KEY (`idMedicao`) REFERENCES `medicao`(`idMedicao`) ON DELETE SET NULL ON UPDATE CASCADE;";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addForeignKey);
        statementLocalhost.executeUpdate(addForeignKey2);
    }

    private static void createTabelaCultura() throws SQLException {
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`cultura` ( `idCultura` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  `nomeCultura` VARCHAR(50) NOT NULL ,  `idUtilizador` INT ,  `idZona` INT NOT NULL ,  `lumLimSup` DOUBLE NOT NULL ,  `lumLimInf` DOUBLE NOT NULL ,  `tempLimSup` DOUBLE NOT NULL ,  `tempLimInf` DOUBLE NOT NULL ,  `humLimSup` DOUBLE NOT NULL ,  `humLimInf` DOUBLE NOT NULL,  `lumLimSupAlerta` DOUBLE NOT NULL ,  `lumLimInfAlerta` DOUBLE NOT NULL ,  `tempLimSupAlerta` DOUBLE NOT NULL ,  `tempLimInfAlerta` DOUBLE NOT NULL ,  `humLimSupAlerta` DOUBLE NOT NULL ,  `humLimInfAlerta` DOUBLE NOT NULL ) ENGINE = InnoDB;";
        String addForeignKey = "ALTER TABLE `cultura` ADD  CONSTRAINT `cultura-utilizador` FOREIGN KEY (`idUtilizador`) REFERENCES `utilizador`(`idUtilizador`) ON DELETE SET NULL ON UPDATE CASCADE;";
        String addForeignKey2 = "ALTER TABLE `cultura` ADD  CONSTRAINT `cultura-zona` FOREIGN KEY (`idZona`) REFERENCES `zona`(`idZona`) ON DELETE CASCADE ON UPDATE CASCADE;";
        statementLocalhost.executeUpdate(createTable);
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
                String createBD = "CREATE DATABASE " + dbName.toLowerCase();
                statementLocalhost.executeUpdate(createBD);
            }

            connectionLocalhost = DriverManager.getConnection(connectionLocalhostURI + dbName.toLowerCase(), "root", null);
            statementLocalhost = connectionLocalhost.createStatement();

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
            while (resultSetCLoud.next()) {
                //Inserir os valores na tabela 'sensor'
                String selectSqlLocalhost = "INSERT INTO `sensor` (`tipoSensor`, `idZona`, `limiteSup`, `limiteInf`) VALUES ('" +
                        resultSetCLoud.getString(2) + "', '" +
                        Integer.parseInt(resultSetCLoud.getString(5)) + "', '" +
                        Double.parseDouble(resultSetCLoud.getString(4)) + "', '" +
                        Double.parseDouble(resultSetCLoud.getString(3)) + "') " +
                        "ON DUPLICATE KEY UPDATE `tipoSensor`=VALUES(`tipoSensor`), `idZona`=VALUES(`idZona`), " +
                        "`limiteSup`=VALUES(`limiteSup`), `limiteInf`=VALUES(`limiteInf`)";
                statementLocalhost.executeUpdate(selectSqlLocalhost);
            }

            String insertCultura = "INSERT INTO `cultura` (`idCultura`, `nomeCultura`, `idUtilizador`, `idZona`, `lumLimSup`, `lumLimInf`, `tempLimSup`, `tempLimInf`, `humLimSup`, `humLimInf`, `lumLimSupAlerta`, `lumLimInfAlerta`, `tempLimSupAlerta`, `tempLimInfAlerta`, `humLimSupAlerta`, `humLimInfAlerta`) VALUES (NULL, 'pêssegos', NULL, '2', '10', '0', '10', '0', '10', '0', '20', '10', '20', '10', '20', '10');";
            statementLocalhost.executeUpdate(insertCultura);
            String insertMedicao = "INSERT INTO `medicao` (`idSensor`, `tempo`, `valorMedicao`) VALUES ('1', current_timestamp(), '2');";
            statementLocalhost.executeUpdate(insertMedicao);
            //String insertAlerta = "INSERT INTO `alerta` (`idCultura`, `idMedicao`, `tipoAlerta`, `mensagem`, `intervaloMinimoAvisos`) VALUES ('1', '1', 'PERIGO', 'asd', '20');\n";
            //statementLocalhost.executeUpdate(insertAlerta);

            String createAlertaProcedure = "DROP PROCEDURE IF EXISTS `create_alerta`; CREATE PROCEDURE `create_alerta`(IN `idCultura` INT, IN `idMedicao` INT, IN `tipoAlerta` VARCHAR(50), IN `mensagem` VARCHAR(200)) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN INSERT INTO `alerta` (`idCultura`, `idMedicao`, `tipoAlerta`, `mensagem`) VALUES (idCultura, idMedicao, tipoAlerta, mensagem); END";
            statementLocalhost.executeUpdate(createAlertaProcedure);

            String createAlertaTrigger = "DROP TRIGGER IF EXISTS `RiscoProximoDoLimite`;CREATE DEFINER=`root`@`localhost` TRIGGER `RiscoProximoDoLimite` AFTER INSERT ON `medicao` FOR EACH ROW BEGIN DECLARE id int; CREATE TEMPORARY TABLE items ( idCultura int); SET @tipo :=(SELECT DISTINCT tipoSensor FROM medicao, sensor WHERE new.idSensor=sensor.idSensor); IF @tipo = 'T' THEN INSERT INTO items (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND (new.valorMedicao<=cultura.tempLimInfAlerta OR new.valorMedicao>=cultura.tempLimSupAlerta)); WHILE EXISTS(SELECT * FROM items) DO SET @id := (SELECT * FROM items LIMIT 1); DELETE FROM items WHERE (idCultura = @id); CALL `Create_Alerta`(@id, new.idMedicao , 'PERIGO', 'ASD'); END WHILE; END IF; IF @tipo = 'H' THEN INSERT INTO items (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND (new.valorMedicao<=cultura.humLimInfAlerta OR new.valorMedicao>=cultura.humLimSupAlerta)); WHILE EXISTS(SELECT * FROM items) DO SET @id := (SELECT * FROM items LIMIT 1); DELETE FROM items WHERE (idCultura = @id); CALL `Create_Alerta`(@id, new.idMedicao , 'PERIGO', 'ASD'); END WHILE; END IF; IF @tipo = 'L' THEN INSERT INTO items (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND (new.valorMedicao<=cultura.lumLimInfAlerta OR new.valorMedicao>=cultura.lumLimSupAlerta)); WHILE EXISTS(SELECT * FROM items) DO SET @id := (SELECT * FROM items LIMIT 1); DELETE FROM items WHERE (idCultura = @id); CALL `Create_Alerta`(@id, new.idMedicao , 'PERIGO', 'ASD'); END WHILE; END IF; END";
            statementLocalhost.executeUpdate(createAlertaTrigger);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}

/*
BEGIN



DROP PROCEDURE IF EXISTS `create_alerta`; CREATE PROCEDURE `create_alerta`(IN `idCultura` INT, IN `idMedicao` INT, IN `tipoAlerta` VARCHAR(50), IN `mensagem` VARCHAR(200)) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN INSERT INTO `alerta` (`idCultura`, `idMedicao`, `tipoAlerta`, `mensagem`) VALUES (idCultura, idMedicao, tipoAlerta, mensagem); END
DROP PROCEDURE IF EXISTS `create_medicao`; CREATE PROCEDURE `create_medicao`(IN `idSensor` INT, IN `tempo` TIMESTAMP, IN `valorMedicao` DOUBLE) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN INSERT INTO `medicao` (`idSensor`, `tempo`, `valorMedicao`) VALUES (idSensor, tempo, valorMedicao); END
CREATE PROCEDURE `create_cultura`(IN `nomeCultura` VARCHAR(50), IN `idUtilizador` INT, IN `idZona` INT, IN `lumLimSup` DOUBLE, IN `lumLimInf` DOUBLE, IN `tempLimSup` DOUBLE, IN `tempLimInf` DOUBLE, IN `humLimSup` DOUBLE, IN `humLimInf` DOUBLE, IN `lumLimSupAlerta` DOUBLE, IN `lumLimInfAlerta` DOUBLE, IN `tempLimSupAlerta` DOUBLE, IN `tempLimInfAlerta` DOUBLE, IN `humLimSupAlerta` DOUBLE, IN `humLimInfAlerta` DOUBLE) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN INSERT INTO `cultura` (`nomeCultura`, `idUtilizador`, `idZona`, `lumLimSup`, `lumLimInf`, `tempLimSup`, `tempLimInf`, `humLimSup`, `humLimInf`, `lumLimSupAlerta`, `lumLimInfAlerta`, `tempLimSupAlerta`, `tempLimInfAlerta`, `humLimSupAlerta`, `humLimInfAlerta`) VALUES (nomeCultura, idUtilizador, idZona, lumLimSup, lumLimInf, tempLimSup, tempLimInf, humLimSup, humLimInf, lumLimSupAlerta, lumLimInfAlerta, tempLimSupAlerta, tempLimInfAlerta, humLimSupAlerta, humLimInfAlerta); END


BEGIN

DECLARE decr int;

SET @test :=(SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND new.valorMedicao<=cultura.tempLimInfAlerta);

SET @decr:=(SELECT COUNT(*) FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND new.valorMedicao<=cultura.tempLimInfAlerta);


WHILE @decr !=0 DO
SET @decr := @decr-1;
SET @id:=(SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND new.valorMedicao<=cultura.tempLimInfAlerta LIMIT decr,1);
CALL `Create_Alerta`(@id, new.idMedicao , 'Cavalo', 'ASD');
END WHILE;
END

















*/