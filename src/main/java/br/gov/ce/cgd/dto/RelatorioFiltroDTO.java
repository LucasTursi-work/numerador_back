package br.gov.ce.cgd.dto;

import java.time.LocalDate; // Nota: Se o seu Java 8 for muito antigo e não aceitar LocalDate, use java.util.Date
import java.util.List;

public class RelatorioFiltroDTO {
    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private List<Long> tiposDocumentoIds;
    private List<Long> cartoriosIds;
    private List<Long> usuariosIds;
    private String imagemGraficoDocsBase64;
    private String imagemGraficoCartoriosBase64;

 
    public RelatorioFiltroDTO() {}

    // Getters e Setters
    public LocalDate getDataInicial() { return dataInicial; }
    public void setDataInicial(LocalDate dataInicial) { this.dataInicial = dataInicial; }

    public LocalDate getDataFinal() { return dataFinal; }
    public void setDataFinal(LocalDate dataFinal) { this.dataFinal = dataFinal; }

    public List<Long> getTiposDocumentoIds() { return tiposDocumentoIds; }
    public void setTiposDocumentoIds(List<Long> tiposDocumentoIds) { this.tiposDocumentoIds = tiposDocumentoIds; }

    public List<Long> getCartoriosIds() { return cartoriosIds; }
    public void setCartoriosIds(List<Long> cartoriosIds) { this.cartoriosIds = cartoriosIds; }

    public List<Long> getUsuariosIds() { return usuariosIds; }
    public void setUsuariosIds(List<Long> usuariosIds) { this.usuariosIds = usuariosIds; }

    public String getImagemGraficoDocsBase64() { return imagemGraficoDocsBase64; }
    public void setImagemGraficoDocsBase64(String imagemGraficoDocsBase64) { this.imagemGraficoDocsBase64 = imagemGraficoDocsBase64; }

    public String getImagemGraficoCartoriosBase64() { return imagemGraficoCartoriosBase64; }
    public void setImagemGraficoCartoriosBase64(String imagemGraficoCartoriosBase64) { this.imagemGraficoCartoriosBase64 = imagemGraficoCartoriosBase64; }
}
