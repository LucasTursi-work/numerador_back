package br.gov.ce.cgd.restcontroller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import br.gov.ce.cgd.entity.Cartorio;
import br.gov.ce.cgd.service.CartorioService;
import br.gov.ce.cgd.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/cartorios")
@Tag(name = "Cartórios", description = "Operações de consulta e manutenção de cartórios")
public class CartorioRestController {

    private final CartorioService cartorioService;
    private final UsuarioService usuarioService;

    public CartorioRestController(CartorioService cartorioService, UsuarioService usuarioService) {
        this.cartorioService = cartorioService;
        this.usuarioService = usuarioService;
    }

    // ========================= LISTAR CARTÓRIOS (com filtro) =========================
    @Operation(
        summary = "Listar cartórios",
        description = "Lista cartórios com paginação e opção de filtro por nome."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public Page<Cartorio> listarCartorios(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        if (nome != null && !nome.isEmpty()) {
            return cartorioService.listarPorNome(nome, pageable);
        } else {
            return cartorioService.listarTodos(pageable);
        }
    }

    // ========================= CRIAR =========================
    @Operation(
        summary = "Criar cartório",
        description = "Cria um novo cartório."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cartório criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public Cartorio criarCartorio(@RequestBody Cartorio cartorio) {
        return cartorioService.salvar(cartorio);
    }

    // ========================= EDITAR =========================
    @Operation(
        summary = "Editar cartório",
        description = "Atualiza um cartório existente pelo ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cartório não encontrado")
    })
    @PutMapping("/{id}")
    public Cartorio editarCartorio(@PathVariable Long id, @RequestBody Cartorio cartorio) {
        return cartorioService.atualizar(id, cartorio);
    }

    // ========================= ALTERAR STATUS =========================
    @Operation(
        summary = "Alterar status",
        description = "Alterna entre ativo e inativo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cartório não encontrado")
    })
    @PatchMapping("/{id}/status")
    public Cartorio alternarStatus(@PathVariable Long id) {
        Cartorio cartorio = cartorioService.buscarPorId(id);
        cartorio.setCartorioAtivo(!cartorio.isCartorioAtivo());
        return cartorioService.atualizar(id, cartorio);
    }

    // ========================= DELETAR =========================
    @Operation(
        summary = "Excluir cartório",
        description = "Remove um cartório pelo ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cartório não encontrado")
    })
    @DeleteMapping("/{id}")
    public void deletarCartorio(@PathVariable Long id) {
        cartorioService.deletar(id);
    }

    // ========================= RETORNAR CARTÓRIO POR ID =========================
    @Operation(
        summary = "Buscar por ID",
        description = "Retorna um cartório pelo ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Encontrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cartório não encontrado")
    })
    @GetMapping("/{id}")
    public Cartorio buscarPorId(@PathVariable Long id) {
        return cartorioService.buscarPorId(id);
    }

//    // ========================= GERAR NUMERAÇÃO =========================
//    @Operation(
//        summary = "Gerar numeração vinculada ao cartório",
//        description = "Gera uma nova numeração usando o tipo de documento e o ID do cartório."
//    )
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "Numeração gerada"),
//        @ApiResponse(responseCode = "400", description = "Erro ao gerar")
//    })
//    @PostMapping("/gerar-com-cartorio")
//    public String gerarNumeracaoComCartorio(
//            @RequestParam Long tipoDocumentoId,
//            @RequestParam Long cartorioId,
//            Principal principal
//    ) {
//        // documentoService.gerarNovaNumeracaoComCartorio(tipoDocumentoId, cartorioId, principal.getName());
//        return "Gerado com sucesso";
//    }
}
