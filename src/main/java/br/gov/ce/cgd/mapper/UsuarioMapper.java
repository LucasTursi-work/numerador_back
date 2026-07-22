package br.gov.ce.cgd.mapper;

import br.gov.ce.cgd.dto.usuario.UsuarioCreateDTO;
import br.gov.ce.cgd.dto.usuario.UsuarioDTO;
import br.gov.ce.cgd.dto.usuario.UsuarioUpdateDTO;
import br.gov.ce.cgd.entity.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UsuarioMapper {

    public static UsuarioDTO toDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .cpf(usuario.getCpf())
                .email(usuario.getEmail())
                .perfil(usuario.getPerfil())
                .status(usuario.getStatus())
                .dataCadastro(usuario.getDataCadastro())
                .dataLogin(usuario.getDataLogin())
                .build();
    }

    public static Usuario fromCreateDTO(UsuarioCreateDTO dto) {
        return Usuario.builder()
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .email(dto.getEmail())
                .senha(dto.getSenha())
                .perfil(dto.getPerfil())
                .status(dto.getStatus())
                .build();
    }


    public static void updateEntityFromDTO(UsuarioUpdateDTO dto, Usuario usuario, PasswordEncoder encoder) {
        if (dto.getNome() != null) usuario.setNome(dto.getNome());
        if (dto.getEmail() != null) usuario.setEmail(dto.getEmail());
        if (dto.getPerfil() != null) usuario.setPerfil(dto.getPerfil());
        if (dto.getStatus() != null) usuario.setStatus(dto.getStatus());
        if (dto.getDataAtualizacao() != null) usuario.setDataAtualizacao(dto.getDataAtualizacao());
        if (dto.getSenha() != null && !dto.getSenha().trim().isEmpty()) {
            usuario.setSenha(encoder.encode(dto.getSenha()));
        }
    }
}
