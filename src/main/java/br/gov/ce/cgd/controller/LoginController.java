package br.gov.ce.cgd.controller;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.gov.ce.cgd.entity.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "Autenticação", description = "Tela de login do sistema")
public class LoginController {

    @GetMapping("/login")
    @Operation(summary = "Exibir tela de login", description = "Renderiza a view da página de login. Se o usuário já estiver autenticado, exibe as informações no modelo.")
    public String loginPage(@AuthenticationPrincipal Usuario usuarioLogado, HttpSession session, Model model) {
        if (usuarioLogado != null) {
            model.addAttribute("usuarioLogado", usuarioLogado);
        }

        Object errorMessage = session.getAttribute("errorMessage");
        if (errorMessage != null) {
            model.addAttribute("erro", errorMessage.toString());
            session.removeAttribute("errorMessage");
        }
        return "login";
    }
}
