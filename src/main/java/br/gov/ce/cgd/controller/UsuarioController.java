package br.gov.ce.cgd.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.gov.ce.cgd.entity.Usuario;
import br.gov.ce.cgd.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Controller
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Operações relacionadas a usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/adicionar")
    @Operation(summary = "Exibir formulário de cadastro")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/adicionar";
    }

    @PostMapping
    @Operation(summary = "Criar novo usuário")
    public String criarUsuario(@Valid @ModelAttribute Usuario usuario, Model model) {
        try {
            usuario.setDataCadastro(LocalDateTime.now());
            usuarioService.salvarUsuario(usuario);
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao salvar usuário: " + e.getMessage());
            return "usuarios/adicionar";
        }
    }

    @GetMapping
    @ResponseBody
    @Operation(summary = "Listar todos os usuários (fragmento de tela)")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        return "fragments/aba-usuarios :: abaUsuarios";
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID (tela)")
    public String buscarUsuarioPorId(@PathVariable Long id, Model model) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        usuario.ifPresent(u -> model.addAttribute("usuario", u));
        return usuario.isPresent() ? "usuarios/editar" : "redirect:/usuarios";
    }

    @GetMapping("/editar/{id}")
    @Operation(summary = "Exibir formulário de edição do usuário")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "usuarios/editar";
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/json")
    @ResponseBody
    @Operation(summary = "Buscar usuário por ID (JSON)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Usuario> buscarUsuarioJson(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(id);
        if (usuarioOpt.isPresent()) {
            return ResponseEntity.ok(usuarioOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @ResponseBody
    @Operation(summary = "Atualizar usuário (JSON)")
    public ResponseEntity<Void> editarUsuario(@PathVariable Long id, @ModelAttribute Usuario usuario, String novaSenha) {
        usuarioService.atualizarUsuario(id, usuario, novaSenha);
        return ResponseEntity.ok().build();
    }

//    @PutMapping("/{id}")
//    public String editarUsuario(@PathVariable Long id, @ModelAttribute("usuario") Usuario usuario, String novaSenha) {
//        usuarioService.atualizarUsuario(id, usuario, novaSenha);
//        return "redirect:/usuarios";
//    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário por ID")
    public String deletarUsuario(@PathVariable Long id) {
        usuarioService.deletarUsuario(id);
        return "redirect:/usuarios";
    }

    @GetMapping("/buscarPorNome")
    @Operation(summary = "Buscar usuários por nome")
    public String buscarPorNome(@RequestParam String nome, Pageable pageable, Model model) {
        Page<Usuario> usuarios = usuarioService.buscarPorNome(nome, pageable);
        model.addAttribute("usuarios", usuarios);
        return "usuarios/listar";
    }

    @GetMapping("/buscarPorCpf")
    @Operation(summary = "Buscar usuários por CPF")
    public String buscarPorCpf(@RequestParam String cpf, Pageable pageable, Model model) {
        Page<Usuario> usuarios = usuarioService.buscarPorCpf(cpf, pageable);
        model.addAttribute("usuarios", usuarios);
        return "usuarios/listar";
    }

    @GetMapping("/alterar-senha")
    @Operation(summary = "Exibir tela de alteração de senha")
    public String alterarSenha(HttpSession session, Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId != null) {
            Optional<Usuario> usuario = usuarioService.findById(usuarioId);
            if (usuario.isPresent()) {
                model.addAttribute("usuario", usuario.get());
                return "usuarios/alterar-senha";
            }
        }

        return "redirect:/usuarios";
    }

    @PostMapping("/alterar-senha")
    @Operation(summary = "Alterar senha do usuário autenticado")
    public String alterarSenha(@RequestParam String senhaAtual,
                               @RequestParam String novaSenha,
                               @RequestParam(name = "confirmarSenha") String confirmarNovaSenha,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuário não autenticado.");
            return "redirect:/login";
        }

        try {
            usuarioService.alterarSenha(usuarioId, senhaAtual, novaSenha, confirmarNovaSenha);
            redirectAttributes.addFlashAttribute("successMessage", "Senha alterada com sucesso.");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("incorreta")) {
                redirectAttributes.addFlashAttribute("errorMessage", "A senha atual está incorreta.");
            } else if (e.getMessage().contains("não coincidem")) {
                redirectAttributes.addFlashAttribute("errorMessage", "A nova senha e a confirmação não coincidem.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Erro ao alterar a senha.");
            }
        }

        return "redirect:/dashboard";
    }

    /** NÃO APAGAR POIS PODEREI USAR
	  @PostMapping("/{id}/enable2fa")
	  public String enable2FA(@PathVariable Long id, Model model) {
	      String qrCodeUrl = usuarioService.enable2FA(id);
	      model.addAttribute("qrCodeUrl", qrCodeUrl);
	      model.addAttribute("successMessage", "QR Code gerado e enviado para o e-mail!");
	      return "redirect:/usuarios";  
	  }
	
	  @GetMapping(value = "/{id}/qrcode", produces = "image/png")
	  public ResponseEntity<byte[]> getQRCode(@PathVariable Long id) throws Exception {
	      byte[] qrCodeImage = usuarioService.generateQRCode(id);
	      return ResponseEntity.ok().body(qrCodeImage);
	  }
	
	  @PostMapping("/{id}/validar")
	  public String validate2FA(@PathVariable Long id, @RequestParam int code, Model model) {
	      boolean isValid = usuarioService.validate2FA(id, code);
	      model.addAttribute("isValid", isValid);
	      return isValid ? "redirect:/solicitar" : "usuarios/login";
	  }
*/

}
