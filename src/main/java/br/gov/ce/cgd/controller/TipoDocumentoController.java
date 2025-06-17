package br.gov.ce.cgd.controller;

import javax.persistence.EntityNotFoundException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.gov.ce.cgd.entity.TipoDocumento;
import br.gov.ce.cgd.service.TipoDocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping("/tipos-documentos")
@Tag(name = "Tipos de Documentos", description = "Operações relacionadas a tipos de documentos")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    public TipoDocumentoController(TipoDocumentoService tipoDocumentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de documentos ativos")
    public String listarTipos(Model model) {
        model.addAttribute("tiposDocumentos", tipoDocumentoService.listarAtivos());
        model.addAttribute("novoTipoDocumento", new TipoDocumento());
        return "tipos-documentos/listarTiposDocumentos"; 
    }
    
    @GetMapping({"/listar"})
    @Operation(summary = "Listar todos os tipos de documentos")
    public String listarTodos(Model model) {
        model.addAttribute("tipoDocumento", tipoDocumentoService.listarTodos());
        return "tipos-documentos/listarTiposDocumentos";
    }
    
    @GetMapping("/cadastrar")
    @Operation(summary = "Exibir formulário de cadastro de tipo de documento")
    public String cadastrar() {
        return "tipos-documentos/listarTiposDocumentos";
    }

    @PostMapping
    @Operation(summary = "Criar novo tipo de documento")
    public String criarTipo(@ModelAttribute TipoDocumento tipoDocumento) {
        tipoDocumento.setDocumentoAtivo(true);
        tipoDocumentoService.salvar(tipoDocumento);
        return "redirect:/dashboard";
    }

    @ResponseBody
    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar status (ativo/inativo) de um tipo de documento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Tipo de documento não encontrado")
    })
    public ResponseEntity<?> alterarStatus(@PathVariable Long id, @RequestParam boolean ativo) {
        boolean atualizado = tipoDocumentoService.alterarStatus(id, ativo);
        if (atualizado) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/editar")
    @Operation(summary = "Exibir formulário de edição de um tipo de documento")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model) {
        try {
            TipoDocumento tipoDocumento = tipoDocumentoService.buscarPorId(id);
            model.addAttribute("tipoDocumento", tipoDocumento);
            return "tipos-documentos/editarTipoDocumento";
        } catch (EntityNotFoundException e) {
            return "redirect:/tipos-documentos";
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de documento existente")
    public String atualizarTipoDocumento(@PathVariable Long id, @ModelAttribute TipoDocumento tipoDocumento) {
        tipoDocumentoService.atualizar(id, tipoDocumento);
        return "redirect:/tipos-documentos";
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir tipo de documento por ID")
    public String deletarTipoDocumento(@PathVariable Long id) {
        tipoDocumentoService.deletar(id);
        return "redirect:/tipos-documentos";
    }
}
