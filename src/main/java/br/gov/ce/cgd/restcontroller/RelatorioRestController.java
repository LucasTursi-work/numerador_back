package br.gov.ce.cgd.restcontroller;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.gov.ce.cgd.service.RelatorioService;
import br.gov.ce.cgd.dto.RelatorioFiltroDTO;
import br.gov.ce.cgd.dto.RelatorioResultadoDTO;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioRestController {
    private final RelatorioService relatorioService;

    public RelatorioRestController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @PostMapping("/dados")
    public ResponseEntity<RelatorioResultadoDTO> obterDadosRelatorio(@RequestBody RelatorioFiltroDTO filtros) {
        RelatorioResultadoDTO dados = relatorioService.compilarDados(filtros);
        return ResponseEntity.ok(dados);
    }

    @PostMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(@RequestBody RelatorioFiltroDTO filtros) {
        byte[] pdfBytes = relatorioService.gerarPdf(filtros);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/exportar/csv")
    public ResponseEntity<byte[]> exportarCsv(@RequestBody RelatorioFiltroDTO filtros) {
        byte[] csvBytes = relatorioService.gerarCsv(filtros);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }
}
