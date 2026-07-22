package br.gov.ce.cgd.restcontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.gov.ce.cgd.dto.LoginRequestDTO;
import br.gov.ce.cgd.entity.Usuario;
import br.gov.ce.cgd.security.JwtUtil;
import br.gov.ce.cgd.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/login")
@Tag(name = "Autenticação", description = "Endpoint para autenticação e geração de token JWT")
public class LoginRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;

    public LoginRestController(AuthenticationManager authenticationManager,
                               JwtUtil jwtUtil,
                               UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getCpf(), loginRequest.getSenha())
            );

            // Extrai o objeto UserDetails do Spring Security que o `UsuarioServiceImpl` montou
UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Busca o usuário para pegar os dados que antes iam para a Session
            Usuario usuario = usuarioService.buscarPorCpf(loginRequest.getCpf())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Atualiza a data de login (já existia no seu código)
            usuarioService.atualizarDataLogin(usuario.getId());

            String token = jwtUtil.gerarToken(userDetails.getUsername(), userDetails.getAuthorities());

            // Adicione os campos necessários para o Angular no mapa de resposta
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("usuarioId", usuario.getId());
            response.put("senhaProvisoria", usuario.getSenhaProvisoria());
            
            return ResponseEntity.ok(response);

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Usuário ou senha inválidos");
        }
    }
}
