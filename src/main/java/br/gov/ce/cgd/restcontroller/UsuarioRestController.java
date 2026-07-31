package br.gov.ce.cgd.restcontroller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.gov.ce.cgd.dto.usuario.UsuarioCreateDTO;
import br.gov.ce.cgd.dto.usuario.UsuarioDTO;
import br.gov.ce.cgd.dto.usuario.UsuarioUpdateDTO;
import br.gov.ce.cgd.entity.Usuario;
import br.gov.ce.cgd.enumerate.Perfil;
import br.gov.ce.cgd.enumerate.Status;
import br.gov.ce.cgd.mapper.UsuarioMapper;
import br.gov.ce.cgd.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "API para gerenciamento completo de usuários")
public class UsuarioRestController {
    @Autowired
    private PasswordEncoder encoder;
    private final UsuarioService usuarioService;
   

    public UsuarioRestController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
       
    }

@Operation(summary = "Criar novo usuário")
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public ResponseEntity<UsuarioDTO> criar(@Valid @RequestBody UsuarioCreateDTO dto) {
    Usuario usuario = UsuarioMapper.fromCreateDTO(dto, encoder);
    Usuario salvo = usuarioService.salvar(usuario);

    // Simplificamos: retornamos apenas o objeto e o status 201
    // O Angular já sabe para onde enviou os dados, não precisamos do Header 'Location' complexo agora
    return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioMapper.toDTO(salvo));
}

    @Operation(summary = "Listar todos os usuários")
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        List<UsuarioDTO> dtos = usuarioService.buscarTodos().stream()
                .map(UsuarioMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Buscar usuário por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(u -> ResponseEntity.ok(UsuarioMapper.toDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar usuário por CPF")
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<UsuarioDTO> buscarPorCpf(@PathVariable String cpf) {
        return usuarioService.buscarPorCpf(cpf)
                .map(u -> ResponseEntity.ok(UsuarioMapper.toDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar usuário por email")
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioDTO> buscarPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
                .map(u -> ResponseEntity.ok(UsuarioMapper.toDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar usuários por nome")
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<UsuarioDTO>> buscarPorNome(@PathVariable String nome) {
        List<UsuarioDTO> dtos = usuarioService.buscarPorNome(nome).stream()
                .map(UsuarioMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @Operation(summary = "Listar usuários com filtros e paginação")
    @GetMapping("/filtrar")
    public ResponseEntity<Page<UsuarioDTO>> filtrarUsuarios(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) Perfil perfil,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<UsuarioDTO> result = usuarioService
            .filtrarUsuarios(nome, cpf, perfil, status, pageable)
            .map(UsuarioMapper::toDTO);

        return ResponseEntity.ok(result);
    }


    @Operation(summary = "Atualizar usuário completo")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable Long id,
                                                @Valid @RequestBody UsuarioUpdateDTO dto) {
        try {
            Usuario atualizado = usuarioService.atualizarUsuario(id, dto, null);
            return ResponseEntity.ok(UsuarioMapper.toDTO(atualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Atualizar usuário parcialmente")
    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizarParcialmente(@PathVariable Long id,
                                                            @RequestBody UsuarioUpdateDTO dto) {
        try {
            Usuario atualizado = usuarioService.atualizarUsuario(id, dto, null);
            return ResponseEntity.ok(UsuarioMapper.toDTO(atualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Alterar senha do usuário autenticado")
    @PostMapping("/alterar-senha")
    public ResponseEntity<?> alterarSenha(@RequestBody Map<String, String> body, Authentication authentication) {
        String senhaAtual = body.get("senhaAtual");
        String novaSenha = body.get("novaSenha");
        String confirmarSenha = body.get("confirmarSenha");
        System.out.println(">>> Autenticação no Controller: " + (authentication != null ? authentication.getName() : "NULL"));
        if (senhaAtual == null || novaSenha == null || confirmarSenha == null) {
            return ResponseEntity.badRequest().body("Todos os campos são obrigatórios.");
        }

        try {
            usuarioService.alterarSenhaUsuarioLogado(senhaAtual, novaSenha, confirmarSenha);
            return ResponseEntity.ok("Senha alterada com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @Operation(summary = "Deletar usuário")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletarPorId(id);
        return ResponseEntity.noContent().build();
    }
}
