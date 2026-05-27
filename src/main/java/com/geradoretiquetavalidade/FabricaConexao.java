package com.geradoretiquetavalidade;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class FabricaConexao {

    // O arquivo do banco de dados será criado na pasta do usuário
    private static final String URL = "jdbc:sqlite:" + System.getProperty("user.home") + "/etiquetas_dados.db";

    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void inicializarBanco() {
        String sqlProdutos = "CREATE TABLE IF NOT EXISTS produtos (" +
                             "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                             "nome TEXT UNIQUE NOT NULL, " +
                             "dias_validade INTEGER NOT NULL);";

        String sqlAuxiliares = "CREATE TABLE IF NOT EXISTS dados_auxiliares (" +
                               "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                               "tipo TEXT NOT NULL, " + // CATEGORIA, ESTOCAGEM, RESPONSAVEL
                               "valor TEXT NOT NULL, " +
                               "UNIQUE(tipo, valor));";

        try (Connection conn = getConexao();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sqlProdutos);
            stmt.execute(sqlAuxiliares);
            
        } catch (SQLException e) {
            LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao inicializar tabelas do SQLite", e);
        }
    }
}