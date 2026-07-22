package br.gov.ce.cgd.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.gov.ce.cgd.dto.DocumentoDTO;
import br.gov.ce.cgd.entity.Cartorio;
import br.gov.ce.cgd.entity.Documento;
import br.gov.ce.cgd.entity.TipoDocumento;
import br.gov.ce.cgd.entity.Usuario;
import br.gov.ce.cgd.repository.CartorioRepository;
import br.gov.ce.cgd.repository.DocumentoRepository;
import br.gov.ce.cgd.repository.TipoDocumentoRepository;
import br.gov.ce.cgd.repository.UsuarioRepository;

@Service
public class DocumentoService {

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CartorioRepository cartorioRepository;

    public List<Object[]> contarDocumentosPorTipo() {
        return documentoRepository.contarDocumentosPorTipo();
    }
    
    public List<Object[]> contarDocumentosPorCartorio() {
        return documentoRepository.contarTotalDocumentosPorCartorio();
    }

    @Transactional
    public Documento gerarNovaNumeracao(Long tipoDocumentoId, Long cartorioId, String username) {
        LocalDateTime agora = LocalDateTime.now();
        int anoAtual = agora.getYear();

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(tipoDocumentoId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de documento não encontrado com ID: " + tipoDocumentoId));

        Cartorio cartorio = cartorioRepository.findById(cartorioId)
                .orElseThrow(() -> new IllegalArgumentException("Cartório não encontrado com ID: " + cartorioId));

        Usuario usuario = usuarioRepository.findByCpf(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com CPF: " + username));

        Integer ultimoNumero = documentoRepository.findMaxNumeroByTipoDocumentoAndAno(tipoDocumentoId, anoAtual);
        if (ultimoNumero == null) {
            ultimoNumero = 0;
        }

        Documento novoDocumento = new Documento();
        novoDocumento.setTipoDocumento(tipoDocumento);
        novoDocumento.setCartorio(cartorio); 
        novoDocumento.setUsuario(usuario);
        novoDocumento.setNumero(ultimoNumero + 1);
        novoDocumento.setDataCadastro(agora);

        documentoRepository.save(novoDocumento);

        return novoDocumento;
    }

    public Integer obterUltimaNumeracao(Long tipoDocumentoId) {
        int anoAtual = LocalDateTime.now().getYear();
        Integer ultimaNumeracao = documentoRepository.findMaxNumeroByTipoDocumentoAndAno(tipoDocumentoId, anoAtual);
        return (ultimaNumeracao != null) ? ultimaNumeracao : 0;
    }
    
    public List<DocumentoDTO> buscarUltimosDocumentosDTOs() {
        return documentoRepository.findByOrderByDataCadastroDesc().stream().map(doc -> {
            DocumentoDTO dto = new DocumentoDTO();
            dto.setId(doc.getIdDocumento());
            dto.setNumero(doc.getNumero());
            dto.setDataCadastro(doc.getDataCadastro());

            dto.setUsuarioId(doc.getUsuario().getId());
            dto.setNomeUsuario(doc.getUsuario().getNome());

            dto.setTipoDocumentoId(doc.getTipoDocumento().getId());
            dto.setNomeTipoDocumento(doc.getTipoDocumento().getNome());

            dto.setCartorioId(doc.getCartorio().getId());
            dto.setNomeCartorio(doc.getCartorio().getNome());

            return dto;
        }).collect(Collectors.toList());
    }

    public Optional<Documento> buscarPorId(Long id) {
        return documentoRepository.findById(id);
    }

    @Transactional
    public Documento salvar(Documento documento) {
        documento.setDataCadastro(LocalDateTime.now());
        return documentoRepository.save(documento);
    }

    @Transactional
    public Optional<Documento> atualizar(Long id, Documento documentoAtualizado) {
        return documentoRepository.findById(id).map(doc -> {
            doc.setTipoDocumento(documentoAtualizado.getTipoDocumento());
            doc.setCartorio(documentoAtualizado.getCartorio());
            doc.setUsuario(documentoAtualizado.getUsuario());
            doc.setNumero(documentoAtualizado.getNumero());
            return documentoRepository.save(doc);
        });
    }

    @Transactional
    public boolean deletar(Long id) {
        return documentoRepository.findById(id).map(doc -> {
            documentoRepository.delete(doc);
            return true;
        }).orElse(false);
    }

    public List<DocumentoDTO> listarTodosDTO() {
        return documentoRepository.findAll().stream().map(doc -> {
            DocumentoDTO dto = new DocumentoDTO();
            dto.setId(doc.getIdDocumento());
            dto.setNumero(doc.getNumero());
            dto.setDataCadastro(doc.getDataCadastro());
            dto.setUsuarioId(doc.getUsuario().getId());
            dto.setTipoDocumentoId(doc.getTipoDocumento().getId());
            dto.setCartorioId(doc.getCartorio().getId());
            return dto;
        }).collect(Collectors.toList());
    }
    
 // Lista todos os documentos dentro de um período como DTO, com filtros opcionais
    public List<Documento> findByPeriodo(LocalDateTime inicio, LocalDateTime fim,
                                         Long tipoDocumentoId, Long cartorioId, Long usuarioId) {
        return documentoRepository.findByPeriodo(inicio, fim, tipoDocumentoId, cartorioId, usuarioId);
    }
    

 // Contagem de documentos por tipo no período com filtros opcionais (MÚLTIPLOS IDS)
    public List<Object[]> contarDocumentosPorTipoPeriodo(LocalDateTime inicio, LocalDateTime fim,
                                                         List<Long> tipoDocumentoIds, List<Long> cartorioIds, List<Long> usuarioIds) {
        // CORREÇÃO: Aponta para o método real que existe no seu DocumentoRepository
        return documentoRepository.contarDocumentosPorTipoNoPeriodoMulti(inicio, fim, tipoDocumentoIds, cartorioIds, usuarioIds);
    }

    // Contagem de documentos por cartório no período com filtros opcionais (MÚLTIPLOS IDS)
    public List<Object[]> contarDocumentosPorCartorioPeriodo(LocalDateTime inicio, LocalDateTime fim,
                                                             List<Long> tipoDocumentoIds, List<Long> cartorioIds, List<Long> usuarioIds) {
        // CORREÇÃO: Aponta para o método real que existe no seu DocumentoRepository
        return documentoRepository.contarDocumentosPorCartorioNoPeriodoMulti(inicio, fim, tipoDocumentoIds, cartorioIds, usuarioIds);
    }

// Contagem de documentos por usuário no período com filtros opcionais (MÚLTIPLOS IDS)
    public List<Object[]> contarDocumentosPorUsuarioPeriodo(LocalDateTime inicio, LocalDateTime fim,
                                                            List<Long> tipoDocumentoIds, List<Long> cartorioIds, List<Long> usuarioIds) {
        // CORREÇÃO: Aponta para o método real que existe no seu DocumentoRepository
        return documentoRepository.contarDocumentosPorUsuarioNoPeriodoMulti(inicio, fim, tipoDocumentoIds, cartorioIds, usuarioIds);
    }

   public List<Documento> findByPeriodoMulti(LocalDateTime inicio, LocalDateTime fim, 
                                              List<Long> tipos, List<Long> cartorios, List<Long> usuarios) {
        return documentoRepository.findByPeriodoMulti(inicio, fim, tipos, cartorios, usuarios);
    }

    public List<Object[]> contarDocumentosPorTipoNoPeriodoMulti(LocalDateTime inicio, LocalDateTime fim, 
                                                                List<Long> tipos, List<Long> cartorios, List<Long> usuarios) {
        return documentoRepository.contarDocumentosPorTipoNoPeriodoMulti(inicio, fim, tipos, cartorios, usuarios);
    }

    public List<Object[]> contarDocumentosPorCartorioNoPeriodoMulti(LocalDateTime inicio, LocalDateTime fim, 
                                                                    List<Long> tipos, List<Long> cartorios, List<Long> usuarios) {
        return documentoRepository.contarDocumentosPorCartorioNoPeriodoMulti(inicio, fim, tipos, cartorios, usuarios);
    }

    public List<Object[]> contarDocumentosPorUsuarioNoPeriodoMulti(LocalDateTime inicio, LocalDateTime fim, 
                                                                   List<Long> tipos, List<Long> cartorios, List<Long> usuarios) {
        return documentoRepository.contarDocumentosPorUsuarioNoPeriodoMulti(inicio, fim, tipos, cartorios, usuarios);
    }

    
}
