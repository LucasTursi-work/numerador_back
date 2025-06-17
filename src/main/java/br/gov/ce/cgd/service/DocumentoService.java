package br.gov.ce.cgd.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
    
    public List<Documento> buscarUltimosDocumentos(int limite) {
        return documentoRepository.findByOrderByDataCadastroDesc(PageRequest.of(0, limite));
    }

    // Métodos CRUD que faltavam

    public List<Documento> listarTodos() {
        return documentoRepository.findAll();
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
            // Atualize outros campos se houver
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
}
