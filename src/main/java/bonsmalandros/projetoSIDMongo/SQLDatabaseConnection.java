package bonsmalandros.projetoSIDMongo;

import java.sql.*;

public class SQLDatabaseConnection {

    private static Connection connectionLocalhost;
    private static Connection connectionCloud;
    private static Statement statementLocalhost;
    private static Statement statementCloud;
    private static final String dbName = "sid2021";
    private static final double outlierTemperatura = 1.0;


    private static boolean databaseExists() throws SQLException {
        ResultSet resultSet = connectionLocalhost.getMetaData().getCatalogs();
        while (resultSet.next()) {
            String databaseName = resultSet.getString(1);
            if (databaseName.equals(dbName.toLowerCase())) {
                return true;
            }
        }
        resultSet.close();
        return false;
    }

    private static boolean hasTable(String nomeTabela) throws SQLException {
        ResultSet rs = statementLocalhost.executeQuery("Show tables");
        while (rs.next()) {
            String table = rs.getString(1);
            if (table.equals(nomeTabela)) {
                return true;
            }
        }
        rs.close();
        return false;
    }

    private static void createTabelaZona() throws SQLException {
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`zona` ( `idZona` INT NOT NULL PRIMARY KEY, `temperatura` DOUBLE , `humidade` DOUBLE , `luz` DOUBLE ) ENGINE = InnoDB;";
        //String addPrimaryKey = "ALTER TABLE `zona` ADD PRIMARY KEY(`idZona`);";
        statementLocalhost.executeUpdate(createTable);
        //statementLocalhost.executeUpdate(addPrimaryKey);
    }

    private static void createTabelaSensor() throws SQLException {
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`sensor` (`idSensor` INT NOT NULL PRIMARY KEY,`tipoSensor` CHAR(1) NOT NULL , `idZona` INT ) ENGINE = InnoDB;";
        String addForeignKey = "ALTER TABLE `sensor` ADD  CONSTRAINT `sensor-zona` FOREIGN KEY (`idZona`) REFERENCES `zona`(`idZona`) ON DELETE SET NULL ON UPDATE CASCADE;";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addForeignKey);
    }

    private static void createTabelaMedicao() throws SQLException {
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`medicao` ( `idMedicao` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  `idSensor` INT ,  `tempo` TIMESTAMP NOT NULL ,  `valorMedicao` DOUBLE NOT NULL , `validacao` CHAR(1) NOT NULL) ENGINE = InnoDB;";
        String addForeignKey = "ALTER TABLE `medicao` ADD  CONSTRAINT `medicao-sensor` FOREIGN KEY (`idSensor`) REFERENCES `sensor`(`idSensor`) ON DELETE CASCADE ON UPDATE CASCADE;";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addForeignKey);
    }

    private static void createTabelaUtilizador() throws SQLException {
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`utilizador` ( `idUtilizador` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  `nomeUtilizador` VARCHAR(100) NOT NULL ,  `email` VARCHAR(50) UNIQUE NOT NULL, `tipoUtilizador` CHAR(1) NOT NULL, `intervaloMinimoAvisos` TIME NOT NULL) ENGINE = InnoDB;";
        statementLocalhost.executeUpdate(createTable);
    }

    private static void createTabelaAlerta() throws SQLException {
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`alerta` ( `idAlerta` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  `idCultura` INT ,  `idMedicao` INT ,  `tipoAlerta` VARCHAR(50) NOT NULL ,  `mensagem` VARCHAR(200) NOT NULL ) ENGINE = InnoDB;";
        String addForeignKey = "ALTER TABLE `alerta` ADD  CONSTRAINT `alerta-cultura` FOREIGN KEY (`idCultura`) REFERENCES `cultura`(`idCultura`) ON DELETE CASCADE ON UPDATE CASCADE;";
        String addForeignKey2 = "ALTER TABLE `alerta` ADD  CONSTRAINT `alerta-medicao` FOREIGN KEY (`idMedicao`) REFERENCES `medicao`(`idMedicao`) ON DELETE SET NULL ON UPDATE CASCADE;";
        statementLocalhost.executeUpdate(createTable);
        statementLocalhost.executeUpdate(addForeignKey);
        statementLocalhost.executeUpdate(addForeignKey2);
    }

    private static void createTabelaCultura() throws SQLException {
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`cultura` ( `idCultura` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  `nomeCultura` VARCHAR(50) NOT NULL ,  `idUtilizador` INT ,  `idZona` INT NOT NULL ,  `lumLimSup` DOUBLE NULL ,  `lumLimInf` DOUBLE NULL ,  `tempLimSup` DOUBLE NULL ,  `tempLimInf` DOUBLE NULL ,  `humLimSup` DOUBLE NULL ,  `humLimInf` DOUBLE NULL,  `lumLimSupAlerta` DOUBLE NULL ,  `lumLimInfAlerta` DOUBLE NULL ,  `tempLimSupAlerta` DOUBLE NULL ,  `tempLimInfAlerta` DOUBLE NULL ,  `humLimSupAlerta` DOUBLE NULL ,  `humLimInfAlerta` DOUBLE NULL, `isValido` BOOLEAN NOT NULL ) ENGINE = InnoDB;";
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

            String connectionLocalhostURI = "jdbc:mysql://localhost/";

            connectionLocalhost = DriverManager.getConnection(connectionLocalhostURI, "root", null); //Conectar ao PC pessoal para escrever
            connectionCloud = DriverManager.getConnection(connectionCloudURI, "aluno", "aluno"); //Conectar à Nuvem para ler
            statementLocalhost = connectionLocalhost.createStatement();
            statementCloud = connectionCloud.createStatement();

            if (databaseExists()) {
                String createBD = "DROP DATABASE " + dbName.toLowerCase();
                statementLocalhost.executeUpdate(createBD);
            }
            //criar bd
            String createBD = "CREATE DATABASE " + dbName.toLowerCase();
            statementLocalhost.executeUpdate(createBD);

            connectionLocalhost = DriverManager.getConnection(connectionLocalhostURI + dbName.toLowerCase(), "root", null);
            statementLocalhost = connectionLocalhost.createStatement();

            //criar tabela zona no localhost
            if (!hasTable("zona")) {
                createTabelaZona();
            }
            //criar a tabela sensor no locahost
            if (!hasTable("sensor")) {
                createTabelaSensor();
            }
            //criar a tabela medicao
            if (!hasTable("medicao")) {
                createTabelaMedicao();
            }
            //criar a tabela utilizador
            if (!hasTable("utilizador")) {
                createTabelaUtilizador();
            }
            //criar a tabela cultura
            if (!hasTable("cultura")) {
                createTabelaCultura();
            }
            //criar a tabela alerta
            if (!hasTable("alerta")) {
                createTabelaAlerta();
            }

            //criar procedimento do alerta
            String dropProcedimentoAlerta = "DROP PROCEDURE IF EXISTS `criar_alerta`";
            String createAlertaProcedure = "CREATE PROCEDURE `criar_alerta`(IN `idCultura` INT, IN `idMedicao` INT, IN `tipoAlerta` VARCHAR(50), IN `mensagem` VARCHAR(200)) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN INSERT INTO `alerta` (`idCultura`, `idMedicao`, `tipoAlerta`, `mensagem`) VALUES (idCultura, idMedicao, tipoAlerta, mensagem); END;";
            statementLocalhost.executeUpdate(dropProcedimentoAlerta);
            statementLocalhost.executeUpdate(createAlertaProcedure);

            //criar procedimento da medicao
            String dropProcedimentoMedicao = "DROP PROCEDURE IF EXISTS `create_medicao`";
            String createMedicaoProcedure = "CREATE PROCEDURE `create_medicao`(IN `idSensor` INT, IN `tempo` TIMESTAMP, IN `valorMedicao` DOUBLE, IN `validacao` CHAR(1)) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN INSERT INTO `medicao` (`idSensor`, `tempo`, `valorMedicao`, `validacao`) VALUES (idSensor, tempo, valorMedicao, validacao); END";

            statementLocalhost.executeUpdate(dropProcedimentoMedicao);
            statementLocalhost.executeUpdate(createMedicaoProcedure);

            //criar procedimento de criar cultura
            String dropProcedimentoCriarCultura = "DROP PROCEDURE IF EXISTS `criar_cultura`";
            String createCriarCulturaProcedure = "CREATE PROCEDURE `criar_cultura`(IN `nomeCultura` VARCHAR(50), IN `idZona` INT) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN INSERT INTO `cultura` (`nomeCultura`, `idUtilizador`, `idZona`, `lumLimSup`, `lumLimInf`, `tempLimSup`, `tempLimInf`, `humLimSup`, `humLimInf`, `lumLimSupAlerta`, `lumLimInfAlerta`, `tempLimSupAlerta`, `tempLimInfAlerta`, `humLimSupAlerta`, `humLimInfAlerta`, `isValido`) VALUES (nomeCultura, NULL, idZona, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0); END";
            statementLocalhost.executeUpdate(dropProcedimentoCriarCultura);
            statementLocalhost.executeUpdate(createCriarCulturaProcedure);

            //criar procedimento de remover cultura
            String dropProcedimentoRemoverCultura = "DROP PROCEDURE IF EXISTS `remover_cultura`";
            String createRemoverCulturaProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `remover_cultura`(IN `idCultura` INT) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN DELETE FROM `cultura` WHERE `idCultura` = idCultura; END;";
            statementLocalhost.executeUpdate(dropProcedimentoRemoverCultura);
            statementLocalhost.executeUpdate(createRemoverCulturaProcedure);

            //criar procedimento de alterar cultura
            String dropProcedimentoAlterarCultura = "DROP PROCEDURE IF EXISTS `alterar_cultura`";
            String createAlterarCulturaProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `alterar_cultura`(IN `idCultura` INT, IN `idZona` INT, IN `nomeCultura` VARCHAR(50), IN `lumLimSup` DOUBLE, IN `lumLimInf` DOUBLE, IN `tempLimSup` DOUBLE, IN `tempLimInf` DOUBLE, IN `humLimSup` DOUBLE, IN `humLimInf` DOUBLE, IN `lumLimSupAlerta` DOUBLE, IN `lumLimInfAlerta` DOUBLE, IN `tempLimSupAlerta` DOUBLE, IN `tempLimInfAlerta` DOUBLE, IN `humLimSupAlerta` DOUBLE, IN `humLimInfAlerta` DOUBLE) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN \n" +
                    "\n" +
                    "SET @cultura_valida :=(SELECT count(*) FROM utilizador,cultura WHERE cultura.idUtilizador=utilizador.idUtilizador and utilizador.email=(select substring_index(user(),'@', 1)) and cultura.idCultura=idCultura); \n" +
                    "\n" +
                    "IF @cultura_valida <> 0 THEN\n" +
                    "\n" +
                    "IF lumLimSup > lumLimSupAlerta and lumLimSupAlerta > lumLimInfAlerta and lumLimInfAlerta > lumLimInf and tempLimSup >  tempLimSupAlerta and tempLimSupAlerta > tempLimInfAlerta and tempLimInfAlerta > tempLimInf and humLimSup >  humLimSupAlerta and humLimSupAlerta > humLimInfAlerta and humLimInfAlerta > humLimInf THEN\n" +
                    "\tSET @isValido := 1; \n" +
                    "ELSE \n" +
                    "    SET @isValido := 0;\n" +
                    "END IF;\n" +
                    "\n" +
                    "UPDATE `cultura` SET `idZona` = idZona, `nomeCultura` = nomeCultura, `lumLimSup` = lumLimSup, `lumLimInf` = lumLimInf, `tempLimSup` = tempLimSup, `tempLimInf` = tempLimInf, `humLimSup` = humLimSup, `humLimInf` = humLimInf, `lumLimSupAlerta` = lumLimSupAlerta, `lumLimInfAlerta` = lumLimInfAlerta, `tempLimSupAlerta` = tempLimSupAlerta, `tempLimInfAlerta` = tempLimInfAlerta, `humLimSupAlerta` = humLimSupAlerta, `humLimInfAlerta` = humLimInfAlerta, `isValido` = @isValido WHERE cultura.idCultura = idCultura;\n" +
                    "ELSE\n" +
                    "\tSIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'É uma cultura inválida para o utilizador atual!';\n" +
                    "END IF;\n" +
                    "END";
            statementLocalhost.executeUpdate(dropProcedimentoAlterarCultura);
            statementLocalhost.executeUpdate(createAlterarCulturaProcedure);

            //criar procedimento de atribuir um utilizador a uma cultura
            String dropProcedimentoAtribuirUtilizadorCultura = "DROP PROCEDURE IF EXISTS `atribuir_cultura_investigador`";
            String createAtribuirUtilizadorCulturaProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `atribuir_cultura_investigador`(IN `idCultura` INT, IN `idUtilizador` INT) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN IF (SELECT tipoUtilizador FROM utilizador WHERE utilizador.idUtilizador = idUtilizador) = 'i' THEN UPDATE `cultura` SET `idUtilizador` = idUtilizador WHERE `cultura`.`idCultura` = idCultura; ELSE SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insira um idUtilizador válido e que seja Investigador!'; END IF; END;";
            statementLocalhost.executeUpdate(dropProcedimentoAtribuirUtilizadorCultura);
            statementLocalhost.executeUpdate(createAtribuirUtilizadorCulturaProcedure);

            //criar procedimento de desatribuir um utilizador a uma cultura
            String dropProcedimentoDesatribuirUtilizadorCultura = "DROP PROCEDURE IF EXISTS `desatribuir_cultura_investigador`";
            String createDesatribuirUtilizadorCulturaProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `desatribuir_cultura_investigador`(IN `idCultura` INT) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN UPDATE `cultura` SET `idUtilizador` = NULL WHERE `cultura`.`idCultura` = idCultura; END;";
            statementLocalhost.executeUpdate(dropProcedimentoDesatribuirUtilizadorCultura);
            statementLocalhost.executeUpdate(createDesatribuirUtilizadorCulturaProcedure);

            //criar procedimento de remover utilizador
            String dropProcedimentoRemoverUtilizador = "DROP PROCEDURE IF EXISTS `remover_utilizador`";
            String createRemoverUtilizadorProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `remover_utilizador`(IN `email` VARCHAR(50)) NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN DELETE FROM `utilizador` WHERE `email` = email; SET @user := CONCAT('DROP USER ''', email, '''@''localhost'''); PREPARE stmt FROM @user; EXECUTE stmt; END;";
            statementLocalhost.executeUpdate(dropProcedimentoRemoverUtilizador);
            statementLocalhost.executeUpdate(createRemoverUtilizadorProcedure);

            //criar procedimento de alterar utilizador
            String dropProcedimentoAlterarUtilizador = "DROP PROCEDURE IF EXISTS `alterar_utilizador`";
            String createAlterarUtilizadorProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `alterar_utilizador`(IN `email` VARCHAR(50), IN `nomeUtilizador` VARCHAR(50), IN `intervaloMinimoAvisos` TIME) NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN UPDATE `utilizador` SET `nomeUtilizador`= nomeUtilizador, `intervaloMinimoAvisos` = intervaloMinimoAvisos WHERE `email` = email; END;";
            statementLocalhost.executeUpdate(dropProcedimentoAlterarUtilizador);
            statementLocalhost.executeUpdate(createAlterarUtilizadorProcedure);

            //criar procedimento que cria um utilizador
            String dropProcedimentoUtilizador = "DROP PROCEDURE IF EXISTS `criar_utilizador`";
            String createUtilizadorProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `criar_utilizador`(IN `username` VARCHAR(50), IN `email` VARCHAR(50), IN `pwd` VARCHAR(50), IN `tipoUtilizador` CHAR(1), IN `intervaloMinimoAvisos` TIME) NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN\n" +
                    "\n" +
                    "IF tipoUtilizador = 'A' or tipoUtilizador = 'I' THEN\n" +
                    "SET @user := CONCAT('CREATE USER ''', email, '''@''localhost''', ' IDENTIFIED BY ''', pwd, '''');\n" +
                    "PREPARE stmt FROM @user; \n" +
                    "EXECUTE stmt;\n" +
                    "CASE \n" +
                    "WHEN tipoUtilizador = 'I' THEN \n" +
                    "\tSET @perm := concat('GRANT investigador TO ''',email,'''@''localhost''');\n" +
                    "    SET @setrole := concat('SET DEFAULT ROLE investigador FOR ''',email,'''@''localhost''');\n" +
                    "WHEN tipoUtilizador = 'A' THEN \n" +
                    "\tSET @perm := concat('GRANT administrador TO ''',email,'''@''localhost''');\n" +
                    "    SET @setrole := concat('SET DEFAULT ROLE administrador FOR ''',email,'''@''localhost''');\n" +
                    "END CASE; \n" +
                    "\n" +
                    "PREPARE grnt FROM @perm; \n" +
                    "EXECUTE grnt;\n" +
                    "PREPARE setrole FROM @setrole; \n" +
                    "EXECUTE setrole;\n" +
                    "\n" +
                    "INSERT INTO `utilizador` (`nomeUtilizador`, `tipoUtilizador`,`email`, `intervaloMinimoAvisos`) VALUES (username, tipoUtilizador ,email, intervaloMinimoAvisos);  \n" +
                    "\n" +
                    "ELSE \n" +
                    "\tSIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Não existe este tipo de utilizador. Só (A)dministrador e (I)nvestigador!'; \n" +
                    "END IF;\n" +
                    "\n" +
                    "END";
            statementLocalhost.executeUpdate(dropProcedimentoUtilizador);
            statementLocalhost.executeUpdate(createUtilizadorProcedure);

            /*//criar trigger do limite de alerta
            String dropTriggerLimiteAlerta = "DROP TRIGGER IF EXISTS `alerta_cultura`";
            String createAlertaTrigger = "CREATE DEFINER=`root`@`localhost` TRIGGER `alerta_cultura` AFTER INSERT ON `medicao` FOR EACH ROW BEGIN \n" +
                    "DECLARE id int; \n" +
                    "DECLARE isRiscoModerado int; \n" +
                    "CREATE TEMPORARY TABLE items (idCultura int); \n" +
                    "SET @tipo :=(SELECT DISTINCT tipoSensor FROM medicao, sensor WHERE new.idSensor=sensor.idSensor); \n" +
                    "\n" +
                    "IF @tipo = 'T' THEN \n" +
                    "INSERT INTO items (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND (new.valorMedicao<=cultura.tempLimInfAlerta OR new.valorMedicao>=cultura.tempLimSupAlerta)); \n" +
                    "WHILE EXISTS(SELECT * FROM items) DO \n" +
                    "SET @id := (SELECT * FROM items LIMIT 1); \n" +
                    "DELETE FROM items WHERE (idCultura = @id); \n" +
                    "SELECT COUNT(*) into isRiscoModerado FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao<cultura.tempLimSup AND new.valorMedicao>cultura.tempLimSupAlerta) OR (new.valorMedicao>cultura.tempLimInf AND new.valorMedicao<cultura.tempLimInfAlerta)); \n" +
                    "IF isRiscoModerado>0 THEN\n" +
                    "CALL `Create_Alerta`(@id, new.idMedicao , 'Alerta Temperatura', 'Foi registada uma medição com um valor que ultrapassa os limites de alerta, mas ainda se encontra dentro da temperatura tolerável pela cultura.');\n" +
                    "ELSE \n" +
                    "CALL `Create_Alerta`(@id, new.idMedicao , 'Alerta Limite Temperatura Ultrapassado', 'Foi registada uma medição com um valor que ultrapassa os limites da temperatura tolerável pela cultura.');\n" +
                    "END IF;\n" +
                    "END WHILE; \n" +
                    "END IF; \n" +
                    "\n" +
                    "IF @tipo = 'H' THEN \n" +
                    "INSERT INTO items (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND (new.valorMedicao<=cultura.humLimInfAlerta OR new.valorMedicao>=cultura.humLimSupAlerta)); \n" +
                    "WHILE EXISTS(SELECT * FROM items) DO \n" +
                    "SET @id := (SELECT * FROM items LIMIT 1); \n" +
                    "DELETE FROM items WHERE (idCultura = @id);\n" +
                    "SELECT COUNT(*) into isRiscoModerado FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao<cultura.humLimSup AND new.valorMedicao>cultura.humLimSupAlerta) OR (new.valorMedicao>cultura.humLimInf AND new.valorMedicao<cultura.humLimInfAlerta)); \n" +
                    "IF isRiscoModerado>0 THEN\n" +
                    "CALL `Create_Alerta`(@id, new.idMedicao , 'Alerta Humidade', 'Foi registada uma medição com um valor que ultrapassa os limites de alerta, mas ainda se encontra dentro da humidade tolerável pela cultura.');\n" +
                    "ELSE\n" +
                    "CALL `Create_Alerta`(@id, new.idMedicao , 'Alerta Limite Humidade Ultrapassado', 'Foi registada uma medição com um valor que ultrapassa os limites de humidade tolerável pela cultura.');\n" +
                    "END IF;\n" +
                    "END WHILE; \n" +
                    "END IF; \n" +
                    "\n" +
                    "IF @tipo = 'L' THEN \n" +
                    "INSERT INTO items (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND (new.valorMedicao<=cultura.lumLimInfAlerta OR new.valorMedicao>=cultura.lumLimSupAlerta)); \n" +
                    "WHILE EXISTS(SELECT * FROM items) DO \n" +
                    "SET @id := (SELECT * FROM items LIMIT 1); \n" +
                    "DELETE FROM items WHERE (idCultura = @id); \n" +
                    "SELECT COUNT(*) into isRiscoModerado FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao<cultura.lumLimSup AND new.valorMedicao>cultura.lumLimSupAlerta) OR (new.valorMedicao>cultura.lumLimInf AND new.valorMedicao<cultura.lumLimInfAlerta)); \n" +
                    "IF isRiscoModerado>0 THEN\n" +
                    "CALL `Create_Alerta`(@id, new.idMedicao , 'Alerta Luminosidade', 'Foi registada uma medição com um valor que ultrapassa os limites de alerta, mas ainda se encontra dentro da luminosidade tolerável pela cultura.');\n" +
                    "ELSE\n" +
                    "CALL `Create_Alerta`(@id, new.idMedicao , 'Alerta Limite Luminosidade Ultrapassado', 'Foi registada uma medição com um valor que ultrapassa os limites da luminosidade tolerável pela cultura.');\n" +
                    "END IF;\n" +
                    "END WHILE; \n" +
                    "END IF; \n" +
                    "DROP TEMPORARY TABLE items; \n" +
                    "END";
            statementLocalhost.executeUpdate(dropTriggerLimiteAlerta);
            statementLocalhost.executeUpdate(createAlertaTrigger);*/

            //criar trigger do limite de alerta de temperatura
            String dropTriggerAlertaTemperatura = "DROP TRIGGER IF EXISTS `alerta_temperatura`";
            String createTriggerAlertaTemperatura = "CREATE DEFINER=`root`@`localhost` TRIGGER `alerta_temperatura` AFTER INSERT ON `medicao` FOR EACH ROW BEGIN \n" +
                    "DECLARE id int; \n" +
                    "DECLARE isRiscoModerado int; \n" +
                    "CREATE TEMPORARY TABLE items (idCultura int); \n" +
                    "SET @tipo :=(SELECT DISTINCT tipoSensor FROM medicao, sensor WHERE new.idSensor=sensor.idSensor); \n" +
                    "\n" +
                    "IF @tipo = 'T' THEN \n" +
                    "INSERT INTO items (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND (new.valorMedicao<=cultura.tempLimInfAlerta OR new.valorMedicao>=cultura.tempLimSupAlerta)); \n" +
                    "WHILE EXISTS(SELECT * FROM items) DO \n" +
                    "SET @id := (SELECT * FROM items LIMIT 1); \n" +
                    "DELETE FROM items WHERE (idCultura = @id); \n" +
                    "SELECT COUNT(*) into isRiscoModerado FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao<cultura.tempLimSup AND new.valorMedicao>cultura.tempLimSupAlerta) OR (new.valorMedicao>cultura.tempLimInf AND new.valorMedicao<cultura.tempLimInfAlerta)); \n" +
                    "IF isRiscoModerado>0 THEN\n" +
                    "CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Temperatura', 'Foi registada uma medição com um valor que ultrapassa os limites de alerta, mas ainda se encontra dentro da temperatura tolerável pela cultura.');\n" +
                    "ELSE \n" +
                    "CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Limite Temperatura Ultrapassado', 'Foi registada uma medição com um valor que ultrapassa os limites da temperatura tolerável pela cultura.');\n" +
                    "END IF;\n" +
                    "END WHILE; \n" +
                    "END IF; \n" +
                    "\n" +
                    "DROP TEMPORARY TABLE items; \n" +
                    "END";
            statementLocalhost.executeUpdate(dropTriggerAlertaTemperatura);
            statementLocalhost.executeUpdate(createTriggerAlertaTemperatura);

            //criar trigger do limite de alerta de humidade
            String dropTriggerAlertaHumidade = "DROP TRIGGER IF EXISTS `alerta_humidade`";
            String createTriggerAlertaHumidade = "CREATE DEFINER=`root`@`localhost` TRIGGER `alerta_humidade` AFTER INSERT ON `medicao` FOR EACH ROW BEGIN \n" +
                    "DECLARE id int; \n" +
                    "DECLARE isRiscoModerado int; \n" +
                    "CREATE TEMPORARY TABLE items (idCultura int); \n" +
                    "SET @tipo :=(SELECT DISTINCT tipoSensor FROM medicao, sensor WHERE new.idSensor=sensor.idSensor); \n" +
                    "\n" +
                    "IF @tipo = 'H' THEN \n" +
                    "INSERT INTO items (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND (new.valorMedicao<=cultura.humLimInfAlerta OR new.valorMedicao>=cultura.humLimSupAlerta)); \n" +
                    "WHILE EXISTS(SELECT * FROM items) DO \n" +
                    "SET @id := (SELECT * FROM items LIMIT 1); \n" +
                    "DELETE FROM items WHERE (idCultura = @id);\n" +
                    "SELECT COUNT(*) into isRiscoModerado FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao<cultura.humLimSup AND new.valorMedicao>cultura.humLimSupAlerta) OR (new.valorMedicao>cultura.humLimInf AND new.valorMedicao<cultura.humLimInfAlerta)); \n" +
                    "IF isRiscoModerado>0 THEN\n" +
                    "CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Humidade', 'Foi registada uma medição com um valor que ultrapassa os limites de alerta, mas ainda se encontra dentro da humidade tolerável pela cultura.');\n" +
                    "ELSE\n" +
                    "CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Limite Humidade Ultrapassado', 'Foi registada uma medição com um valor que ultrapassa os limites de humidade tolerável pela cultura.');\n" +
                    "END IF;\n" +
                    "END WHILE; \n" +
                    "END IF; \n" +
                    "\n" +
                    "DROP TEMPORARY TABLE items; \n" +
                    "END";
            statementLocalhost.executeUpdate(dropTriggerAlertaHumidade);
            statementLocalhost.executeUpdate(createTriggerAlertaHumidade);

            //criar trigger do limite de alerta de luminosidade
            String dropTriggerAlertaLuminosidade = "DROP TRIGGER IF EXISTS `alerta_luminosidade`";
            String createTriggerAlertaLuminosidade = "CREATE DEFINER=`root`@`localhost` TRIGGER `alerta_luminosidade` AFTER INSERT ON `medicao` FOR EACH ROW BEGIN \n" +
                    "DECLARE id int; \n" +
                    "DECLARE isRiscoModerado int; \n" +
                    "CREATE TEMPORARY TABLE items (idCultura int); \n" +
                    "SET @tipo :=(SELECT DISTINCT tipoSensor FROM medicao, sensor WHERE new.idSensor=sensor.idSensor); \n" +
                    "\n" +
                    "IF @tipo = 'L' THEN \n" +
                    "INSERT INTO items (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao AND (new.valorMedicao<=cultura.lumLimInfAlerta OR new.valorMedicao>=cultura.lumLimSupAlerta)); \n" +
                    "WHILE EXISTS(SELECT * FROM items) DO \n" +
                    "SET @id := (SELECT * FROM items LIMIT 1); \n" +
                    "DELETE FROM items WHERE (idCultura = @id); \n" +
                    "SELECT COUNT(*) into isRiscoModerado FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao<cultura.lumLimSup AND new.valorMedicao>cultura.lumLimSupAlerta) OR (new.valorMedicao>cultura.lumLimInf AND new.valorMedicao<cultura.lumLimInfAlerta)); \n" +
                    "IF isRiscoModerado>0 THEN\n" +
                    "CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Luminosidade', 'Foi registada uma medição com um valor que ultrapassa os limites de alerta, mas ainda se encontra dentro da luminosidade tolerável pela cultura.');\n" +
                    "ELSE\n" +
                    "CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Limite Luminosidade Ultrapassado', 'Foi registada uma medição com um valor que ultrapassa os limites da luminosidade tolerável pela cultura.');\n" +
                    "END IF;\n" +
                    "END WHILE; \n" +
                    "END IF; \n" +
                    "DROP TEMPORARY TABLE items; \n" +
                    "END";
            statementLocalhost.executeUpdate(dropTriggerAlertaLuminosidade);
            statementLocalhost.executeUpdate(createTriggerAlertaLuminosidade);

            //criar trigger do valor fora do limite do sensor
/*            String dropTriggerValorInvalido = "DROP TRIGGER IF EXISTS `valor_invalido`";
            String createForaDoLimiteTrigger = "CREATE DEFINER=`root`@`localhost` TRIGGER `valor_invalido` BEFORE INSERT ON `medicao` FOR EACH ROW BEGIN DECLARE nInvalidos integer; SELECT COUNT(*) into nInvalidos FROM sensor, medicao WHERE sensor.idSensor=new.idSensor AND (sensor.limiteSup<new.valorMedicao OR sensor.limiteInf>new.valorMedicao); IF nInvalidos>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Valor inválido recusado!'; END IF; END;";
            statementLocalhost.executeUpdate(dropTriggerValorInvalido);
            statementLocalhost.executeUpdate(createForaDoLimiteTrigger);
*/


            //criar trigger para outliers
            String dropTriggerValidacaoTemp = "DROP TRIGGER IF EXISTS `validacao_temperatura`";
            String createValidacaoTempTrigger = "CREATE DEFINER=`root`@`localhost` TRIGGER `validacao_temperatura` BEFORE INSERT ON `medicao` FOR EACH ROW BEGIN\n" +
                    "\n" +
                    "CREATE TEMPORARY TABLE vetor (valorMedicao double); \n" +
                    "\n" +
                    "SET @tipo :=(SELECT DISTINCT tipoSensor FROM medicao, sensor WHERE new.idSensor=sensor.idSensor);\n" +
                    "\n" +
                    "SET @ultimaMedicao := (SELECT medicao.tempo FROM medicao WHERE new.idSensor=medicao.idSensor ORDER BY medicao.idMedicao DESC LIMIT 1);\n" +
                    "\n" +
                    "IF new.validacao = 'v' THEN\n" +

                    "\n" +
                    "IF @tipo = 'T' AND (SELECT TIMESTAMPDIFF(MINUTE, @ultimaMedicao,new.tempo))<10 THEN\n" +

                    "INSERT INTO vetor (SELECT valorMedicao FROM medicao WHERE new.idSensor=medicao.idSensor ORDER BY medicao.idMedicao DESC LIMIT 5);\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "SET @menorValor := (SELECT valorMedicao FROM vetor ORDER BY valorMedicao ASC LIMIT 0,1);\n" +
                    "\n" +
                    "SET @mediana := (SELECT valorMedicao FROM vetor ORDER BY valorMedicao ASC LIMIT 2,1);\n" +
                    "\n" +
                    "SET @maiorValor := (SELECT valorMedicao FROM vetor ORDER BY valorMedicao ASC LIMIT 4,1);\n" +
                    "\n" +
                    "IF (SELECT COUNT(*) FROM vetor)>4 THEN\n" +

                    "\t\tSET @limiteInf := (@mediana - @menorValor +" + outlierTemperatura + ");\n" +
                    "        SET @limiteSup := (@maiorValor - @mediana +" + outlierTemperatura + ");\n" +
                    "        IF (new.valorMedicao<(@mediana-@limiteInf) OR new.valorMedicao>(@mediana+@limiteSup)) THEN \n" +
                    "SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = 'Call Procedure';" +
                    "\t\t\tCALL `create_medicao`(new.idSensor , new.tempo, new.valorMedicao, 'i'); \n" +
                    "\t\tEND IF;\n" +
                    "END IF;\n" +
                    "END IF;\n" +
                    "END IF;\n" +
                    "DROP TEMPORARY TABLE vetor;\n" +
                    "END";
            statementLocalhost.executeUpdate(dropTriggerValidacaoTemp);
            statementLocalhost.executeUpdate(createValidacaoTempTrigger);


            //Ler a tabela 'zona'
            String selectSqlCloud = "SELECT * FROM `zona`";
            ResultSet resultSetCLoud = statementCloud.executeQuery(selectSqlCloud);

            while (resultSetCLoud.next()) {
                //Inserir na tabela 'zona' os valores
                String selectSqlLocalhost = "INSERT INTO `zona`(`idzona`, `temperatura`, `humidade`, `luz`) VALUES (" +
                        Double.parseDouble(resultSetCLoud.getString(1)) + "," +
                        Double.parseDouble(resultSetCLoud.getString(2)) + "," +
                        Double.parseDouble(resultSetCLoud.getString(3)) + "," +
                        Double.parseDouble(resultSetCLoud.getString(4)) + ")" +
                        "ON DUPLICATE KEY UPDATE `temperatura`=VALUES(`temperatura`), `humidade`=VALUES(`humidade`), " +
                        "`luz`=VALUES(`luz`)";
                statementLocalhost.executeUpdate(selectSqlLocalhost);
            }


            //ler a tabela 'sensor'
            selectSqlCloud = "SELECT * FROM `sensor`";
            resultSetCLoud = statementCloud.executeQuery(selectSqlCloud);
            int idSensor = 1;
            while (resultSetCLoud.next()) {
                //Inserir os valores na tabela 'sensor'
                String selectSqlLocalhost = "INSERT INTO `sensor` (`idSensor`,`tipoSensor`, `idZona`) VALUES ('" +
                        idSensor + "', '" +
                        resultSetCLoud.getString(2) + "', '" +
                        Integer.parseInt(resultSetCLoud.getString(5)) + "') " +

                        "ON DUPLICATE KEY UPDATE `tipoSensor`=VALUES(`tipoSensor`), `idZona`=VALUES(`idZona`)";
                idSensor++;
                statementLocalhost.executeUpdate(selectSqlLocalhost);
            }

            String insertCultura = "INSERT INTO `cultura` (`idCultura`, `nomeCultura`, `idUtilizador`, `idZona`, `lumLimSup`, `lumLimInf`, `tempLimSup`, `tempLimInf`, `humLimSup`, `humLimInf`, `lumLimSupAlerta`, `lumLimInfAlerta`, `tempLimSupAlerta`, `tempLimInfAlerta`, `humLimSupAlerta`, `humLimInfAlerta`, `isValido`) VALUES (NULL, 'pêssegos', NULL, '1', '25', '5', '25', '5', '25', '5', '20', '10', '20', '10', '20', '10', '1');";
            statementLocalhost.executeUpdate(insertCultura);
            String insertMedicao = "INSERT INTO `medicao` (`idSensor`, `tempo`, `valorMedicao`, `validacao`) VALUES ('1', current_timestamp(), '2', 'v');";
            statementLocalhost.executeUpdate(insertMedicao);
            String insertAlerta = "INSERT INTO `alerta` (`idCultura`, `idMedicao`, `tipoAlerta`, `mensagem`) VALUES ('1', '1', 'PERIGO', 'asd');";
            statementLocalhost.executeUpdate(insertAlerta);


            for(int i =0; i<5; i++) {

                String procedMedicaoInsert = "CALL `create_medicao`('3', '2021-04-28 11:50:0"+i+"', '6', 'v');";
                statementLocalhost.executeUpdate(procedMedicaoInsert);

            }


            String procedMedicaoInsert = "CALL `create_medicao`('3', '2021-04-28 11:50:0"+6+"', '20', 'v');";
            //statementLocalhost.executeUpdate(procedMedicaoInsert);

            //Criar ROLE investigador
            String dropRoleInvestigador = "DROP ROLE IF EXISTS `investigador`;";
            String createInvestigador = "CREATE ROLE investigador;";
            String privilegiosProcedureInvestigador = "GRANT EXECUTE ON PROCEDURE sid2021.alterar_cultura TO 'investigador'";
            String privilegiosSelectCulturaInvestigador = "GRANT SELECT ON `sid2021`.`cultura` TO 'investigador';";
            String privilegiosSelectUtilizadorInvestigador = "GRANT SELECT ON `sid2021`.`utilizador` TO 'investigador';";
            statementLocalhost.executeUpdate(dropRoleInvestigador);
            statementLocalhost.executeUpdate(createInvestigador);
            statementLocalhost.executeUpdate(privilegiosProcedureInvestigador);
            statementLocalhost.executeUpdate(privilegiosSelectCulturaInvestigador);
            statementLocalhost.executeUpdate(privilegiosSelectUtilizadorInvestigador);

            //Criar ROLE administrador
            String dropRoleAdmin = "DROP ROLE IF EXISTS `administrador`;";
            String createAdmin = "CREATE ROLE administrador;";
            String privilegiosAdminUtilizador = "GRANT DELETE, INSERT (`nomeUtilizador`,`email`) ON  `sid2021`.`utilizador` TO 'administrador'";
            String privilegiosAdminCulturas = "GRANT DELETE, UPDATE (`idUtilizador`), INSERT (`nomeCultura`,`idUtilizador`) ON `sid2021`.`cultura` TO 'administrador'";
            statementLocalhost.executeUpdate(dropRoleAdmin);
            statementLocalhost.executeUpdate(createAdmin);
            statementLocalhost.executeUpdate(privilegiosAdminUtilizador);
            statementLocalhost.executeUpdate(privilegiosAdminCulturas);

            //Criar ROLE java
            String dropRoleJava = "DROP ROLE IF EXISTS `java`;";
            String createJava = "CREATE ROLE java;";
            String privilegiosCriarMedicaoProcedureJava = "GRANT EXECUTE ON PROCEDURE sid2021.criar_medicao TO 'java'";
            String privilegiosCriarAlertaProcedureJava = "GRANT EXECUTE ON PROCEDURE sid2021.criar_alerta TO 'java'";
            String dropUserJava = "DROP USER IF EXISTS java@localhost;";
            String createUserJava = "CREATE USER java@localhost IDENTIFIED BY 'java'";
            String grantDefaultRoleJava = "GRANT java TO java@localhost;";
            String setDefaultRoleJava = "SET DEFAULT ROLE java FOR java@localhost";
            statementLocalhost.executeUpdate(dropRoleJava);
            statementLocalhost.executeUpdate(createJava);
            statementLocalhost.executeUpdate(privilegiosCriarMedicaoProcedureJava);
            statementLocalhost.executeUpdate(privilegiosCriarAlertaProcedureJava);
            statementLocalhost.executeUpdate(dropUserJava);
            statementLocalhost.executeUpdate(createUserJava);
            statementLocalhost.executeUpdate(grantDefaultRoleJava);
            statementLocalhost.executeUpdate(setDefaultRoleJava);

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




TRIGGEEEEEER limites


DELIMITER // DROP TRIGGER IF EXISTS `valor_invalido`// CREATE DEFINER=`root`@`localhost` TRIGGER `valor_invalido` BEFORE INSERT ON `medicao` FOR EACH ROW BEGIN DECLARE nInvalidos integer; SELECT COUNT(*) into nInvalidos FROM sensor, medicao WHERE sensor.idSensor=new.idSensor AND (sensor.limiteSup<new.valorMedicao OR sensor.limiteInf>new.valorMedicao); IF nInvalidos>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Valor inválido recusado!'; END IF; END// DELIMITER ;






select substring_index(current_user(),'@', 1)
SELECT nomeCultura FROM utilizador,cultura WHERE cultura.idUtilizador=utilizador.idUtilizador and utilizador.email=(select substring_index(current_user(),'@', 1))


CREATE DEFINER=`root`@`localhost` PROCEDURE `alterar_cultura`(IN `idCultura` INT, IN `idZona` INT, IN `nomeCultura` VARCHAR(50), IN `lumLimSup` DOUBLE, IN `lumLimInf` DOUBLE, IN `tempLimSup` DOUBLE, IN `tempLimInf` DOUBLE, IN `humLimSup` DOUBLE, IN `humLimInf` DOUBLE, IN `lumLimSupAlerta` DOUBLE, IN `lumLimInfAlerta` DOUBLE, IN `tempLimSupAlerta` DOUBLE, IN `tempLimInfAlerta` DOUBLE, IN `humLimSupAlerta` DOUBLE, IN `humLimInfAlerta` DOUBLE) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN

IF lumLimSup > lumLimSupAlerta and lumLimSupAlerta > lumLimInfAlerta and lumLimInfAlerta > lumLimInf and tempLimSup >  tempLimSupAlerta and tempLimSupAlerta > tempLimInfAlerta and tempLimInfAlerta > tempLimInf and humLimSup >  humLimSupAlerta and humLimSupAlerta > humLimInfAlerta and humLimInfAlerta > humLimInf THEN
	SET @isValido := 1;
ELSE
    SET @isValido := 0;
END IF;

SET @cultura_valida :=(SELECT count(*) FROM utilizador,cultura WHERE cultura.idUtilizador=utilizador.idUtilizador and utilizador.email=(select substring_index(current_user(),'@', 1)) and cultura.idCultura=idCultura);

IF @cultura_valida <> 0 THEN
	DELETE FROM `cultura` WHERE `idCultura` = idCultura;
ELSE
	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'É uma cultura inválida para o utilizador atual!';
END IF;
END

GRANT EXECUTE ON PROCEDURE sid2021.alterar_cultura TO 'investigador'
CALL `alterar_cultura`(1,2,'morangos',20,10,20,10,20,30,15,12,15,12,15,12)
SELECT count(*) FROM utilizador,cultura WHERE cultura.idUtilizador=utilizador.idUtilizador and utilizador.email=(select substring_index(current_user(),'@', 1)) and cultura.idCultura=idCultura
*/