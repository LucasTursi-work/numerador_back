package br.gov.ce.cgd.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.gov.ce.cgd.entity.TipoDocumento;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

    Page<TipoDocumento> findAll(Pageable pageable);

    Page<TipoDocumento> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    List<TipoDocumento> findByTipoDocumentoAtivoTrue(Sort sort);
}
