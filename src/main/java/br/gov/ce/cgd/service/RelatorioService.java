package br.gov.ce.cgd.service;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

import br.gov.ce.cgd.dto.GraficoDataDTO;
import br.gov.ce.cgd.dto.LogRelatorioDTO;
import br.gov.ce.cgd.dto.RelatorioFiltroDTO;
import br.gov.ce.cgd.dto.RelatorioResultadoDTO;
import br.gov.ce.cgd.entity.Documento;

@Service
public class RelatorioService {

    private final DocumentoService documentoService;

    // Injeção correta do DocumentoService
    public RelatorioService(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    /**
     * Compila os dados reais filtrados do banco para renderizar os gráficos e os logs na tela do Angular.
     */
    public RelatorioResultadoDTO compilarDados(RelatorioFiltroDTO filtros) {
        
        // Tratamento de segurança: Se a lista vier vazia [], vira null para o COALESCE do SQL funcionar
        List<Long> tipos = (filtros.getTiposDocumentoIds() != null && !filtros.getTiposDocumentoIds().isEmpty()) 
                            ? filtros.getTiposDocumentoIds() : null;
                            
        List<Long> cartorios = (filtros.getCartoriosIds() != null && !filtros.getCartoriosIds().isEmpty()) 
                            ? filtros.getCartoriosIds() : null;
                            
        List<Long> usuarios = (filtros.getUsuariosIds() != null && !filtros.getUsuariosIds().isEmpty()) 
                            ? filtros.getUsuariosIds() : null;

        // CORREÇÃO DE TIPO: Convertendo LocalDate para LocalDateTime para bater com a assinatura do Service/Repository
        java.time.LocalDateTime dataInicio = (filtros.getDataInicial() != null) 
                ? filtros.getDataInicial().atStartOfDay() : null;
                
        java.time.LocalDateTime dataFim = (filtros.getDataFinal() != null) 
                ? filtros.getDataFinal().atTime(23, 59, 59) : null;

        // Busca os dados agrupados diretamente do Banco de Dados utilizando os novos LocalDateTime criados acima
        List<Object[]> dadosTipo = documentoService.contarDocumentosPorTipoNoPeriodoMulti(
                dataInicio, dataFim, tipos, cartorios, usuarios);
                
        List<Object[]> dadosCartorio = documentoService.contarDocumentosPorCartorioNoPeriodoMulti(
                dataInicio, dataFim, tipos, cartorios, usuarios);

        // Transforma o Object[] do Banco de Dados em List<GraficoDataDTO> para o Chart.js
        List<GraficoDataDTO> graficoDocs = mapearParaGraficoDataDTO(dadosTipo);
        List<GraficoDataDTO> graficoCartorios = mapearParaGraficoDataDTO(dadosCartorio);

        // Busca as linhas de documentos individuais para alimentar a tabela inferior de logs
        List<Documento> documentosReais = documentoService.findByPeriodoMulti(
                dataInicio, dataFim, tipos, cartorios, usuarios);

        // Transforma os documentos em registros de log textuais
        List<LogRelatorioDTO> logs = documentosReais.stream().map(doc -> {
            String acao = String.format("Gerou o documento %s nº %d do cartório %s", 
                                        doc.getTipoDocumento().getNome(), 
                                        doc.getNumero(), 
                                        doc.getCartorio().getNome());
            return new LogRelatorioDTO(doc.getUsuario().getNome(), acao);
        }).collect(Collectors.toList());

        return new RelatorioResultadoDTO(graficoDocs, graficoCartorios, logs);
    }

    /**
     * Método auxiliar para converter o retorno do Banco de Dados (Object[]) para GraficoDataDTO
     */
    private List<GraficoDataDTO> mapearParaGraficoDataDTO(List<Object[]> dadosBrutos) {
        return dadosBrutos.stream().map(obj -> {
            String label = (String) obj[0];
            Long quantidade = (Long) obj[1];
            return new GraficoDataDTO(label, quantidade);
        }).collect(Collectors.toList());
    }

   public byte[] gerarPdf(RelatorioFiltroDTO filtros) {
    RelatorioResultadoDTO dados = this.compilarDados(filtros);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (Document document = new Document()) {
        PdfWriter.getInstance(document, baos);
        document.open();

        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
        document.add(new Paragraph("SISTEMA NUMERADOR - RELATÓRIO DE PRODUÇÃO", fontTitulo));
        document.add(new Paragraph("Emitido via módulo de auditoria analítica integrativa"));
        document.add(new Paragraph("------------------------------------------------------------------------------------------"));
        document.add(new Paragraph(" "));

        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        document.add(new Paragraph("Histórico de Registros Processados:", fontSubtitulo));
        document.add(new Paragraph(" "));

        if (dados.getLogs() == null || dados.getLogs().isEmpty()) {
            document.add(new Paragraph("Nenhum registro encontrado para a combinação de filtros aplicada."));
        } else {
            for (LogRelatorioDTO log : dados.getLogs()) {
                document.add(new Paragraph(log.getUsuario() + " -> " + log.getAcao()));
            }
        }
        // document.close() é chamado automaticamente pelo try-with-resources
    } catch (Exception e) {
        throw new RuntimeException("Erro ao gerar o PDF", e);
    }

    return baos.toByteArray();
}

    public byte[] gerarCsv(RelatorioFiltroDTO filtros) {
        StringBuilder csv = new StringBuilder();
        
        // CORREÇÃO ESSENCIAL: Insere o caractere universal BOM (Byte Order Mark) 
        // para garantir que acentos ("Cartório", "Ação") abram legíveis em qualquer versão do Excel/LibreOffice
        csv.append("\uFEFF"); 
        
        csv.append("Usuário;Ação\n");
        
        RelatorioResultadoDTO dados = this.compilarDados(filtros);
        for (LogRelatorioDTO log : dados.getLogs()) {
            csv.append(log.getUsuario())
               .append(";")
               .append(log.getAcao())
               .append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    
}