package br.gov.ce.cgd.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.ce.cgd.entity.Cartorio;

public interface CartorioRepository extends JpaRepository<Cartorio, Long>{

	List<Cartorio> findByCartorioAtivoTrue(Sort id);
	
	Page<Cartorio> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Cartorio> findAll(Pageable pageable);

}
