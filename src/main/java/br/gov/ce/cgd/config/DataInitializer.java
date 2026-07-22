package br.gov.ce.cgd.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.gov.ce.cgd.entity.Usuario;
import br.gov.ce.cgd.enumerate.Perfil;
import br.gov.ce.cgd.enumerate.Status;
import br.gov.ce.cgd.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDefaultAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // CPF e email fixos do admin padrão
            String cpfAdmin = "12345678909";
            String emailAdmin = "admin@admin";

            // Verifica se já existe
            if (!usuarioRepository.existsByCpf(cpfAdmin) && !usuarioRepository.existsByEmail(emailAdmin)) {
                Usuario admin = Usuario.builder()
                        .nome("Administrador do Sistema")
                        .cpf(cpfAdmin)
                        .email(emailAdmin)
                        .senha(passwordEncoder.encode("admin"))
                        .perfil(Perfil.ADMIN)
                        .status(Status.ATIVO)
                        .tentativasLogin(0)
                        .senhaProvisoria(false)
                        .build();

                usuarioRepository.save(admin);
//                System.out.println("✅ Usuário padrão criado");
//                System.out.println("✅ Usuário: 12345678909");
//                System.out.println("✅ Senha: admin");
            } else {
//                System.out.println("ℹ️ Usuário admin já existe — não foi recriado.");
            }
        };
    }
}
