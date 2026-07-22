package br.gov.ce.cgd.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.cgd.entity.TipoDocumento;
import br.gov.ce.cgd.repository.TipoDocumentoRepository;

@Service
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    public TipoDocumentoService(TipoDocumentoRepository tipoDocumentoRepository) {
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }

    public Page<TipoDocumento> listarTodos(Pageable pageable) {
        return tipoDocumentoRepository.findAll(pageable);
    }

    public Page<TipoDocumento> listarPorNome(String nome, Pageable pageable) {
        return tipoDocumentoRepository.findByNomeContainingIgnoreCase(nome, pageable);
    }

    public List<TipoDocumento> listarAtivos() {
        return tipoDocumentoRepository.findByTipoDocumentoAtivoTrue(Sort.by("nome").ascending());
    }

    public TipoDocumento buscarPorId(Long id) {
        return tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de Documento não encontrado com ID: " + id));
    }

    @Transactional
    public TipoDocumento salvar(TipoDocumento tipoDocumento) {
        tipoDocumento.setTipoDocumentoAtivo(true);
        return tipoDocumentoRepository.save(tipoDocumento);
    }

    @Transactional
    public TipoDocumento atualizar(Long id, TipoDocumento tipoDocumentoAtualizado) {
        TipoDocumento tipoDocumento = buscarPorId(id);
        tipoDocumento.setNome(tipoDocumentoAtualizado.getNome());
        tipoDocumento.setTipoDocumentoAtivo(tipoDocumentoAtualizado.isTipoDocumentoAtivo());
        return tipoDocumentoRepository.save(tipoDocumento);
    }

    @Transactional
    public void deletar(Long id) {
        if (!tipoDocumentoRepository.existsById(id)) {
            throw new RuntimeException("Tipo de Documento não encontrado com ID: " + id);
        }
        tipoDocumentoRepository.deleteById(id);
    }
}
