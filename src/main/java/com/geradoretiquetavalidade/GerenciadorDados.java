package com.geradoretiquetavalidade;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorDados {

    private static final String ARQUIVO_DADOS = System.getProperty("user.home") + "/Desktop/dados_sistema.txt";

    // Salva a informação. Se for produto, o valor virá como "Nome;Dias"
    public static void salvarDado(String tipo, String valor) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO_DADOS, true))) {
            writer.write(tipo + ":" + valor);
            writer.newLine();
        } catch (IOException e) {
            // Exemplo para colocar dentro dos blocos catch da classe GerenciadorDados:
            LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Erro de I/O no banco de dados textual", e);
        }
    }

    // Carrega os dados filtrados. No caso de produto, remove os ";dias" para exibir apenas o nome na tela
    public static List<String> carregarDadosPorTipo(String tipo) {
        List<String> dados = new ArrayList<>();
        File arquivo = new File(ARQUIVO_DADOS);
        
        if (!arquivo.exists()) {
            return dados;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(":")) {
                    String[] partes = line.split(":", 2);
                    if (partes[0].trim().equalsIgnoreCase(tipo)) {
                        String valor = partes[1].trim();
                        
                        // SE FOR PRODUTO: Pega apenas o nome antes do ";" para exibir no ComboBox
                        if (tipo.equalsIgnoreCase("PRODUTO") && valor.contains(";")) {
                            valor = valor.split(";")[0];
                        }
                        
                        if (!dados.contains(valor)) {
                            dados.add(valor);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Erro de I/O no banco de dados textual", e);
        }
        return dados;
    }

    // NOVO MÉTODO: Procura no TXT quantos dias de validade pertencem àquele produto
    public static String buscarDiasDoProduto(String nomeProduto) {
        File arquivo = new File(ARQUIVO_DADOS);
        if (!arquivo.exists()) return "";

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("PRODUTO:")) {
                    String valor = line.split(":", 2)[1].trim();
                    if (valor.contains(";")) {
                        String[] partesProduto = valor.split(";");
                        String nomeSalvo = partesProduto[0].trim();
                        String diasSalvos = partesProduto[1].trim();
                        
                        if (nomeSalvo.equalsIgnoreCase(nomeProduto.trim())) {
                            return diasSalvos; // Encontrou o produto, retorna os dias
                        }
                    }
                }
            }
        } catch (IOException e) {
            LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Erro de I/O no banco de dados textual", e);
        }
        return ""; // Se não achar, retorna vazio
    }
}