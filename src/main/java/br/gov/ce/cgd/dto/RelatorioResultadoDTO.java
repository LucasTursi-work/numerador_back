package br.gov.ce.cgd.dto;

import java.util.List;

public class RelatorioResultadoDTO {
    private List<GraficoDataDTO> graficoDocumentos;
    private List<GraficoDataDTO> graficoCartorios;
    private List<LogRelatorioDTO> logs;

    public RelatorioResultadoDTO() {}

    public RelatorioResultadoDTO(List<GraficoDataDTO> graficoDocumentos, List<GraficoDataDTO> graficoCartorios, List<LogRelatorioDTO> logs) {
        this.graficoDocumentos = graficoDocumentos;
        this.graficoCartorios = graficoCartorios;
        this.logs = logs;
    }

    public List<GraficoDataDTO> getGraficoDocumentos() { return graficoDocumentos; }
    public void setGraficoDocumentos(List<GraficoDataDTO> graficoDocumentos) { this.graficoDocumentos = graficoDocumentos; }

    public List<GraficoDataDTO> getGraficoCartorios() { return graficoCartorios; }
    public void setGraficoCartorios(List<GraficoDataDTO> graficoCartorios) { this.graficoCartorios = graficoCartorios; }

    public List<LogRelatorioDTO> getLogs() { return logs; }
    public void setLogs(List<LogRelatorioDTO> logs) { this.logs = logs; }
}