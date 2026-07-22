package br.gov.ce.cgd.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.Map;

@Component
public class JwtUtil {

    // Chave fixa segura (não mudar em tempo de execução)
    private static final String SECRET = "A1b2C3d4E5f6G7h8I9j0K1l2M3n4O5p6";

    private final Key chaveSecreta = Keys.hmacShaKeyFor(SECRET.getBytes());

    private final long validadeToken = 1000 * 60 * 30; // 30 minutos

    // Criação do token
public String gerarToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        
        // Converte a coleção de GrantedAuthority em uma lista de Strings ("ROLE_ADMIN", "ROLE_USER")
        claims.put("roles", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims) // 👈 Injeta o mapa contendo as roles do usuário
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validadeToken))
                .signWith(chaveSecreta, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extrair o login (CPF)
    public String extrairUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(chaveSecreta)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validação completa
    public boolean validarToken(String token, String username) {
        try {
            String subject = extrairUsername(token);

            return (subject.equals(username) &&
                    !isTokenExpirado(token));

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpirado(String token) {
        Date expiracao = Jwts.parserBuilder()
                .setSigningKey(chaveSecreta)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expiracao.before(new Date());
    }
}
