package br.gov.ce.cgd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.gov.ce.cgd.entity.Usuario;
import br.gov.ce.cgd.enumerate.Perfil;
import br.gov.ce.cgd.enumerate.Status;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByCpf(String cpf);

    Optional<Usuario> findByEmail(String email);
    
    List<Usuario> findByNomeContainingIgnoreCase(String nome);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM Usuario u WHERE "
    	    + "(:nome IS NULL OR :nome = '' OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) "
    	    + "AND (:cpf IS NULL OR :cpf = '' OR u.cpf LIKE CONCAT('%', :cpf, '%')) "
    	    + "AND (:perfil IS NULL OR u.perfil = :perfil) "
    	    + "AND (:status IS NULL OR u.status = :status) "
    	    + "ORDER BY u.nome ASC")
    	Page<Usuario> filtrarUsuarios(@Param("nome") String nome,
    	                              @Param("cpf") String cpf,
    	                              @Param("perfil") Perfil perfil,
    	                              @Param("status") Status status,
    	                              Pageable pageable);

}


