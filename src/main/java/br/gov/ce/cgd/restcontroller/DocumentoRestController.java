package br.gov.ce.cgd.restcontroller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.gov.ce.cgd.dto.DocumentoDTO;
import br.gov.ce.cgd.entity.Documento;
import br.gov.ce.cgd.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/documentos")
@Tag(name = "Documentos", description = "Gerenciamento de documentos e geração de numeração")
public class DocumentoRestController {

    private final DocumentoService documentoService;

    public DocumentoRestController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @Operation(summary = "Gerar numeração de documento", description = "Gera uma nova numeração para um documento com base no tipo e cartório informados.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documento gerado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou incompletos")
    })
    @PostMapping("/gerar")
    public ResponseEntity<Void> gerarDocumento(@RequestBody DocumentoDTO request, Principal principal) {
        try {
            if (principal == null) {
                System.out.println("Principal está null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            if (request.getTipoDocumentoId() == null || request.getCartorioId() == null) {
                System.out.println("Campos obrigatórios nulos");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            documentoService.gerarNovaNumeracao(
                request.getTipoDocumentoId(),
                request.getCartorioId(),
                principal.getName()
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Buscar últimos documentos", description = "Retorna os últimos documentos gerados.")
    @GetMapping("/ultimos")
    public ResponseEntity<List<DocumentoDTO>> getUltimosDocumentos() {
        return ResponseEntity.ok(documentoService.buscarUltimosDocumentosDTOs());
    }

    @Operation(summary = "Listar todos os documentos", description = "Retorna todos os documentos cadastrados.")
    @GetMapping
    public ResponseEntity<List<DocumentoDTO>> listarTodos() {
        List<DocumentoDTO> documentos = documentoService.listarTodosDTO();
        return ResponseEntity.ok(documentos);
    }


    @Operation(summary = "Buscar documento por ID", description = "Retorna o documento correspondente ao ID informado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documento encontrado"),
        @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Documento> buscarPorId(
            @Parameter(description = "ID do documento") @PathVariable Long id) {
        return documentoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Criar novo documento", description = "Cadastra um novo documento.")
    @PostMapping
    public ResponseEntity<Documento> criarDocumento(@RequestBody Documento documento) {
        Documento criado = documentoService.salvar(documento);
        return ResponseEntity.ok(criado);
    }

    @Operation(summary = "Atualizar documento", description = "Atualiza os dados do documento pelo ID informado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documento atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Documento> atualizarDocumento(
            @PathVariable Long id, @RequestBody Documento documento) {
        return documentoService.atualizar(id, documento)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Excluir documento", description = "Remove o documento correspondente ao ID informado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documento excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarDocumento(@PathVariable Long id) {
        boolean deletado = documentoService.deletar(id);
        return deletado ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
