package br.gov.ce.cgd.controller;

import java.security.Principal;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.gov.ce.cgd.entity.Cartorio;
import br.gov.ce.cgd.entity.TipoDocumento;
import br.gov.ce.cgd.entity.Usuario;
import br.gov.ce.cgd.service.CartorioService;
import br.gov.ce.cgd.service.DocumentoService;
import br.gov.ce.cgd.service.TipoDocumentoService;
import br.gov.ce.cgd.service.UsuarioService;
import br.gov.ce.cgd.util.Chart;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "Dashboard", description = "Endpoints relacionados ao dashboard da aplicação")
public class DashboardController {

    private final TipoDocumentoService tipoDocumentoService;
    private final DocumentoService documentoService;
    private final CartorioService cartorioService;
    private final UsuarioService usuarioService;

    public DashboardController(
            TipoDocumentoService tipoDocumentoService,
            DocumentoService documentoService,
            CartorioService cartorioService,
            UsuarioService usuarioService) {

        this.tipoDocumentoService = tipoDocumentoService;
        this.documentoService = documentoService;
        this.cartorioService = cartorioService;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Exibe o dashboard com dados, gráficos e lista paginada de usuários")
    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            Principal principal,
            @Parameter(description = "Número da página para paginação da aba usuários", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página para paginação da aba usuários", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        List<TipoDocumento> tipos = tipoDocumentoService.listarAtivos();

        Map<Long, Object> ultimasNumeracoes = tipos.stream()
                .collect(Collectors.toMap(
                        TipoDocumento::getId,
                        tipo -> documentoService.obterUltimaNumeracao(tipo.getId())
                ));

        List<Object[]> dadosGraficos = documentoService.contarDocumentosPorTipo();
        Map<String, List<?>> listasGraficos = Chart.separarNomesEQuantidades(dadosGraficos);

        List<Object[]> dadosCartorios = documentoService.contarDocumentosPorCartorio();
        Map<String, List<?>> listasGraficosCartorio = Chart.separarNomesEQuantidades(dadosCartorios);

        List<Cartorio> cartorios = cartorioService.listarAtivos();

        Usuario usuario = usuarioService.findByCpf(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o CPF: " + principal.getName()));

        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").ascending()); //Define tamanho da paginação
        Page<Usuario> usuarios = usuarioService.listarTodos(pageable);

        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarioPerfil", usuario.getPerfil().name());
        model.addAttribute("tiposDocumentos", tipos);
        model.addAttribute("ultimasNumeracoes", ultimasNumeracoes);
        model.addAttribute("anoAtual", Year.now().getValue());
        model.addAttribute("novoTipoDocumento", new TipoDocumento());
        model.addAttribute("novoUsuario", new Usuario());
        model.addAttribute("novoCartorio", new Cartorio());
        model.addAttribute("cartorios", cartorios);

        model.addAttribute("nomesGraficos", listasGraficos.get("nomes"));
        model.addAttribute("quantidadesGraficos", listasGraficos.get("quantidades"));

        model.addAttribute("nomesCartorios", listasGraficosCartorio.get("nomes"));
        model.addAttribute("quantidadesCartorios", listasGraficosCartorio.get("quantidades"));

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuarios.getTotalPages());

        return "dashboard";
    }
}
