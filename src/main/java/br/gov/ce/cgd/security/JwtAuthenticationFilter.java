package br.gov.ce.cgd.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import br.gov.ce.cgd.entity.Usuario;
import br.gov.ce.cgd.service.impl.UsuarioServiceImpl;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioServiceImpl usuarioService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UsuarioServiceImpl usuarioService) {
        this.jwtUtil = jwtUtil;
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Permitir endpoints públicos sem autenticação
        if (isPublicEndpoint(path) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        
        String token = resolverToken(request);
        
        if (token == null) {
            sendUnauthorizedError(response, "Token não encontrado");
            return;
        }
        
        try {
            String cpf = jwtUtil.extrairUsername(token);
            
            if (cpf != null && jwtUtil.validarToken(token, cpf)) {
                Usuario usuario = usuarioService.buscarPorCpf(cpf).orElse(null);
                
                if (usuario != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    sendUnauthorizedError(response, "Usuário não encontrado");
                    return;
                }
            } else {
                sendUnauthorizedError(response, "Token inválido ou expirado");
                return;
            }
        } catch (Exception e) {
            sendUnauthorizedError(response, "Erro na autenticação: " + e.getMessage());
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.contains("/api/login") || 
               path.contains("/v3/api-docs") || 
               path.contains("/swagger-ui") ||
               path.contains("/swagger-ui.html");
    }
    
    private String resolverToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
    
    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"Não autorizado\", \"message\": \"" + message + "\"}");
    }
}