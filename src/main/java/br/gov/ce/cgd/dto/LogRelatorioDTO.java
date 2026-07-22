package br.gov.ce.cgd.dto;

public class LogRelatorioDTO {
    private String usuario;
    private String acao;

    public LogRelatorioDTO() {}

    public LogRelatorioDTO(String usuario, String acao) {
        this.usuario = usuario;
        this.acao = acao;
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }
}
