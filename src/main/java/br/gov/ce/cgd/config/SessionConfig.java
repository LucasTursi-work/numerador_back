package br.gov.ce.cgd.config;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;
import java.util.EnumSet;
import java.util.Set;

@Configuration
public class SessionConfig {
    
    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            // Desativa todos os modos de tracking de sessão
            Set<SessionTrackingMode> sessionTrackingModes = EnumSet.noneOf(SessionTrackingMode.class);
            servletContext.setSessionTrackingModes(sessionTrackingModes);
            
            // Configura cookie de sessão para expirar imediatamente
            servletContext.getSessionCookieConfig().setMaxAge(0);
            servletContext.getSessionCookieConfig().setHttpOnly(true);
            servletContext.getSessionCookieConfig().setSecure(false);
            servletContext.getSessionCookieConfig().setPath("/numerador");
            
            // Timeout zero
            servletContext.setSessionTimeout(0);
        };
    }
}