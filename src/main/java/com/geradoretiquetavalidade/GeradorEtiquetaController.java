package com.geradoretiquetavalidade;

// IMPORTS JAVAFX CORRIGIDOS
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

// IMPORTS NATIVOS DO JAVA
import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class GeradorEtiquetaController {

    private final GeradorEtiquetaView view;
    private String caminhoUltimoPdfGerado = "";
    private final DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public GeradorEtiquetaController(GeradorEtiquetaView view) {
        this.view = view;
        vincularEventos();
        inicializarDadosTela();
        configurarNavegacaoTeclado();
    }

    /**
     * NOVA FUNÇÃO: Cria a sequência lógica de focos ao pressionar a tecla Enter
     */
    private void configurarNavegacaoTeclado() {
        // Criamos o fluxo na ordem natural de preenchimento da tela
        configurarFocoAoPremerEnter(view.comboProduto, view.comboCategoria);
        configurarFocoAoPremerEnter(view.comboCategoria, view.pickerDataManipulacao);
        configurarFocoAoPremerEnter(view.pickerDataManipulacao, view.txtDiasValidade);
        configurarFocoAoPremerEnter(view.txtDiasValidade, view.comboEstocagem);
        configurarFocoAoPremerEnter(view.comboEstocagem, view.comboResponsavel);
        configurarFocoAoPremerEnter(view.comboResponsavel, view.spinnerQuantidade);
        
        // Quando estiver no último campo (Quantidade), o Enter aciona direto o botão de Gerar!
        view.spinnerQuantidade.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                acaoGerarPdf();
                event.consume(); // Evita comportamentos duplicados do sistema
            }
        });
    }

    /**
 * Método auxiliar corrigido: Usa KEY_RELEASED para não conflitar com o Autocomplete
 */
private void configurarFocoAoPremerEnter(Node atual, Node seguinte) {
    // Se for um ComboBox, precisamos escutar o editor de texto dele por causa do autocomplete
    Node nodoEscuta = (atual instanceof ComboBox) ? ((ComboBox<?>) atual).getEditor() : atual;

    // Mudamos de KEY_PRESSED para KEY_RELEASED para pegar o Enter após a confirmação do Autocomplete
    nodoEscuta.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
        if (event.getCode() == KeyCode.ENTER) {
            
            // Se o próximo campo for um ComboBox, garante que a caixinha dele feche antes de focar
            if (seguinte instanceof ComboBox) {
                ((ComboBox<?>) seguinte).hide();
            }
            
            seguinte.requestFocus(); // Transfere o foco para o próximo campo
            event.consume(); // Consome o evento para evitar repetições
        }
    });
}
    
    
    
    private void inicializarDadosTela() {
        view.comboProduto.getItems().addAll(EtiquetaDAO.carregarProdutos());
        FxUtil.habilitarAutocomplete(view.comboProduto);
        atualizarCombo(view.comboCategoria, "CATEGORIA");
        atualizarCombo(view.comboEstocagem, "ESTOCAGEM");
        atualizarCombo(view.comboResponsavel, "RESPONSAVEL");
    }

    private void vincularEventos() {
        // Escuta a seleção do produto para trazer a validade automática
            view.comboProduto.setOnAction(e -> {
        String produtoSelecionado = view.comboProduto.getValue();
            if (produtoSelecionado != null) {
        String dias = EtiquetaDAO.buscarDiasDoProduto(produtoSelecionado);
            view.txtDiasValidade.setText(dias);
        }
    });

        // Eventos dos botões de ação principais
        view.btnGerar.setOnAction(e -> acaoGerarPdf());
        view.btnVisualizar.setOnAction(e -> acaoVisualizarPdf());
        view.btnImprimirDireto.setOnAction(e -> acaoImprimirDireto());

        // Eventos dos botões de cadastro rápido "+"
        view.btnAddProduto.setOnAction(e -> acaoCadastrarProduto());
        view.btnAddCategoria.setOnAction(e -> acaoCadastrarRapido("CATEGORIA", view.comboCategoria));
        view.btnAddEstocagem.setOnAction(e -> acaoCadastrarRapido("ESTOCAGEM", view.comboEstocagem));
        view.btnAddResponsavel.setOnAction(e -> acaoCadastrarRapido("RESPONSAVEL", view.comboResponsavel));
    }

    private void atualizarCombo(ComboBox<String> combo, String tipo) {
    combo.getItems().clear();
    combo.getItems().addAll(EtiquetaDAO.carregarDadosPorTipo(tipo));
    FxUtil.habilitarAutocomplete(combo); 
}

    private void acaoGerarPdf() {
    if (view.comboProduto.getEditor().getText().trim().isEmpty() || view.txtDiasValidade.getText().trim().isEmpty() || 
        view.pickerDataManipulacao.getValue() == null || view.comboResponsavel.getEditor().getText().trim().isEmpty()) {
        view.exibirAlerta(Alert.AlertType.WARNING, "Aviso", "Campos Obrigatórios", "Por favor, preencha todos os campos.");
        return;
    }

    try {
        LocalDate dataManipulacao = view.pickerDataManipulacao.getValue();
        LocalDate dataAtual = LocalDate.now();
        
        // --- NOVA VALIDAÇÃO: Bloqueia datas anteriores a 3 dias atrás ---
        LocalDate limitePassado = dataAtual.minusDays(3);
        if (dataManipulacao.isBefore(limitePassado)) {
            String dataLimiteFormatada = limitePassado.format(formatador);
            
            // Registra a tentativa inválida no arquivo de log
            LogConfig.getLogger().warning("Tentativa de gerar etiqueta com data retroativa bloqueada. Data selecionada: " 
                    + dataManipulacao.format(formatador) + " | Limite permitido: " + dataLimiteFormatada);
            
            view.exibirAlerta(Alert.AlertType.WARNING, "Data Inválida", "Data de Manipulação muito antiga", 
                    "Por motivos de segurança, o sistema só permite retroagir até 3 dias antes da data atual.\n\n"
                    + "Data mínima permitida: " + dataLimiteFormatada);
            return;
        }

        // Se passar na validação da data, o fluxo normal continua...
        String produto = view.comboProduto.getEditor().getText().trim();
        String categoria = view.comboCategoria.getEditor().getText().trim();
        int diasValidade = Integer.parseInt(view.txtDiasValidade.getText().trim());
        String estocagem = view.comboEstocagem.getEditor().getText().trim();
        String responsavel = view.comboResponsavel.getEditor().getText().trim();
        int quantidade = view.spinnerQuantidade.getValue();

        Etiqueta etiqueta = new Etiqueta(produto, categoria, dataManipulacao, diasValidade, estocagem, responsavel);

        caminhoUltimoPdfGerado = System.getProperty("user.home") + "/Desktop/etiquetas_javafx.pdf";
        etiqueta.exportarParaPdf(caminhoUltimoPdfGerado, quantidade);

        view.btnVisualizar.setDisable(false);
        view.btnImprimirDireto.setDisable(false);

        String textoSucesso = "Arquivo gerado na Área de Trabalho!\nValidade: " + etiqueta.calcularDataValidade().format(formatador);
        view.exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "PDF Pronto", textoSucesso);

    } catch (NumberFormatException ex) {
        LogConfig.getLogger().warning("Usuário digitou um valor inválido em Dias de Validade. Texto digitado: " + view.txtDiasValidade.getText());
        view.exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro de Digitação", "O campo 'Dias de Validade' precisa ser um número.");
    } catch (Exception ex) {
        LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Falha crítica ao tentar exportar o PDF da etiqueta", ex);
        view.exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro Crítico", "Falha ao gerar etiquetas. Detalhes salvos em erros.log");
    }
}

    private void acaoVisualizarPdf() {
        try {
            File arquivo = new File(caminhoUltimoPdfGerado);
            if (arquivo.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(arquivo);
            }
        } catch (Exception ex) {
            LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao tentar abrir o visualizador de PDF nativo", ex);
            view.exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao abrir", ex.getMessage());
        }
    }

    private void acaoImprimirDireto() {
        try {
            File arquivo = new File(caminhoUltimoPdfGerado);
            if (arquivo.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().print(arquivo);
            }
        } catch (Exception ex) {
            LogConfig.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao enviar requisição de impressão para o Sistema Operacional", ex);
            view.exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha de Impressão", ex.getMessage());
        }
    }

    private void acaoCadastrarProduto() {
        TextInputDialog dialogNome = new TextInputDialog();
        dialogNome.setTitle("Cadastrar Produto");
        dialogNome.setHeaderText("Passo 1 de 2: Nome do Produto");
        dialogNome.setContentText("Digite o nome:");
        
        Optional<String> resultadoNome = dialogNome.showAndWait();
        if (resultadoNome.isPresent() && !resultadoNome.get().trim().isEmpty()) {
            String nomeProd = resultadoNome.get().trim();
            
            TextInputDialog dialogDias = new TextInputDialog();
            dialogDias.setTitle("Cadastrar Validade");
            dialogDias.setHeaderText("Passo 2 de 2: Dias de Validade para: " + nomeProd);
            dialogDias.setContentText("Digite os dias:");
            
            Optional<String> resultadoDias = dialogDias.showAndWait();
            if (resultadoDias.isPresent() && !resultadoDias.get().trim().isEmpty()) {
                //GerenciadorDados.salvarDado("PRODUTO", nomeProd + ";" + resultadoDias.get().trim());
                EtiquetaDAO.salvarProduto(nomeProd, Integer.parseInt(resultadoDias.get().trim()));
                atualizarCombo(view.comboProduto, "PRODUTO");
                view.comboProduto.getItems().clear();
                view.comboProduto.getItems().addAll(EtiquetaDAO.carregarProdutos());
                //view.comboProduto.setValue(nomeProd);
                //view.txtDiasValidade.setText(resultadoDias.get().trim());
            }
        }
    }

    private void acaoCadastrarRapido(String tipo, ComboBox<String> combo) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cadastrar Novo");
        dialog.setHeaderText("Cadastrar em: " + tipo);
        dialog.setContentText("Digite o valor:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(valor -> {
            if (!valor.trim().isEmpty()) {
                //GerenciadorDados.salvarDado(tipo, valor.trim());
                EtiquetaDAO.salvarDadoAuxiliar(tipo, valor.trim());
                atualizarCombo(combo, tipo);
                combo.setValue(valor.trim());
            }
        });
    }
}