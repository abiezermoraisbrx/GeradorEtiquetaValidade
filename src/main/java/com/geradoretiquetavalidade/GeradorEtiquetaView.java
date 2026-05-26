package com.geradoretiquetavalidade;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GeradorEtiquetaView {

    protected ComboBox<String> comboProduto = new ComboBox<>();
    protected DatePicker pickerDataManipulacao = new DatePicker(LocalDate.now()); 
    protected TextField txtDiasValidade = new TextField();
    protected ComboBox<String> comboCategoria = new ComboBox<>();
    protected ComboBox<String> comboEstocagem = new ComboBox<>();
    protected ComboBox<String> comboResponsavel = new ComboBox<>();
    protected Spinner<Integer> spinnerQuantidade = new Spinner<>(1, 100, 1);
    
    // INCLUSÃO DE ÍCONES UNICODE NOS TEXTOS DOS BOTÕES
    // \u2699 = Engrenagem/Config, \uD83D\uDCC4 = Página/Documento, \uD83D\uDDAA = Impressora
    protected Button btnGerar = new Button("\u2699  1. Gerar PDF");
    protected Button btnVisualizar = new Button("\uD83D\uDCC4  2. Visualizar");
    protected Button btnImprimirDireto = new Button("\uD83D\uDDAA  3. Imprimir Direto");
    
    // \u2795 = Símbolo de "+" estilizado em negrito
    protected Button btnAddProduto = new Button("\u2795");
    protected Button btnAddCategoria = new Button("\u2795");
    protected Button btnAddEstocagem = new Button("\u2795");
    protected Button btnAddResponsavel = new Button("\u2795");

    private final DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Scene scene;

    public GeradorEtiquetaView() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        // Configurações básicas de larguras máximas
        comboProduto.setMaxWidth(Double.MAX_VALUE);
        comboCategoria.setMaxWidth(Double.MAX_VALUE);
        comboEstocagem.setMaxWidth(Double.MAX_VALUE);
        comboResponsavel.setMaxWidth(Double.MAX_VALUE);
        pickerDataManipulacao.setMaxWidth(Double.MAX_VALUE);
        spinnerQuantidade.setMaxWidth(Double.MAX_VALUE);
        
        configurarAlturasCampos();
        configurarFormatadorData();

        // Atribuição das classes CSS
        btnGerar.getStyleClass().add("btn-gerar");
        btnVisualizar.getStyleClass().add("btn-visualizar");
        btnImprimirDireto.getStyleClass().add("btn-imprimir");
        
        btnAddProduto.getStyleClass().add("button-add");
        btnAddCategoria.getStyleClass().add("button-add");
        btnAddEstocagem.getStyleClass().add("button-add");
        btnAddResponsavel.getStyleClass().add("button-add");

        // Configuração de tamanho dos botões de ação
        btnGerar.setMaxWidth(Double.MAX_VALUE); btnGerar.setMinHeight(38);
        btnVisualizar.setMaxWidth(Double.MAX_VALUE); btnVisualizar.setMinHeight(38);
        btnImprimirDireto.setMaxWidth(Double.MAX_VALUE); btnImprimirDireto.setMinHeight(38);
        
        btnVisualizar.setDisable(true);
        btnImprimirDireto.setDisable(true);

        // Montando a grade
        adicionarLinhaGrade(grid, "Produto / Item:", comboProduto, btnAddProduto, 0);
        adicionarLinhaGrade(grid, "Categoria:", comboCategoria, btnAddCategoria, 1);
        
        grid.add(new Label("Data de Manipulação:"), 0, 2); 
        grid.add(pickerDataManipulacao, 1, 2);

        grid.add(new Label("Dias de Validade:"), 0, 3); 
        grid.add(txtDiasValidade, 1, 3);

        adicionarLinhaGrade(grid, "Estocagem:", comboEstocagem, btnAddEstocagem, 4);
        adicionarLinhaGrade(grid, "Responsável:", comboResponsavel, btnAddResponsavel, 5);

        grid.add(new Label("Quantidade de Cópias:"), 0, 6); 
        grid.add(spinnerQuantidade, 1, 6);

        HBox boxBotoesAcao = new HBox(10, btnGerar, btnVisualizar, btnImprimirDireto);
        boxBotoesAcao.setAlignment(Pos.CENTER);
        boxBotoesAcao.setPadding(new Insets(15, 0, 0, 0));

        VBox root = new VBox(10);
        root.getChildren().addAll(grid, boxBotoesAcao);
        root.setPadding(new Insets(10, 20, 20, 20));
        root.setAlignment(Pos.CENTER);

        // Aumentamos levemente a largura da janela para acomodar confortavelmente os ícones textuais
        this.scene = new Scene(root, 580, 440);

        // Carregamento do arquivo CSS Externo
        try {
            String css = getClass().getResource("/estilo.css").toExternalForm();
            this.scene.getStylesheets().add(css);
        } catch (Exception e) {
            try {
                String cssFallback = getClass().getResource("estilo.css").toExternalForm();
                this.scene.getStylesheets().add(cssFallback);
            } catch (Exception ex) {
                System.err.println("Aviso: Arquivo estilo.css não pôde ser carregado.");
            }
        }
    }

    public void exibir(Stage stage) {
        stage.setTitle("Controle de Validade - MVC + CSS + Ícones");
        stage.setScene(this.scene);
        stage.setResizable(false);
        stage.show();
    }

    private void adicionarLinhaGrade(GridPane grid, String textoLabel, ComboBox<String> combo, Button btn, int línea) {
        grid.add(new Label(textoLabel), 0, línea);
        grid.add(new HBox(5, combo, btn), 1, línea);
    }

    private void configurarAlturasCampos() {
        comboProduto.setMinHeight(30); comboCategoria.setMinHeight(30); comboEstocagem.setMinHeight(30);
        comboResponsavel.setMinHeight(30); txtDiasValidade.setMinHeight(30); pickerDataManipulacao.setMinHeight(30);
    }

    private void configurarFormatadorData() {
        pickerDataManipulacao.setConverter(new StringConverter<LocalDate>() {
            @Override public String toString(LocalDate date) { return date != null ? formatador.format(date) : ""; }
            @Override public LocalDate fromString(String string) { return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatador) : null; }
        });
    }

    public void exibirAlerta(Alert.AlertType tipo, String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }
}