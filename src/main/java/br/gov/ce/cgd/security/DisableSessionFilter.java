package br.gov.ce.cgd.security;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class DisableSessionFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 1. Invalida sessão existente
        HttpSession existingSession = httpRequest.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }
        
        // 2. Remove cookie JSESSIONID se existir
        if (httpRequest.getHeader("Cookie") != null && 
            httpRequest.getHeader("Cookie").contains("JSESSIONID")) {
            httpResponse.setHeader("Set-Cookie", 
                "JSESSIONID=; Path=/numerador; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; HttpOnly; SameSite=Strict");
        }
        
        // 3. Headers de segurança
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setHeader("Expires", "Sat, 01 Jan 2000 00:00:00 GMT");
        
        // 4. Wrapper que impede criação de sessão
        chain.doFilter(new HttpServletRequestWrapper(httpRequest) {
            @Override
            public HttpSession getSession() {
                return this.getSession(false);
            }
            
            @Override
            public HttpSession getSession(boolean create) {
                if (!create) {
                    return null;
                }
                // Se forçado a criar, cria e invalida imediatamente
                HttpSession session = httpRequest.getSession(create);
                if (session != null) {
                    session.invalidate();
                }
                return null;
            }
            
            @Override
            public String getRequestedSessionId() {
                return null;
            }
            
            @Override
            public boolean isRequestedSessionIdValid() {
                return false;
            }
            
            @Override
            public boolean isRequestedSessionIdFromCookie() {
                return false;
            }
            
            @Override
            public boolean isRequestedSessionIdFromURL() {
                return false;
            }
            
            @Override
            public boolean isRequestedSessionIdFromUrl() {
                return false;
            }
        }, response);
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    @Override
    public void destroy() {}
}