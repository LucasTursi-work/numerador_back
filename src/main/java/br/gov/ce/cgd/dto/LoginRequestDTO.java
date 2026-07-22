package br.gov.ce.cgd.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String cpf;
    private String senha;
}
