package br.gov.ce.cgd.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import br.gov.ce.cgd.dto.usuario.UsuarioUpdateDTO;
import br.gov.ce.cgd.entity.Usuario;
import br.gov.ce.cgd.enumerate.Perfil;
import br.gov.ce.cgd.enumerate.Status;

public interface UsuarioService extends UserDetailsService {

    // CRUD básico
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorId(Long id);
    Optional<Usuario> buscarPorCpf(String cpf);
    Optional<Usuario> buscarPorEmail(String email);
    void deletarPorId(Long id);

    // Validações e existências
    boolean existePorCpf(String cpf);
    boolean existePorEmail(String email);

    // Atualizações
    void atualizarDataLogin(Long id);
    Usuario alterarStatus(Long id, Status status);
    Usuario atualizarUsuario(Long id, UsuarioUpdateDTO dto, String senhaAtual);

    // Filtragem
    List<Usuario> buscarPorNome(String nome);
    List<Usuario> buscarTodos();
    Page<Usuario> filtrarUsuarios(String nome, String cpf, Perfil perfil, Status status, Pageable pageable);

    // Senha
    void alterarSenha(Long id, String senhaAtual, String novaSenha, String confirmarNovaSenha);
    void senhaProvisoria(String email);

    // Sessão
    Usuario getUsuarioLogado();
    void alterarSenhaUsuarioLogado(String senhaAtual, String novaSenha, String confirmarSenha);
}
