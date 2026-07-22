package br.gov.ce.cgd.dto;

import lombok.Data;
import java.time.LocalDateTime;
import br.gov.ce.cgd.enumerate.Status;

@Data
public class DocumentoDTO {
    private Long id;
    private Integer numero;
    private LocalDateTime dataCadastro;
    private Status status;

    private Long usuarioId;
    private String nomeUsuario;

    private Long tipoDocumentoId;
    private String nomeTipoDocumento;

    private Long cartorioId;
    private String nomeCartorio;
}


