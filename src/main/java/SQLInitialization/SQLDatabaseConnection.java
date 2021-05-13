package SQLInitialization;

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
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`sensor` (`idSensor` INT NOT NULL PRIMARY KEY,`tipoSensor` CHAR(1) NOT NULL , `idZona` INT) ENGINE = InnoDB;";
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
        String createTable = "CREATE TABLE " + dbName.toLowerCase() + ".`alerta` ( `idAlerta` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,  `idCultura` INT ,  `idMedicao` INT ,  `tipoAlerta` VARCHAR(100) NOT NULL ,  `mensagem` VARCHAR(200) NOT NULL ) ENGINE = InnoDB;";
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
            String createAlertaProcedure = "CREATE PROCEDURE `criar_alerta`(IN `idCultura` INT, IN `idMedicao` INT, IN `tipoAlerta` VARCHAR(100), IN `mensagem` VARCHAR(200)) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN INSERT INTO `alerta` (`idCultura`, `idMedicao`, `tipoAlerta`, `mensagem`) VALUES (idCultura, idMedicao, tipoAlerta, mensagem); END;";
            statementLocalhost.executeUpdate(dropProcedimentoAlerta);
            statementLocalhost.executeUpdate(createAlertaProcedure);

            //criar procedimento da medicao
            String dropProcedimentoMedicao = "DROP PROCEDURE IF EXISTS `criar_medicao`";
            String createMedicaoProcedure = "CREATE PROCEDURE `criar_medicao`(IN `idSensor` INT, IN `tempo` TIMESTAMP, IN `valorMedicao` DOUBLE, IN `validacao` CHAR(1)) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN INSERT INTO `medicao` (`idSensor`, `tempo`, `valorMedicao`, `validacao`) VALUES (idSensor, tempo, valorMedicao, validacao); END";
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
                    "SET @cultura_valida :=(SELECT count(*) FROM utilizador,cultura WHERE cultura.idUtilizador=utilizador.idUtilizador and utilizador.email=(select substring_index(user(),'@localhost', 1)) and cultura.idCultura=idCultura); \n" +
                    "\n" +
                    "IF @cultura_valida <> 0 THEN\n" +
                    "IF (SELECT COUNT(*) FROM zona WHERE zona.idZona=idZona) > 0 THEN\n" +
                    "IF lumLimSup > lumLimSupAlerta and lumLimSupAlerta > lumLimInfAlerta and lumLimInfAlerta > lumLimInf and tempLimSup >  tempLimSupAlerta and tempLimSupAlerta > tempLimInfAlerta and tempLimInfAlerta > tempLimInf and humLimSup >  humLimSupAlerta and humLimSupAlerta > humLimInfAlerta and humLimInfAlerta > humLimInf THEN\n" +
                    "\tSET @isValido := 1; \n" +
                    "ELSE \n" +
                    "    SET @isValido := 0;\n" +
                    "END IF;\n" +
                    "\n" +
                    "UPDATE `cultura` SET `idZona` = idZona, `nomeCultura` = nomeCultura, `lumLimSup` = lumLimSup, `lumLimInf` = lumLimInf, `tempLimSup` = tempLimSup, `tempLimInf` = tempLimInf, `humLimSup` = humLimSup, `humLimInf` = humLimInf, `lumLimSupAlerta` = lumLimSupAlerta, `lumLimInfAlerta` = lumLimInfAlerta, `tempLimSupAlerta` = tempLimSupAlerta, `tempLimInfAlerta` = tempLimInfAlerta, `humLimSupAlerta` = humLimSupAlerta, `humLimInfAlerta` = humLimInfAlerta, `isValido` = @isValido WHERE cultura.idCultura = idCultura;\n" +
                    "\n" +
                    "ELSE\n" +
                    "\tSIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'É uma zona inválida!';\n" +
                    "END IF;\n" +
                    "ELSE\n" +
                    "\tSIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'É uma cultura inválida para o utilizador atual!';\n" +
                    "END IF;\n" +
                    "END";
            statementLocalhost.executeUpdate(dropProcedimentoAlterarCultura);
            statementLocalhost.executeUpdate(createAlterarCulturaProcedure);

            //criar procedimento de atribuir um utilizador a uma cultura
            String dropProcedimentoAtribuirUtilizadorCultura = "DROP PROCEDURE IF EXISTS `atribuir_cultura_investigador`";
            String createAtribuirUtilizadorCulturaProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `atribuir_cultura_investigador`(IN `idCultura` INT, IN `idUtilizador` INT) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN \n" +
                    "\n" +
                    "IF (SELECT tipoUtilizador FROM utilizador WHERE utilizador.idUtilizador = idUtilizador) = 'i' THEN \n" +
                    "\tIF (SELECT COUNT(*) FROM cultura WHERE idCultura = cultura.idCultura and cultura.lumLimSup > cultura.lumLimSupAlerta and cultura.lumLimSupAlerta > cultura.lumLimInfAlerta and cultura.lumLimInfAlerta > cultura.lumLimInf and cultura.tempLimSup >  cultura.tempLimSupAlerta and cultura.tempLimSupAlerta > cultura.tempLimInfAlerta and cultura.tempLimInfAlerta > cultura.tempLimInf and cultura.humLimSup >  cultura.humLimSupAlerta and cultura.humLimSupAlerta > cultura.humLimInfAlerta and cultura.humLimInfAlerta > cultura.humLimInf) > 0 THEN\n" +
                    "\tSET @isValido := 1; \n" +
                    "ELSE \n" +
                    "    SET @isValido := 0;\n" +
                    "END IF;\n" +
                    "\tUPDATE `cultura` SET `idUtilizador` = idUtilizador, `isValido` = @isValido WHERE `cultura`.`idCultura` = idCultura; \n" +
                    "ELSE \n" +
                    "\tSIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insira um idUtilizador válido e que seja Investigador!'; \n" +
                    "END IF; \n" +
                    "END;";
            statementLocalhost.executeUpdate(dropProcedimentoAtribuirUtilizadorCultura);
            statementLocalhost.executeUpdate(createAtribuirUtilizadorCulturaProcedure);

            //criar procedimento de desatribuir um utilizador a uma cultura
            String dropProcedimentoDesatribuirUtilizadorCultura = "DROP PROCEDURE IF EXISTS `desatribuir_cultura_investigador`";
            String createDesatribuirUtilizadorCulturaProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `desatribuir_cultura_investigador`(IN `idCultura` INT) NOT DETERMINISTIC MODIFIES SQL DATA SQL SECURITY DEFINER BEGIN UPDATE `cultura` SET `idUtilizador` = NULL, `isValido` = 0 WHERE `cultura`.`idCultura` = idCultura; END;";
            statementLocalhost.executeUpdate(dropProcedimentoDesatribuirUtilizadorCultura);
            statementLocalhost.executeUpdate(createDesatribuirUtilizadorCulturaProcedure);

            //criar procedimento de remover utilizador
            String dropProcedimentoRemoverUtilizador = "DROP PROCEDURE IF EXISTS `remover_utilizador`";
            String createRemoverUtilizadorProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `remover_utilizador`(IN `email` VARCHAR(50)) NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN DELETE FROM `utilizador` WHERE `email` = email; SET @user := CONCAT('DROP USER ''', email, '''@''localhost'''); PREPARE stmt FROM @user; EXECUTE stmt; END;";
            statementLocalhost.executeUpdate(dropProcedimentoRemoverUtilizador);
            statementLocalhost.executeUpdate(createRemoverUtilizadorProcedure);

            //criar procedimento de alterar utilizador
            String dropProcedimentoAlterarUtilizador = "DROP PROCEDURE IF EXISTS `alterar_utilizador`";
            String createAlterarUtilizadorProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `alterar_utilizador`(IN `emailInput` VARCHAR(50), IN `nomeUtilizadorInput` VARCHAR(50), IN `intervaloMinimoAvisosInput` TIME) NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN UPDATE `utilizador` SET `nomeUtilizador`= nomeUtilizadorInput, `intervaloMinimoAvisos` = intervaloMinimoAvisosInput WHERE `email` = emailInput; END;";
            statementLocalhost.executeUpdate(dropProcedimentoAlterarUtilizador);
            statementLocalhost.executeUpdate(createAlterarUtilizadorProcedure);

            //criar procedimento que cria um utilizador
            String dropProcedimentoUtilizador = "DROP PROCEDURE IF EXISTS `criar_utilizador`";
            String createUtilizadorProcedure = "CREATE DEFINER=`root`@`localhost` PROCEDURE `criar_utilizador`(IN `username` VARCHAR(50), IN `email` VARCHAR(50), IN `pwd` VARCHAR(50), IN `tipoUtilizador` CHAR(1), IN `intervaloMinimoAvisos` TIME) NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN\r\n"
            		+ "\r\n"
            		+ "IF tipoUtilizador = 'A' or tipoUtilizador = 'I' or tipoUtilizador = 'T' THEN\r\n"
            		+ "SET @user := CONCAT('CREATE USER ''', email, '''@''localhost''', ' IDENTIFIED BY ''', pwd, '''');\r\n"
            		+ "PREPARE stmt FROM @user; \r\n"
            		+ "EXECUTE stmt;\r\n"
            		+ "CASE \r\n"
            		+ "WHEN tipoUtilizador = 'I' THEN \r\n"
            		+ "	SET @perm := concat('GRANT investigador TO ''',email,'''@''localhost''');\r\n"
            		+ "    SET @setrole := concat('SET DEFAULT ROLE investigador FOR ''',email,'''@''localhost''');\r\n"
            		+ "WHEN tipoUtilizador = 'A' THEN \r\n"
            		+ "	SET @perm := concat('GRANT administrador TO ''',email,'''@''localhost''');\r\n"
            		+ "    SET @setrole := concat('SET DEFAULT ROLE administrador FOR ''',email,'''@''localhost''');\r\n"
            		+ "WHEN tipoUtilizador = 'T' THEN \r\n"
            		+ "	SET @perm := concat('GRANT tecnico TO ''',email,'''@''localhost''');\r\n"
            		+ "    SET @setrole := concat('SET DEFAULT ROLE tecnico FOR ''',email,'''@''localhost''');\r\n"
            		+ "END CASE; \r\n"
            		+ "\r\n"
            		+ "PREPARE grnt FROM @perm; \r\n"
            		+ "EXECUTE grnt;\r\n"
            		+ "PREPARE setrole FROM @setrole; \r\n"
            		+ "EXECUTE setrole;\r\n"
            		+ "\r\n"
            		+ "INSERT INTO `utilizador` (`nomeUtilizador`, `tipoUtilizador`,`email`, `intervaloMinimoAvisos`) VALUES (username, tipoUtilizador ,email, intervaloMinimoAvisos);  \r\n"
            		+ "\r\n"
            		+ "ELSE \r\n"
            		+ "	SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Não existe este tipo de utilizador. Só (A)dministrador, (I)nvestigador e (T)ecnico!'; \r\n"
            		+ "END IF;\r\n"
            		+ "\r\n"
            		+ "END";
            statementLocalhost.executeUpdate(dropProcedimentoUtilizador);
            statementLocalhost.executeUpdate(createUtilizadorProcedure);

            //criar procedimento que atualiza a lista de alertas mostrada
            String dropProcedimentoAtualizarAlertas = "DROP PROCEDURE IF EXISTS `atualizar_alertas`";
            String createUtilizadorProcedureAtualizarAlertas = "CREATE DEFINER=`root`@`localhost` PROCEDURE `atualizar_alertas`(IN `tempo` TIMESTAMP) NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN SELECT alerta.tipoAlerta FROM alerta, medicao, cultura, utilizador, sensor WHERE medicao.idMedicao=alerta.idMedicao AND medicao.idSensor=sensor.idSensor AND cultura.idCultura=alerta.idCultura AND utilizador.idUtilizador=cultura.idUtilizador AND utilizador.email=(select substring_index(user(),'@localhost', 1)) AND medicao.tempo > tempo; END";
            statementLocalhost.executeUpdate(dropProcedimentoAtualizarAlertas);
            statementLocalhost.executeUpdate(createUtilizadorProcedureAtualizarAlertas);

            //criar procedimento que lista alertas
            String dropProcedimentoListarAlertas = "DROP PROCEDURE IF EXISTS `listar_alertas`";
            String createUtilizadorProcedureListarAlertas = "CREATE DEFINER=`root`@`localhost` PROCEDURE `listar_alertas`(IN `date` DATE) NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN SELECT alerta.tipoAlerta, alerta.mensagem, medicao.tempo, medicao.valorMedicao, cultura.idZona, cultura.nomeCultura, cultura.idCultura, sensor.tipoSensor, utilizador.idUtilizador FROM alerta, medicao, cultura, utilizador, sensor WHERE medicao.idMedicao=alerta.idMedicao AND medicao.idSensor=sensor.idSensor AND cultura.idCultura=alerta.idCultura AND utilizador.idUtilizador=cultura.idUtilizador AND utilizador.email=(select substring_index(user(),'@localhost', 1)) AND DATE(medicao.tempo) = date; END";
            statementLocalhost.executeUpdate(dropProcedimentoListarAlertas);
            statementLocalhost.executeUpdate(createUtilizadorProcedureListarAlertas);
            
            //criar procedimento que lista alertas do tecnico
            String dropProcedimentoListarAlertasTecnico = "DROP PROCEDURE IF EXISTS `listar_alertas_tecnico`";
            String createUtilizadorProcedureListarAlertasTecnico = "CREATE DEFINER=`root`@`localhost` PROCEDURE `listar_alertas_tecnico`(IN `date` DATE) NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN (SELECT DISTINCT alerta.tipoAlerta, alerta.mensagem, null as tempo, null as valorMedicao FROM alerta, medicao WHERE alerta.idCultura is null and (alerta.idMedicao is null)) UNION (SELECT DISTINCT alerta.tipoAlerta, alerta.mensagem, medicao.tempo, medicao.valorMedicao FROM alerta, medicao WHERE alerta.idCultura is null and alerta.idMedicao=medicao.idMedicao); END";
            statementLocalhost.executeUpdate(dropProcedimentoListarAlertasTecnico);
            statementLocalhost.executeUpdate(createUtilizadorProcedureListarAlertasTecnico);

            //criar procedimento que lista culturas
            String dropProcedimentoListarCulturas = "DROP PROCEDURE IF EXISTS `listar_culturas`";
            String createUtilizadorProcedureListarCulturas = "CREATE DEFINER=`root`@`localhost` PROCEDURE `listar_culturas`() NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN SELECT * FROM cultura INNER JOIN utilizador ON cultura.idUtilizador=utilizador.idUtilizador WHERE utilizador.email = (select substring_index(user(),'@localhost', 1)); END";
            statementLocalhost.executeUpdate(dropProcedimentoListarCulturas);
            statementLocalhost.executeUpdate(createUtilizadorProcedureListarCulturas);
            
            //criar procedimento que lista culturas válidas
            String dropProcedimentoListarCulturasValidas = "DROP PROCEDURE IF EXISTS `listar_culturas_validas`";
            String createUtilizadorProcedureListarCulturasValidas = "CREATE DEFINER=`root`@`localhost` PROCEDURE `listar_culturas_validas`() NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN SELECT * FROM cultura INNER JOIN utilizador ON cultura.idUtilizador=utilizador.idUtilizador WHERE utilizador.email = (select substring_index(user(),'@localhost', 1)) and cultura.isValido=1; END";
            statementLocalhost.executeUpdate(dropProcedimentoListarCulturasValidas);
            statementLocalhost.executeUpdate(createUtilizadorProcedureListarCulturasValidas);

            //criar procedimento que lista medicoes
            String dropProcedimentoListarMedicoes = "DROP PROCEDURE IF EXISTS `listar_medicoes`";
            String createUtilizadorProcedureListarMedicoes = "CREATE DEFINER=`root`@`localhost` PROCEDURE `listar_medicoes`(IN `tempo` TIMESTAMP) NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN SELECT medicao.tempo, medicao.valorMedicao, medicao.validacao, sensor.tipoSensor, zona.idZona FROM medicao JOIN sensor ON sensor.idSensor=medicao.idSensor JOIN zona ON zona.idZona=sensor.idZona WHERE medicao.tempo > tempo ORDER BY medicao.tempo ASC; END";
            statementLocalhost.executeUpdate(dropProcedimentoListarMedicoes);
            statementLocalhost.executeUpdate(createUtilizadorProcedureListarMedicoes);

            //criar procedimento que mostra cultura
            String dropProcedimentoMostraCultura = "DROP PROCEDURE IF EXISTS `mostra_cultura`";
            String createUtilizadorProcedureMostraCultura = "CREATE DEFINER=`root`@`localhost` PROCEDURE `mostra_cultura`(IN `idCultura` INT) NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN SELECT nomeCultura,idZona,nomeUtilizador, lumLimSup, lumLimInf, tempLimSup, tempLimInf, humLimSup, humLimInf, lumLimSupAlerta, lumLimInfAlerta, tempLimSupAlerta, tempLimInfAlerta, humLimSupAlerta, humLimInfAlerta FROM cultura, utilizador WHERE cultura.idUtilizador=utilizador.idUtilizador AND utilizador.email=(select substring_index(user(),'@localhost', 1)) AND cultura.idCultura=idCultura; END";
            statementLocalhost.executeUpdate(dropProcedimentoMostraCultura);
            statementLocalhost.executeUpdate(createUtilizadorProcedureMostraCultura);

            //criar procedimento que mostra utilizador
            String dropProcedimentoMostraUtilizador = "DROP PROCEDURE IF EXISTS `mostra_utilizador`";
            String createUtilizadorProcedureMostraUtilizador = "CREATE DEFINER=`root`@`localhost` PROCEDURE `mostra_utilizador`() NOT DETERMINISTIC NO SQL SQL SECURITY DEFINER BEGIN SELECT nomeUtilizador, intervaloMinimoAvisos FROM utilizador WHERE email=(select substring_index(user(),'@localhost', 1)); END";
            statementLocalhost.executeUpdate(dropProcedimentoMostraUtilizador);
            statementLocalhost.executeUpdate(createUtilizadorProcedureMostraUtilizador);

            //criar trigger do limite de alerta de temperatura
            String dropTriggerAlertaTemperatura = "DROP TRIGGER IF EXISTS `alerta_temperatura`";
            String createTriggerAlertaTemperatura = "CREATE DEFINER=`root`@`localhost` TRIGGER `alerta_temperatura` AFTER INSERT ON `medicao` FOR EACH ROW BEGIN\n" +
                    "DECLARE id int;\n" +
                    "DECLARE isRiscoModerado int;\n" +
                    "CREATE TEMPORARY TABLE culturas_temperatura (idCultura int);\n" +
                    "CREATE TEMPORARY TABLE ultima_medicao_temperatura (tipoAlerta varchar(100), tempo timestamp);\n" +
                    "SET @tipo :=(SELECT DISTINCT tipoSensor FROM medicao, sensor WHERE new.idSensor=sensor.idSensor);\n" +
                    "\n" +
                    "IF @tipo = 'T' and new.validacao = 'v' THEN\n" +
                    "INSERT INTO culturas_temperatura (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao and cultura.isValido=1);\n" +
                    "WHILE EXISTS(SELECT * FROM culturas_temperatura) DO\n" +
                    "SET @id := (SELECT * FROM culturas_temperatura LIMIT 1);\n" +
                    "DELETE FROM culturas_temperatura WHERE (idCultura = @id);\n" +
                    "SET @interval :=(SELECT intervaloMinimoAvisos FROM utilizador,cultura WHERE @id=cultura.idCultura and cultura.idUtilizador=utilizador.idUtilizador);\n" +
                    "INSERT INTO ultima_medicao_temperatura (SELECT tipoAlerta,tempo FROM alerta, medicao,sensor  WHERE @id=alerta.idCultura and alerta.idMedicao=medicao.idMedicao and medicao.idSensor=sensor.idSensor and sensor.tipoSensor='T' ORDER BY medicao.tempo DESC LIMIT 1);\n" +
                    "CASE\n" +
                    "WHEN (SELECT COUNT(*) FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao<cultura.tempLimSup AND new.valorMedicao>=cultura.tempLimSupAlerta) OR (new.valorMedicao>cultura.tempLimInf AND new.valorMedicao<=cultura.tempLimInfAlerta))) > 0 THEN\n" +
                    "SET @tipo_nova_medicao := 'Alerta';\n" +
                    "WHEN (SELECT COUNT(*) FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao>=cultura.tempLimSup) OR (new.valorMedicao<=cultura.tempLimInf))) > 0 THEN\n" +
                    "SET @tipo_nova_medicao := 'Critico';\n" +
                    "ELSE\n" +
                    "SET @tipo_nova_medicao := 'Bom';\n" +
                    "END CASE;\n" +
                    "CASE\n" +
                    "WHEN (SELECT COUNT(*) FROM ultima_medicao_temperatura) = 0 OR (SELECT tipoAlerta FROM ultima_medicao_temperatura) = 'Temperatura Totalmente Recuperada' OR (SELECT tipoAlerta FROM ultima_medicao_temperatura) = 'Recuperação da Temperatura Crítica - Estado Atual Bom' THEN\n" +
                    "    CASE\n" +
                    "    WHEN @tipo_nova_medicao = 'Alerta' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Temperatura', 'A temperatura encontra-se em estado de alerta para a cultura!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Critico' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Temperatura Crítica' , 'A temperatura atingiu valores não suportados pela cultura!');\n" +
                    "        ELSE BEGIN END;\n" +
                    "   \tEND CASE;\n" +
                    "WHEN (SELECT tipoAlerta FROM ultima_medicao_temperatura) = 'Alerta Temperatura' OR (SELECT tipoAlerta FROM ultima_medicao_temperatura) = 'Recuperação da Temperatura Crítica - Estado Atual Alerta' THEN\n" +
                    "    CASE\n" +
                    "    WHEN @tipo_nova_medicao = 'Bom' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Temperatura Totalmente Recuperada', 'A temperatura já não se encontra em estado de alerta para essa cultura, está tudo bem com a sua cultura!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Alerta' THEN\n" +
                    "        IF new.tempo > ADDTIME(@interval,(SELECT tempo FROM ultima_medicao_temperatura)) THEN\n" +
                    "            CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Temperatura', 'A temperatura encontra-se em estado de alerta para a cultura!');\n" +
                    "        END IF;\n" +
                    "    WHEN @tipo_nova_medicao = 'Critico' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Temperatura Crítica', 'A temperatura atingiu valores não suportados pela cultura!');\n" +
                    "    END CASE;\n" +
                    "WHEN (SELECT tipoAlerta FROM ultima_medicao_temperatura) = 'Temperatura Crítica' THEN\n" +
                    "    CASE\n" +
                    "    WHEN @tipo_nova_medicao = 'Bom' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Recuperação da Temperatura Crítica - Estado Atual Bom', 'A cultura já não se encontra em estado crítico devido à temperatura, está tudo bem com a sua cultura!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Alerta' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Recuperação da Temperatura Crítica - Estado Atual Alerta', 'A cultura já não se encontra em estado crítico devido à temperatura, mas ainda está em estado de alerta!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Critico' THEN\n" +
                    "        IF new.tempo > ADDTIME(@interval,(SELECT tempo FROM ultima_medicao_temperatura)) THEN\n" +
                    "            CALL `criar_alerta`(@id, new.idMedicao , 'Temperatura Crítica', 'A temperatura atingiu valores não suportados pela cultura!');\n" +
                    "        END IF;\n" +
                    "    END CASE;\n" +
                    "END CASE;\n" +
                    "DELETE FROM ultima_medicao_temperatura LIMIT 1;\n" +
                    "END WHILE;\n" +
                    "END IF;\n" +
                    "DROP TEMPORARY TABLE culturas_temperatura;\n" +
                    "DROP TEMPORARY TABLE ultima_medicao_temperatura;\n" +
                    "END";
            statementLocalhost.executeUpdate(dropTriggerAlertaTemperatura);
            statementLocalhost.executeUpdate(createTriggerAlertaTemperatura);

            //criar trigger do limite de alerta de humidade
            String dropTriggerAlertaHumidade = "DROP TRIGGER IF EXISTS `alerta_humidade`";
            String createTriggerAlertaHumidade = "CREATE DEFINER=`root`@`localhost` TRIGGER `alerta_humidade` AFTER INSERT ON `medicao` FOR EACH ROW BEGIN\n" +
                    "DECLARE id int;\n" +
                    "DECLARE isRiscoModerado int;\n" +
                    "CREATE TEMPORARY TABLE culturas_humidade (idCultura int);\n" +
                    "CREATE TEMPORARY TABLE ultima_medicao_humidade (tipoAlerta varchar(100), tempo timestamp);\n" +
                    "SET @tipo :=(SELECT DISTINCT tipoSensor FROM medicao, sensor WHERE new.idSensor=sensor.idSensor);\n" +
                    "\n" +
                    "IF @tipo = 'H' and new.validacao = 'v' THEN\n" +
                    "INSERT INTO culturas_humidade (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao and cultura.isValido=1);\n" +
                    "WHILE EXISTS(SELECT * FROM culturas_humidade) DO\n" +
                    "SET @id := (SELECT * FROM culturas_humidade LIMIT 1);\n" +
                    "DELETE FROM culturas_humidade WHERE (idCultura = @id);\n" +
                    "SET @interval :=(SELECT intervaloMinimoAvisos FROM utilizador,cultura WHERE @id=cultura.idCultura and cultura.idUtilizador=utilizador.idUtilizador);\n" +
                    "INSERT INTO ultima_medicao_humidade (SELECT tipoAlerta,tempo FROM alerta, medicao,sensor  WHERE @id=alerta.idCultura and alerta.idMedicao=medicao.idMedicao and medicao.idSensor=sensor.idSensor and sensor.tipoSensor='H' ORDER BY medicao.tempo DESC LIMIT 1);\n" +
                    "CASE\n" +
                    "WHEN (SELECT COUNT(*) FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao<cultura.humLimSup AND new.valorMedicao>=cultura.humLimSupAlerta) OR (new.valorMedicao>cultura.humLimInf AND new.valorMedicao<=cultura.humLimInfAlerta))) > 0 THEN\n" +
                    "SET @tipo_nova_medicao := 'Alerta';\n" +
                    "WHEN (SELECT COUNT(*) FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao>=cultura.humLimSup) OR (new.valorMedicao<=cultura.humLimInf))) > 0 THEN\n" +
                    "SET @tipo_nova_medicao := 'Critico';\n" +
                    "ELSE\n" +
                    "SET @tipo_nova_medicao := 'Bom';\n" +
                    "END CASE;\n" +
                    "CASE\n" +
                    "WHEN (SELECT COUNT(*) FROM ultima_medicao_humidade) = 0 OR (SELECT tipoAlerta FROM ultima_medicao_humidade) = 'Humidade Totalmente Recuperada' OR (SELECT tipoAlerta FROM ultima_medicao_humidade) = 'Recuperação da Humidade Crítica - Estado Atual Bom' THEN\n" +
                    "    CASE\n" +
                    "    WHEN @tipo_nova_medicao = 'Alerta' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Humidade', 'A humidade encontra-se em estado de alerta para a cultura!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Critico' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Humidade Crítica' , 'A humidade atingiu valores não suportados pela cultura!');\n" +
                    "        ELSE BEGIN END;\n" +
                    "   \tEND CASE;\n" +
                    "WHEN (SELECT tipoAlerta FROM ultima_medicao_humidade) = 'Alerta Humidade' OR (SELECT tipoAlerta FROM ultima_medicao_humidade) = 'Recuperação da Humidade Crítica - Estado Atual Alerta' THEN\n" +
                    "    CASE\n" +
                    "    WHEN @tipo_nova_medicao = 'Bom' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Humidade Totalmente Recuperada', 'A humidade já não se encontra em estado de alerta para essa cultura, está tudo bem com a sua cultura!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Alerta' THEN\n" +
                    "        IF new.tempo > ADDTIME(@interval,(SELECT tempo FROM ultima_medicao_humidade)) THEN\n" +
                    "            CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Humidade', 'A humidade encontra-se em estado de alerta para a cultura!');\n" +
                    "        END IF;\n" +
                    "    WHEN @tipo_nova_medicao = 'Critico' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Humidade Crítica', 'A humidade atingiu valores não suportados pela cultura!');\n" +
                    "    END CASE;\n" +
                    "WHEN (SELECT tipoAlerta FROM ultima_medicao_humidade) = 'Humidade Crítica' THEN\n" +
                    "    CASE\n" +
                    "    WHEN @tipo_nova_medicao = 'Bom' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Recuperação da Humidade Crítica - Estado Atual Bom', 'A cultura já não se encontra em estado crítico devido à humidade, está tudo bem com a sua cultura!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Alerta' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Recuperação da Humidade Crítica - Estado Atual Alerta', 'A cultura já não se encontra em estado crítico devido à humidade, mas ainda está em estado de alerta!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Critico' THEN\n" +
                    "        IF new.tempo > ADDTIME(@interval,(SELECT tempo FROM ultima_medicao_humidade)) THEN\n" +
                    "            CALL `criar_alerta`(@id, new.idMedicao , 'Humidade Crítica', 'A humidade atingiu valores não suportados pela cultura!');\n" +
                    "        END IF;\n" +
                    "    END CASE;\n" +
                    "END CASE;\n" +
                    "DELETE FROM ultima_medicao_humidade LIMIT 1;\n" +
                    "END WHILE;\n" +
                    "END IF;\n" +
                    "DROP TEMPORARY TABLE culturas_humidade;\n" +
                    "DROP TEMPORARY TABLE ultima_medicao_humidade;\n" +
                    "END";
            statementLocalhost.executeUpdate(dropTriggerAlertaHumidade);
            statementLocalhost.executeUpdate(createTriggerAlertaHumidade);

            //criar trigger do limite de alerta de luminosidade
            String dropTriggerAlertaLuminosidade = "DROP TRIGGER IF EXISTS `alerta_luminosidade`";
            String createTriggerAlertaLuminosidade = "CREATE DEFINER=`root`@`localhost` TRIGGER `alerta_luminosidade` AFTER INSERT ON `medicao` FOR EACH ROW BEGIN\n" +
                    "DECLARE id int;\n" +
                    "DECLARE isRiscoModerado int;\n" +
                    "CREATE TEMPORARY TABLE culturas_luminosidade (idCultura int);\n" +
                    "CREATE TEMPORARY TABLE ultima_medicao_luminosidade (tipoAlerta varchar(100), tempo timestamp);\n" +
                    "SET @tipo :=(SELECT DISTINCT tipoSensor FROM medicao, sensor WHERE new.idSensor=sensor.idSensor);\n" +
                    "\n" +
                    "IF @tipo = 'L' and new.validacao = 'v' THEN\n" +
                    "INSERT INTO culturas_luminosidade (SELECT idCultura FROM cultura, medicao, sensor, zona WHERE cultura.idZona=zona.idZona AND zona.idZona=sensor.idZona AND medicao.idSensor=sensor.idSensor AND new.idMedicao=medicao.idMedicao and cultura.isValido=1);\n" +
                    "WHILE EXISTS(SELECT * FROM culturas_luminosidade) DO\n" +
                    "SET @id := (SELECT * FROM culturas_luminosidade LIMIT 1);\n" +
                    "DELETE FROM culturas_luminosidade WHERE (idCultura = @id);\n" +
                    "SET @interval :=(SELECT intervaloMinimoAvisos FROM utilizador,cultura WHERE @id=cultura.idCultura and cultura.idUtilizador=utilizador.idUtilizador);\n" +
                    "INSERT INTO ultima_medicao_luminosidade (SELECT tipoAlerta,tempo FROM alerta, medicao,sensor  WHERE @id=alerta.idCultura and alerta.idMedicao=medicao.idMedicao and medicao.idSensor=sensor.idSensor and sensor.tipoSensor='L' ORDER BY medicao.tempo DESC LIMIT 1);\n" +
                    "CASE\n" +
                    "WHEN (SELECT COUNT(*) FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao<cultura.lumLimSup AND new.valorMedicao>=cultura.lumLimSupAlerta) OR (new.valorMedicao>cultura.lumLimInf AND new.valorMedicao<=cultura.lumLimInfAlerta))) > 0 THEN\n" +
                    "SET @tipo_nova_medicao := 'Alerta';\n" +
                    "WHEN (SELECT COUNT(*) FROM cultura WHERE @id=cultura.idCultura AND ((new.valorMedicao>=cultura.lumLimSup) OR (new.valorMedicao<=cultura.lumLimInf))) > 0 THEN\n" +
                    "SET @tipo_nova_medicao := 'Critico';\n" +
                    "ELSE\n" +
                    "SET @tipo_nova_medicao := 'Bom';\n" +
                    "END CASE;\n" +
                    "CASE\n" +
                    "WHEN (SELECT COUNT(*) FROM ultima_medicao_luminosidade) = 0 OR (SELECT tipoAlerta FROM ultima_medicao_luminosidade) = 'Luminosidade Totalmente Recuperada' OR (SELECT tipoAlerta FROM ultima_medicao_luminosidade) = 'Recuperação da Luminosidade Crítica - Estado Atual Bom' THEN\n" +
                    "    CASE\n" +
                    "    WHEN @tipo_nova_medicao = 'Alerta' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Luminosidade', 'A luminosidade encontra-se em estado de alerta para a cultura!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Critico' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Luminosidade Crítica' , 'A luminosidade atingiu valores não suportados pela cultura!');\n" +
                    "        ELSE BEGIN END;\n" +
                    "   \tEND CASE;\n" +
                    "WHEN (SELECT tipoAlerta FROM ultima_medicao_luminosidade) = 'Alerta Luminosidade' OR (SELECT tipoAlerta FROM ultima_medicao_luminosidade) = 'Recuperação da Luminosidade Crítica - Estado Atual Alerta' THEN\n" +
                    "    CASE\n" +
                    "    WHEN @tipo_nova_medicao = 'Bom' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Luminosidade Totalmente Recuperada', 'A luminosidade já não se encontra em estado de alerta para essa cultura, está tudo bem com a sua cultura!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Alerta' THEN\n" +
                    "        IF new.tempo > ADDTIME(@interval,(SELECT tempo FROM ultima_medicao_luminosidade)) THEN\n" +
                    "            CALL `criar_alerta`(@id, new.idMedicao , 'Alerta Luminosidade', 'A luminosidade encontra-se em estado de alerta para a cultura!');\n" +
                    "        END IF;\n" +
                    "    WHEN @tipo_nova_medicao = 'Critico' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Luminosidade Crítica', 'A luminosidade atingiu valores não suportados pela cultura!');\n" +
                    "    END CASE;\n" +
                    "WHEN (SELECT tipoAlerta FROM ultima_medicao_luminosidade) = 'Luminosidade Crítica' THEN\n" +
                    "    CASE\n" +
                    "    WHEN @tipo_nova_medicao = 'Bom' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Recuperação da Luminosidade Crítica - Estado Atual Bom', 'A cultura já não se encontra em estado crítico devido à luminosidade, está tudo bem com a sua cultura!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Alerta' THEN\n" +
                    "        CALL `criar_alerta`(@id, new.idMedicao , 'Recuperação da Luminosidade Crítica - Estado Atual Alerta', 'A cultura já não se encontra em estado crítico devido à luminosidade, mas ainda está em estado de alerta!');\n" +
                    "    WHEN @tipo_nova_medicao = 'Critico' THEN\n" +
                    "        IF new.tempo > ADDTIME(@interval,(SELECT tempo FROM ultima_medicao_luminosidade)) THEN\n" +
                    "            CALL `criar_alerta`(@id, new.idMedicao , 'Luminosidade Crítica', 'A luminosidade atingiu valores não suportados pela cultura!');\n" +
                    "        END IF;\n" +
                    "    END CASE;\n" +
                    "END CASE;\n" +
                    "DELETE FROM ultima_medicao_luminosidade LIMIT 1;\n" +
                    "END WHILE;\n" +
                    "END IF;\n" +
                    "DROP TEMPORARY TABLE culturas_luminosidade;\n" +
                    "DROP TEMPORARY TABLE ultima_medicao_luminosidade;\n" +
                    "END;";
            statementLocalhost.executeUpdate(dropTriggerAlertaLuminosidade);
            statementLocalhost.executeUpdate(createTriggerAlertaLuminosidade);

            //criar trigger de alerta de medicao fora dos limites
            String dropTriggerAlertaSensor = "DROP TRIGGER IF EXISTS `alerta_sensor`";
            String createTriggerAlertaSensor = "CREATE DEFINER=`root`@`localhost` TRIGGER `alerta_sensor` AFTER INSERT ON `medicao` FOR EACH ROW BEGIN IF new.validacao = 's' THEN CALL `criar_alerta`(NULL, NULL, 'Alerta Valor de Medição Fora dos Limites do Sensor', 'Foi registada uma medição com um valor que ultrapassa os limites de hardware do sensor.'); END IF; END";
            statementLocalhost.executeUpdate(dropTriggerAlertaSensor);
            statementLocalhost.executeUpdate(createTriggerAlertaSensor);

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


            //Criar ROLE investigador
            String dropRoleInvestigador = "DROP ROLE IF EXISTS `investigador`;";
            String createInvestigador = "CREATE ROLE investigador;";
            String privilegiosProcedureInvestigador = "GRANT EXECUTE ON PROCEDURE sid2021.alterar_cultura TO 'investigador'";
            String privilegiosUserProcedureInvestigador = "GRANT EXECUTE ON PROCEDURE sid2021.alterar_utilizador TO 'investigador'";
            String privilegiosAtualizarAlertasProcedureInvestigador = "GRANT EXECUTE ON PROCEDURE sid2021.atualizar_alertas TO 'investigador'";
            String privilegiosListarAlertasProcedureInvestigador = "GRANT EXECUTE ON PROCEDURE sid2021.listar_alertas TO 'investigador'";
            String privilegiosListarCulturasProcedureInvestigador = "GRANT EXECUTE ON PROCEDURE sid2021.listar_culturas TO 'investigador'";
            String privilegiosListarCulturasValidasProcedureInvestigador = "GRANT EXECUTE ON PROCEDURE sid2021.listar_culturas_validas TO 'investigador'";
            String privilegiosListarMedicoesProcedureInvestigador = "GRANT EXECUTE ON PROCEDURE sid2021.listar_medicoes TO 'investigador'";
            String privilegiosMostraCulturaProcedureInvestigador = "GRANT EXECUTE ON PROCEDURE sid2021.mostra_cultura TO 'investigador'";
            String privilegiosMostraUtilizadorProcedureInvestigador = "GRANT EXECUTE ON PROCEDURE sid2021.mostra_utilizador TO 'investigador'";
            statementLocalhost.executeUpdate(dropRoleInvestigador);
            statementLocalhost.executeUpdate(createInvestigador);
            statementLocalhost.executeUpdate(privilegiosProcedureInvestigador);
            statementLocalhost.executeUpdate(privilegiosUserProcedureInvestigador);
            statementLocalhost.executeUpdate(privilegiosAtualizarAlertasProcedureInvestigador);
            statementLocalhost.executeUpdate(privilegiosListarAlertasProcedureInvestigador);
            statementLocalhost.executeUpdate(privilegiosListarCulturasProcedureInvestigador);
            statementLocalhost.executeUpdate(privilegiosListarCulturasValidasProcedureInvestigador);
            statementLocalhost.executeUpdate(privilegiosListarMedicoesProcedureInvestigador);
            statementLocalhost.executeUpdate(privilegiosMostraCulturaProcedureInvestigador);
            statementLocalhost.executeUpdate(privilegiosMostraUtilizadorProcedureInvestigador);

            //Criar ROLE administrador
            String dropRoleAdmin = "DROP ROLE IF EXISTS `administrador`;";
            String createAdmin = "CREATE ROLE administrador;";
            String privilegiosSelectCulturaAdministrador = "GRANT SELECT ON `sid2021`.`cultura` TO 'administrador';";
            String privilegiosSelectUtilizadorAdministrador = "GRANT SELECT ON `sid2021`.`utilizador` TO 'administrador';";
            String privilegiosCriarCulturaProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.criar_cultura TO 'administrador'";
            String privilegiosRemoverCulturaProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.remover_cultura TO 'administrador'";
            String privilegiosAtribuirCulturaInvestigadorProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.atribuir_cultura_investigador TO 'administrador'";
            String privilegiosDesatribuirCulturaInvestigadorProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.desatribuir_cultura_investigador TO 'administrador'";
            String privilegiosAlterarCulturaProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.alterar_cultura TO 'administrador'";
            String privilegiosCriarUtilizadorProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.criar_utilizador TO 'administrador'";
            String privilegiosRemoverUtilizadorProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.remover_utilizador TO 'administrador'";
            String privilegiosAlterarUtilizadorProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.alterar_utilizador TO 'administrador'";
            String privilegiosAtualizarAlertasProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.atualizar_alertas TO 'administrador'";
            String privilegiosListarAlertasProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.listar_alertas TO 'administrador'";
            String privilegiosListarCulturasProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.listar_culturas TO 'administrador'";
            String privilegiosListarCulturasValidasProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.listar_culturas_validas TO 'administrador'";
            String privilegiosListarMedicoesProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.listar_medicoes TO 'administrador'";
            String privilegiosMostraCulturaProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.mostra_cultura TO 'administrador'";
            String privilegiosMostraUtilizadorProcedureAdministrador = "GRANT EXECUTE ON PROCEDURE sid2021.mostra_utilizador TO 'administrador'";
            statementLocalhost.executeUpdate(dropRoleAdmin);
            statementLocalhost.executeUpdate(createAdmin);
            statementLocalhost.executeUpdate(privilegiosSelectCulturaAdministrador);
            statementLocalhost.executeUpdate(privilegiosSelectUtilizadorAdministrador);
            statementLocalhost.executeUpdate(privilegiosCriarCulturaProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosRemoverCulturaProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosAtribuirCulturaInvestigadorProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosDesatribuirCulturaInvestigadorProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosAlterarCulturaProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosCriarUtilizadorProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosRemoverUtilizadorProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosAlterarUtilizadorProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosAtualizarAlertasProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosListarAlertasProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosListarCulturasProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosListarCulturasValidasProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosListarMedicoesProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosMostraCulturaProcedureAdministrador);
            statementLocalhost.executeUpdate(privilegiosMostraUtilizadorProcedureAdministrador);

            //Criar ROLE técnico de manutenção
            String dropRoleTecnico = "DROP ROLE IF EXISTS `tecnico`;";
            String createTecnico = "CREATE ROLE tecnico;";
            String privilegiosTecnico = "GRANT EXECUTE ON PROCEDURE sid2021.listar_alertas_tecnico TO 'tecnico'";
            statementLocalhost.executeUpdate(dropRoleTecnico);
            statementLocalhost.executeUpdate(createTecnico);
            statementLocalhost.executeUpdate(privilegiosTecnico);

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