package com.geradoretiquetavalidade;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Etiqueta {
    // Atributos privados (Encapsulamento)
    private String produto;
    private String categoria;
    private LocalDate dataManipulacao;
    private int diasValidade;
    private String estocagem;
    private String responsavel;

    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Construtor da classe
    public Etiqueta(String produto, String categoria, LocalDate dataManipulacao, int diasValidade, String estocagem, String responsavel) {
        this.produto = produto;
        this.categoria = categoria;
        this.dataManipulacao = dataManipulacao;
        this.diasValidade = diasValidade;
        this.estocagem = estocagem;
        this.responsavel = responsavel;
    }

    // Regra de Negócio: Calcula a data de validade automaticamente
    public LocalDate calcularDataValidade() {
        return this.dataManipulacao.plusDays(this.diasValidade);
    }

    // Método responsável por gerar seu próprio PDF
    public void exportarParaPdf(String nomeArquivo, int quantidade) throws DocumentException, IOException {
        Rectangle tamanhoEtiqueta = new Rectangle(226, 141); 
        Document documento = new Document(tamanhoEtiqueta, 0, 0, 0, 0);
        PdfWriter.getInstance(documento, new FileOutputStream(nomeArquivo));
        
        documento.open();

        Font fonteTitulo = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        Font fonteLabel = new Font(Font.HELVETICA, 7, Font.BOLD, Color.DARK_GRAY);
        Font fonteValor = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.BLACK);
        Font fonteValidadeDestaque = new Font(Font.HELVETICA, 8, Font.BOLD, Color.RED);

        String manipStr = this.dataManipulacao.format(FORMATADOR);
        String valStr = this.calcularDataValidade().format(FORMATADOR);

        for (int i = 0; i < quantidade; i++) {
            if (i > 0) {
                documento.newPage();
            }

            PdfPTable tabela = new PdfPTable(1);
            tabela.setWidthPercentage(100);

            PdfPCell celulaTitulo = new PdfPCell(new Paragraph("CONTROLE DE VALIDADE", fonteTitulo));
            celulaTitulo.setBackgroundColor(Color.BLACK);
            celulaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
            celulaTitulo.setPadding(3);
            tabela.addCell(celulaTitulo);

            tabela.addCell(criarCelulaCampo("PRODUTO:", this.produto, fonteLabel, fonteValor));
            tabela.addCell(criarCelulaCampo("CATEGORIA:", this.categoria, fonteLabel, fonteValor));
            
            // Tabela interna para as datas
            PdfPTable tabelaDatas = new PdfPTable(2);
            tabelaDatas.setWidthPercentage(100);
            
            PdfPCell celulaManip = criarCelulaCampo("MANIPULAÇÃO:", manipStr, fonteLabel, fonteValor);
            celulaManip.setBorder(Rectangle.NO_BORDER);
            tabelaDatas.addCell(celulaManip);
            
            PdfPCell celulaVal = criarCelulaCampo("VALIDADE:", valStr, fonteLabel, fonteValidadeDestaque);
            celulaVal.setBorder(Rectangle.NO_BORDER);
            tabelaDatas.addCell(celulaVal);
            
            PdfPCell celulaLinhaDatas = new PdfPCell(tabelaDatas);
            celulaLinhaDatas.setPadding(0);
            celulaLinhaDatas.setBorderColor(Color.LIGHT_GRAY);
            tabela.addCell(celulaLinhaDatas);

            tabela.addCell(criarCelulaCampo("ESTOCAGEM:", this.estocagem, fonteLabel, fonteValor));
            tabela.addCell(criarCelulaCampo("RESPONSÁVEL:", this.responsavel, fonteLabel, fonteValor));

            documento.add(tabela);
        }

        documento.close();
    }

    private PdfPCell criarCelulaCampo(String label, String valor, Font fonteLabel, Font fonteValor) {
        Paragraph p = new Paragraph();
        p.add(new Paragraph(label + " ", fonteLabel));
        p.add(new Paragraph(valor, fonteValor));
        
        PdfPCell celula = new PdfPCell(p);
        celula.setPaddingLeft(6);
        celula.setPaddingTop(2);    
        celula.setPaddingBottom(3); 
        celula.setBorderColor(Color.LIGHT_GRAY);
        return celula;
    }

    // Getters e Setters (Caso precise acessar ou modificar os dados depois)
    public String getProduto() { return produto; }
    public void setProduto(String produto) { this.produto = produto; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public LocalDate getDataManipulacao() { return dataManipulacao; }
    public void setDataManipulacao(LocalDate dataManipulacao) { this.dataManipulacao = dataManipulacao; }
    public int getDiasValidade() { return diasValidade; }
    public void setDiasValidade(int diasValidade) { this.diasValidade = diasValidade; }
    public String getEstocagem() { return estocagem; }
    public void setEstocagem(String estocagem) { this.estocagem = estocagem; }
    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
}