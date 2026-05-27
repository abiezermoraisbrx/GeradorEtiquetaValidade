package com.geradoretiquetavalidade;

import javafx.application.Application;
import javafx.stage.Stage;

public class GeradorEtiquetaValidade extends Application {

    @Override
    public void start(Stage primaryStage) {
        
        // INICIALIZA O SISTEMA DE LOGS DO PROJETO
        LogConfig.configurarLog();
        
        FabricaConexao.inicializarBanco(); // <-- NOVA ATUALIZAÇÃO: Garante o banco SQL ativo
        // 1. Instancia a Janela/Layout (View)
        GeradorEtiquetaView view = new GeradorEtiquetaView();
        
        // 2. Instancia o Cérebro do sistema (Controller) passando a View para ele gerenciar
        new GeradorEtiquetaController(view);
        
        // 3. Exibe a interface gráfica na tela
        view.exibir(primaryStage);
    }

    public static void main(String[] args) {
        Application.launch(GeradorEtiquetaValidade.class, args);
    }
}