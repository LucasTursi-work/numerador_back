package br.gov.ce.cgd.dto;

public class GraficoDataDTO {
    private String label;
    private Long quantidade;

    public GraficoDataDTO() {}

    public GraficoDataDTO(String label, Long quantidade) {
        this.label = label;
        this.quantidade = quantidade;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Long getQuantidade() { return quantidade; }
    public void setQuantidade(Long quantidade) { this.quantidade = quantidade; }
}
