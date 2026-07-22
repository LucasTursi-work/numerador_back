package br.gov.ce.cgd.dto.usuario;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.br.CPF;

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
public class UsuarioCreateDTO {

    @NotBlank
    private String nome;

    @NotBlank
    @CPF(message = "CPF inválido! <br> Informe de CPF válido")
    private String cpf;

    @NotBlank
    @Email
    private String email;

    private String senha;

    private Perfil perfil;

	private Status status;
}