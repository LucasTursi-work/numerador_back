package br.gov.ce.cgd.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.cgd.dto.usuario.UsuarioUpdateDTO;
import br.gov.ce.cgd.entity.Usuario;
import br.gov.ce.cgd.enumerate.Perfil;
import br.gov.ce.cgd.enumerate.Status;
import br.gov.ce.cgd.repository.UsuarioRepository;
import br.gov.ce.cgd.service.EmailService;
import br.gov.ce.cgd.service.GoogleAuthenticatorService;
import br.gov.ce.cgd.service.UsuarioService;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthenticatorService googleAuthenticatorService;
    private final EmailService emailService;

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              GoogleAuthenticatorService googleAuthenticatorService,
                              EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.googleAuthenticatorService = googleAuthenticatorService;
        this.emailService = emailService;
    }

    // ----------------------- CRUD -----------------------

    @Override
    public Usuario salvar(Usuario usuario) {
        String senhaGerada;

        if (usuario.getSenha() != null && !usuario.getSenha().trim().isEmpty()) {
            senhaGerada = usuario.getSenha().trim();
            usuario.setSenhaProvisoria(false);
        } else {
            senhaGerada = gerarSenhaAleatoria(8);
            usuario.setSenhaProvisoria(true);
        }

        usuario.setSenha(passwordEncoder.encode(senhaGerada));

        if (usuario.getStatus() == null) {
            usuario.setStatus(Status.ATIVO);
        }
        
        if (usuario.getPerfil() == null) {
            usuario.setPerfil(Perfil.USUARIO);
        }

        Usuario salvo = usuarioRepository.save(usuario);

        String assunto = "Acesso ao Numerador";
        String mensagem = "Olá " + salvo.getNome() + ",\n\n"
                + "Seu usuário foi criado com sucesso!\n"
                + "CPF: " + salvo.getCpf() + "\n"
                + "Senha: " + senhaGerada + "\n\n"
                + "Acesse o endereço abaixo para utilizar o numerador \n"
                + "http://numerador.cgd.ce.gov.br:8090/numerador \n"
                + "Você deverá alterar no primeiro acesso.";

        emailService.enviarEmail(salvo.getEmail(), assunto, mensagem);

        return salvo;
    }
    
    @Override
    @Transactional
    public Usuario atualizarUsuario(Long id, UsuarioUpdateDTO dto, String senhaAtual) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Nome (se enviado e não vazio)
        if (dto.getNome() != null && !dto.getNome().trim().isEmpty()) {
            usuarioExistente.setNome(dto.getNome().trim());
        }

        // Email (se enviado e não vazio) - valida unicidade somente se trocou
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String novoEmail = dto.getEmail().trim();
            if (!novoEmail.equalsIgnoreCase(usuarioExistente.getEmail())) {
                Optional<Usuario> porEmail = usuarioRepository.findByEmail(novoEmail);
                if (porEmail.isPresent() && !porEmail.get().getId().equals(usuarioExistente.getId())) {
                    throw new RuntimeException("E-mail já cadastrado: " + novoEmail);
                }
                usuarioExistente.setEmail(novoEmail);
            }
        }

        // Senha (apenas se enviado e não vazio)
        if (dto.getSenha() != null && !dto.getSenha().trim().isEmpty()) {
            usuarioExistente.setSenha(passwordEncoder.encode(dto.getSenha().trim()));
            usuarioExistente.setSenhaProvisoria(false);
        }

        // Perfil / Status (podem ser null no dto)
        if (dto.getPerfil() != null) {
            usuarioExistente.setPerfil(dto.getPerfil());
        }
        if (dto.getStatus() != null) {
            usuarioExistente.setStatus(dto.getStatus());
        }

        usuarioExistente.setDataAtualizacao(LocalDateTime.now());

        try {
            return usuarioRepository.save(usuarioExistente);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            Throwable root = e.getRootCause();
            String msg = (root != null) ? root.getMessage() : e.getMessage();
            // Mapeamento simples de mensagens conhecidas
            if (msg != null && msg.toLowerCase().contains("unique") && msg.toLowerCase().contains("email")) {
                throw new RuntimeException("E-mail já cadastrado no sistema");
            }
            throw new RuntimeException("Erro ao atualizar usuário: " + (msg != null ? msg : e.getMessage()), e);
        }
    }

    @Override
    public void deletarPorId(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> buscarPorCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public List<Usuario> buscarPorNome(String nome) {
        return usuarioRepository.findByNomeContainingIgnoreCase(nome);
    }

    @Override
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public boolean existePorCpf(String cpf) {
        return usuarioRepository.existsByCpf(cpf);
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public Page<Usuario> filtrarUsuarios(String nome, String cpf, Perfil perfil, Status status, Pageable pageable) {
        return usuarioRepository.filtrarUsuarios(nome, cpf, perfil, status, pageable);
    }

    // ----------------------- Login / UserDetails -----------------------

    @Override
    public UserDetails loadUserByUsername(String cpf) throws UsernameNotFoundException {
        System.out.println(">>> Tentando carregar usuario pelo CPF: [" + cpf + "]");
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> {
                    System.out.println(">>> ERRO: CPF não encontrado no banco de dados!");
                    return new UsernameNotFoundException("Usuário não encontrado com o CPF: " + cpf);
                });
        System.out.println(">>> Usuário encontrado: " + usuario.getNome());
        System.out.println(">>> Status atual no banco: " + usuario.getPerfil() + " | " + usuario.getStatus());
        verificarStatus(usuario);
        System.out.println(">>> Passou pela verificação de status com sucesso.");
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (usuario.getPerfil() == Perfil.ADMIN) authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (usuario.getPerfil() == Perfil.USUARIO) authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        //teste do tipo de senha
        boolean match = passwordEncoder.matches("outroadmin".trim(), usuario.getSenha());
        System.out.println(">>> Teste manual de senha ('outroadmin'): " + match);
        System.out.println(">>> É senha provisória? " + usuario.getSenhaProvisoria());
        return User.builder()
                .username(usuario.getCpf())
                .password(usuario.getSenha())
                .authorities(authorities)
                .build();
    }

    private void verificarStatus(Usuario usuario) {
        if (usuario.getStatus() == Status.INATIVO) throw new DisabledException("Usuário inativo.");
        if (usuario.getStatus() == Status.BLOQUEADO) throw new DisabledException("Usuário bloqueado.");
        if (usuario.getStatus() == null) throw new LockedException("Entre em contato com o suporte.");
    }

    @Transactional
    public void registrarFalhaLogin(String cpf) {
        usuarioRepository.findByCpf(cpf).ifPresent(usuario -> {
            int tentativas = usuario.getTentativasLogin() + 1;
            usuario.setTentativasLogin(tentativas);
            if (tentativas >= 5) usuario.setStatus(Status.BLOQUEADO);
            usuarioRepository.save(usuario);
        });
    }

    public Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new RuntimeException("Usuário não autenticado");
        String cpf = auth.getName();
        return usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado no banco"));
    }

    // ----------------------- Senha -----------------------

    @Override
    public void alterarSenha(Long id, String senhaAtual, String novaSenha, String confirmarNovaSenha) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com o ID: " + id));

        // Valida senha atual apenas se não for provisória
        if (!Boolean.TRUE.equals(usuario.getSenhaProvisoria()) &&
            (senhaAtual == null || !passwordEncoder.matches(senhaAtual, usuario.getSenha()))) {
            throw new RuntimeException("Senha atual incorreta.");
        }

        // Confirmação da nova senha
        if (novaSenha == null || !novaSenha.equals(confirmarNovaSenha)) {
            throw new RuntimeException("As senhas não coincidem.");
        }

        // Atualiza senha e marca como não provisória
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setSenhaProvisoria(false);
        usuario.setDataAtualizacao(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }

    // Método auxiliar para o usuário logado (opcional)
    public void alterarSenhaUsuarioLogado(String senhaAtual, String novaSenha, String confirmarSenha) {
        alterarSenha(getUsuarioLogado().getId(), senhaAtual, novaSenha, confirmarSenha);
    }


    public void atualizarDataLogin(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setDataLogin(LocalDateTime.now());
            usuario.setTentativasLogin(0);
            usuarioRepository.save(usuario);
        });
    }

    public Usuario alterarStatus(Long id, Status status) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setStatus(status);
            return usuarioRepository.save(usuario);
        }).orElse(null);
    }

    public void resetarTentativas(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setTentativasLogin(0);
            usuarioRepository.save(usuario);
        });
    }

    // ----------------------- Senha Provisória -----------------------

    @Override
    public void senhaProvisoria(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String senhaProvisoria = gerarSenhaAleatoria(8);
        usuario.setSenha(passwordEncoder.encode(senhaProvisoria));
        usuario.setTentativasLogin(0);
        usuario.setSenhaProvisoria(true);
        if (usuario.getStatus().equals(Status.BLOQUEADO)){
        	usuario.setStatus(Status.ATIVO);
        }
        usuarioRepository.save(usuario);

        String assunto = "Sua senha provisória";
        String mensagem = "Olá " + usuario.getNome() + ",\n\n"
        		+ "Sua senha provisória é: " + senhaProvisoria + "\n"
                + "Você deverá alterá-la ao fazer login. \n"
                + "Acesse o endereço abaixo para utilizar o numerador \n" 
                + "http://numerador.cgd.ce.gov.br:8090/numerador";
        
        //teste de celeridade do servidor ao desabilitar temporariamente o e-mail automático.
        emailService.enviarEmail(usuario.getEmail(), assunto, mensagem);
    }

    private String gerarSenhaAleatoria(int tamanho) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder senha = new StringBuilder();
        for (int i = 0; i < tamanho; i++) {
            senha.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        return senha.toString();
    }

}
