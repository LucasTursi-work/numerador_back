package br.gov.ce.cgd.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.gov.ce.cgd.entity.Documento;
import br.gov.ce.cgd.entity.TipoDocumento;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    Optional<Documento> findTopByTipoDocumentoOrderByNumeroDesc(TipoDocumento tipoDocumento);

    @Query("SELECT MAX(d.numero) FROM Documento d WHERE d.tipoDocumento.id = :tipoDocumentoId AND FUNCTION('YEAR', d.dataCadastro) = :ano")
    Integer findMaxNumeroByTipoDocumentoAndAno(@Param("tipoDocumentoId") Long tipoDocumentoId, @Param("ano") int ano);

    // Contagem total por tipo
    @Query("SELECT d.tipoDocumento.nome AS tipo, COUNT(d) AS total " +
           "FROM Documento d " +
           "GROUP BY d.tipoDocumento.nome " +
           "ORDER BY total DESC")
    List<Object[]> contarDocumentosPorTipo();

    // Contagem total por cartório
    @Query("SELECT d.cartorio.nome AS cartorio, COUNT(d) AS total " +
           "FROM Documento d " +
           "GROUP BY d.cartorio.nome " +
           "ORDER BY d.cartorio.nome ASC")
    List<Object[]> contarTotalDocumentosPorCartorio();

    // Contagem total por usuário
    @Query("SELECT d.usuario.nome AS usuario, COUNT(d) AS total " +
           "FROM Documento d " +
           "GROUP BY d.usuario.nome " +
           "ORDER BY d.usuario.nome ASC")
    List<Object[]> contarTotalDocumentosPorUsuario();

    // Últimos documentos
    List<Documento> findByOrderByDataCadastroDesc();

    // Documentos por período com filtros opcionais
    @Query("SELECT d FROM Documento d " +
           "WHERE d.dataCadastro >= :inicio AND d.dataCadastro <= :fim " +
           "AND (:tipoDocumentoId IS NULL OR d.tipoDocumento.id = :tipoDocumentoId) " +
           "AND (:cartorioId IS NULL OR d.cartorio.id = :cartorioId) " +
           "AND (:usuarioId IS NULL OR d.usuario.id = :usuarioId)")
    List<Documento> findByPeriodo(@Param("inicio") LocalDateTime inicio,
                                  @Param("fim") LocalDateTime fim,
                                  @Param("tipoDocumentoId") Long tipoDocumentoId,
                                  @Param("cartorioId") Long cartorioId,
                                  @Param("usuarioId") Long usuarioId);

    // Contagem por tipo no período
    @Query("SELECT d.tipoDocumento.nome AS tipo, COUNT(d) AS total " +
           "FROM Documento d " +
           "WHERE d.dataCadastro >= :inicio AND d.dataCadastro <= :fim " +
           "AND (:tipoDocumentoId IS NULL OR d.tipoDocumento.id = :tipoDocumentoId) " +
           "AND (:cartorioId IS NULL OR d.cartorio.id = :cartorioId) " +
           "AND (:usuarioId IS NULL OR d.usuario.id = :usuarioId) " +
           "GROUP BY d.tipoDocumento.nome " +
           "ORDER BY total DESC")
    List<Object[]> contarDocumentosPorTipoNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                                    @Param("fim") LocalDateTime fim,
                                                    @Param("tipoDocumentoId") Long tipoDocumentoId,
                                                    @Param("cartorioId") Long cartorioId,
                                                    @Param("usuarioId") Long usuarioId);

    // Contagem por cartório no período
    @Query("SELECT d.cartorio.nome AS cartorio, COUNT(d) AS total " +
           "FROM Documento d " +
           "WHERE d.dataCadastro >= :inicio AND d.dataCadastro <= :fim " +
           "AND (:tipoDocumentoId IS NULL OR d.tipoDocumento.id = :tipoDocumentoId) " +
           "AND (:cartorioId IS NULL OR d.cartorio.id = :cartorioId) " +
           "AND (:usuarioId IS NULL OR d.usuario.id = :usuarioId) " +
           "GROUP BY d.cartorio.nome " +
           "ORDER BY d.cartorio.nome ASC")
    List<Object[]> contarDocumentosPorCartorioNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                                       @Param("fim") LocalDateTime fim,
                                                       @Param("tipoDocumentoId") Long tipoDocumentoId,
                                                       @Param("cartorioId") Long cartorioId,
                                                       @Param("usuarioId") Long usuarioId);

    // Contagem por usuário no período
    @Query("SELECT d.usuario.nome AS usuario, COUNT(d) AS total " +
           "FROM Documento d " +
           "WHERE d.dataCadastro >= :inicio AND d.dataCadastro <= :fim " +
           "AND (:tipoDocumentoId IS NULL OR d.tipoDocumento.id = :tipoDocumentoId) " +
           "AND (:cartorioId IS NULL OR d.cartorio.id = :cartorioId) " +
           "AND (:usuarioId IS NULL OR d.usuario.id = :usuarioId) " +
           "GROUP BY d.usuario.nome " +
           "ORDER BY d.usuario.nome ASC")
    List<Object[]> contarDocumentosPorUsuarioNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                                       @Param("fim") LocalDateTime fim,
                                                       @Param("tipoDocumentoId") Long tipoDocumentoId,
                                                       @Param("cartorioId") Long cartorioId,
                                                       @Param("usuarioId") Long usuarioId);

// 1. Logs de documentos
    @Query("SELECT d FROM Documento d " +
           "WHERE (:inicio IS NULL OR d.dataCadastro >= :inicio) " +
           "  AND (:fim IS NULL OR d.dataCadastro <= :fim) " +
           "  AND (cast(:tiposDocumentoIds as text) IS NULL OR d.tipoDocumento.id IN :tiposDocumentoIds) " +
           "  AND (cast(:cartoriosIds as text) IS NULL OR d.cartorio.id IN :cartoriosIds) " +
           "  AND (cast(:usuariosIds as text) IS NULL OR d.usuario.id IN :usuariosIds)")
    List<Documento> findByPeriodoMulti(@Param("inicio") LocalDateTime inicio,
                                       @Param("fim") LocalDateTime fim,
                                       @Param("tiposDocumentoIds") List<Long> tiposDocumentoIds,
                                       @Param("cartoriosIds") List<Long> cartoriosIds,
                                       @Param("usuariosIds") List<Long> usuariosIds);

    // 2. Contagem por tipo no período
    @Query("SELECT d.tipoDocumento.nome AS tipo, COUNT(d) AS total " +
           "FROM Documento d " +
           "WHERE (:inicio IS NULL OR d.dataCadastro >= :inicio) " +
           "  AND (:fim IS NULL OR d.dataCadastro <= :fim) " +
           "  AND (cast(:tiposDocumentoIds as text) IS NULL OR d.tipoDocumento.id IN :tiposDocumentoIds) " +
           "  AND (cast(:cartoriosIds as text) IS NULL OR d.cartorio.id IN :cartoriosIds) " +
           "  AND (cast(:usuariosIds as text) IS NULL OR d.usuario.id IN :usuariosIds) " +
           "GROUP BY d.tipoDocumento.nome " +
           "ORDER BY total DESC")
    List<Object[]> contarDocumentosPorTipoNoPeriodoMulti(@Param("inicio") LocalDateTime inicio,
                                                         @Param("fim") LocalDateTime fim,
                                                         @Param("tiposDocumentoIds") List<Long> tiposDocumentoIds,
                                                         @Param("cartoriosIds") List<Long> cartoriosIds,
                                                         @Param("usuariosIds") List<Long> usuariosIds);

    // 3. Contagem por cartório no período
    @Query("SELECT d.cartorio.nome AS cartorio, COUNT(d) AS total " +
           "FROM Documento d " +
           "WHERE (:inicio IS NULL OR d.dataCadastro >= :inicio) " +
           "  AND (:fim IS NULL OR d.dataCadastro <= :fim) " +
           "  AND (cast(:tiposDocumentoIds as text) IS NULL OR d.tipoDocumento.id IN :tiposDocumentoIds) " +
           "  AND (cast(:cartoriosIds as text) IS NULL OR d.cartorio.id IN :cartoriosIds) " +
           "  AND (cast(:usuariosIds as text) IS NULL OR d.usuario.id IN :usuariosIds) " +
           "GROUP BY d.cartorio.nome " +
           "ORDER BY d.cartorio.nome ASC")
    List<Object[]> contarDocumentosPorCartorioNoPeriodoMulti(@Param("inicio") LocalDateTime inicio,
                                                             @Param("fim") LocalDateTime fim,
                                                             @Param("tiposDocumentoIds") List<Long> tiposDocumentoIds,
                                                             @Param("cartoriosIds") List<Long> cartoriosIds,
                                                             @Param("usuariosIds") List<Long> usuariosIds);

    // 4. Contagem por usuário no período
    @Query("SELECT d.usuario.nome AS usuario, COUNT(d) AS total " +
           "FROM Documento d " +
           "WHERE (:inicio IS NULL OR d.dataCadastro >= :inicio) " +
           "  AND (:fim IS NULL OR d.dataCadastro <= :fim) " +
           "  AND (cast(:tiposDocumentoIds as text) IS NULL OR d.tipoDocumento.id IN :tiposDocumentoIds) " +
           "  AND (cast(:cartoriosIds as text) IS NULL OR d.cartorio.id IN :cartoriosIds) " +
           "  AND (cast(:usuariosIds as text) IS NULL OR d.usuario.id IN :usuariosIds) " +
           "GROUP BY d.usuario.nome " +
           "ORDER BY d.usuario.nome ASC")
    List<Object[]> contarDocumentosPorUsuarioNoPeriodoMulti(@Param("inicio") LocalDateTime inicio,
                                                            @Param("fim") LocalDateTime fim,
                                                            @Param("tiposDocumentoIds") List<Long> tiposDocumentoIds,
                                                            @Param("cartoriosIds") List<Long> cartoriosIds,
                                                            @Param("usuariosIds") List<Long> usuariosIds);
}
