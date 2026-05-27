package com.geradoretiquetavalidade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EtiquetaDAO {

    public static void salvarDadoAuxiliar(String tipo, String valor) {
        String sql = "INSERT OR IGNORE INTO dados_auxiliares (tipo, valor) VALUES (?, ?);";
        try (Connection conn = FabricaConexao.getConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tipo.toUpperCase());
            pstmt.setString(2, valor.trim());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao salvar dado auxiliar no SQLite", e);
        }
    }

    public static List<String> carregarDadosPorTipo(String tipo) {
        List<String> dados = new ArrayList<>();
        String sql = "SELECT valor FROM dados_auxiliares WHERE tipo = ? ORDER BY valor ASC;"; // <-- Corrigido para ASC
        try (Connection conn = FabricaConexao.getConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tipo.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    dados.add(rs.getString("valor"));
                }
            }
        } catch (SQLException e) {
            LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao carregar dados auxiliares do SQLite", e);
        }
        return dados;
    }

    public static void salvarProduto(String nome, int diasValidade) {
        String sql = "INSERT INTO produtos (nome, dias_validade) VALUES (?, ?) " +
                     "ON CONFLICT(nome) DO UPDATE SET dias_validade = excluded.dias_validade;";
        try (Connection conn = FabricaConexao.getConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome.trim());
            pstmt.setInt(2, diasValidade);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao salvar produto no SQLite", e);
        }
    }

    public static String buscarDiasDoProduto(String nome) {
        String sql = "SELECT dias_validade FROM produtos WHERE nome = ?;";
        try (Connection conn = FabricaConexao.getConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return String.valueOf(rs.getInt("dias_validade"));
                }
            }
        } catch (SQLException e) {
            LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao buscar validade do produto no SQLite", e);
        }
        return "";
    }

    public static List<String> carregarProdutos() {
        List<String> produtos = new ArrayList<>();
        String sql = "SELECT nome FROM produtos ORDER BY nome ASC;"; // <-- Corrigido para ASC
        try (Connection conn = FabricaConexao.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                produtos.add(rs.getString("nome"));
            }
        } catch (SQLException e) {
            LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao listar produtos do SQLite", e);
        }
        return produtos;
    }
} // <-- Esta é a chave que estava faltando e causava o erro!