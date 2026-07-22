package br.gov.ce.cgd.dto.usuario;

import java.time.LocalDateTime;

import javax.validation.constraints.Email;

import br.gov.ce.cgd.enumerate.Perfil;
import br.gov.ce.cgd.enumerate.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {

    private Long id;

    private String nome;

    @Email
    private String email;

    public String senha;

    private Perfil perfil;

    private Status status;
    
    private LocalDateTime dataAtualizacao;

}