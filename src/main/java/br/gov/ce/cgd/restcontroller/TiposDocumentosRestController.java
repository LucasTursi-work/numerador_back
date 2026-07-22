package br.gov.ce.cgd.restcontroller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import br.gov.ce.cgd.entity.TipoDocumento;
import br.gov.ce.cgd.service.TipoDocumentoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/tipos-documentos")
@Tag(name = "TiposDocumentos", description = "Operações relacionadas aos tipos de documentos")
public class TiposDocumentosRestController {

    private final TipoDocumentoService tipoDocumentoService;

    public TiposDocumentosRestController(TipoDocumentoService tipoDocumentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
    }

    // ========================= LISTAR COM PAGINAÇÃO / FILTRO =========================

    @Operation(summary = "Listar tipos de documentos", description = "Retorna uma página de tipos de documentos com opção de filtro por nome.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public Page<TipoDocumento> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());

        if (nome != null && !nome.isEmpty()) {
            return tipoDocumentoService.listarPorNome(nome, pageable);
        }
        return tipoDocumentoService.listarTodos(pageable);
    }

    // ========================= LISTAR APENAS ATIVOS =========================

    @Operation(summary = "Listar tipos de documentos ativos", description = "Retorna todos os tipos de documentos ativos.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping("/ativos")
    public Iterable<TipoDocumento> listarAtivos() {
        return tipoDocumentoService.listarAtivos();
    }

    // ========================= CRIAR =========================

    @Operation(summary = "Criar tipo de documento", description = "Cria um novo tipo de documento.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public TipoDocumento criar(@RequestBody TipoDocumento tipoDocumento) {
        return tipoDocumentoService.salvar(tipoDocumento);
    }

    // ========================= EDITAR =========================

    @Operation(summary = "Editar tipo de documento", description = "Atualiza um tipo de documento existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Tipo de documento não encontrado")
    })
    @PutMapping("/{id}")
    public TipoDocumento editar(@PathVariable Long id, @RequestBody TipoDocumento tipoDocumento) {
        return tipoDocumentoService.atualizar(id, tipoDocumento);
    }

    // ========================= ALTERAR STATUS =========================

    @Operation(summary = "Alterar status", description = "Alterna entre ativo e inativo.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status atualizado"),
        @ApiResponse(responseCode = "404", description = "Tipo de documento não encontrado")
    })
    @PatchMapping("/{id}/status")
    public TipoDocumento alternarStatus(@PathVariable Long id) {
        TipoDocumento tipoDocumento = tipoDocumentoService.buscarPorId(id);
        tipoDocumento.setTipoDocumentoAtivo(!tipoDocumento.isTipoDocumentoAtivo());
        return tipoDocumentoService.atualizar(id, tipoDocumento);
    }

    // ========================= DELETAR =========================

    @Operation(summary = "Excluir", description = "Remove um tipo de documento pelo ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Tipo de documento não encontrado")
    })
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        tipoDocumentoService.deletar(id);
    }
}
