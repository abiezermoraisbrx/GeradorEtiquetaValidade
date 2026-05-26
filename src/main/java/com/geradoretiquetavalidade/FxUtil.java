package com.geradoretiquetavalidade;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class FxUtil {

    // Transforma qualquer ComboBox comum em um campo com busca/autocomplete
    public static void habilitarAutocomplete(ComboBox<String> comboBox) {
        comboBox.setEditable(true); // Permite que o usuário digite dentro do Combo
        
        // Guarda uma cópia da lista original de itens salvos no TXT
        ObservableList<String> itensOriginais = FXCollections.observableArrayList(comboBox.getItems());

        // Adiciona um ouvinte para capturar cada tecla digitada pelo usuário
        comboBox.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            // Ignora teclas de navegação para não atrapalhar a seleção por setas do teclado
            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN || 
                event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT || 
                event.getCode() == KeyCode.HOME || event.getCode() == KeyCode.END || 
                event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.ENTER) {
                return;
            }

            String textoDigitado = comboBox.getEditor().getText();

            if (textoDigitado == null || textoDigitado.isEmpty()) {
                // Se o usuário apagar tudo, mostra a lista completa original novamente
                comboBox.setItems(itensOriginais);
            } else {
                // Filtra os itens da lista que contêm o texto digitado (desconsiderando maiúsculas/minúsculas)
                ObservableList<String> itensFiltrados = FXCollections.observableArrayList();
                for (String item : itensOriginais) {
                    if (item.toLowerCase().contains(textoDigitado.toLowerCase())) {
                        itensFiltrados.add(item);
                    }
                }
                comboBox.setItems(itensFiltrados);
            }

            // Mantém o texto digitado visível e abre a caixinha de opções (dropdown) automaticamente
            comboBox.getEditor().setText(textoDigitado);
            comboBox.getEditor().end(); // Move o cursor para o final do texto
            if (!comboBox.getItems().isEmpty()) {
                comboBox.show();
            }
        });

        // Garante que, ao perder o foco, se o usuário digitou algo válido da lista, o valor seja fixado
        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                comboBox.getEditor().setText(novo);
            }
        });
    }
}