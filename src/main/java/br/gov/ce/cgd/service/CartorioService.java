package br.gov.ce.cgd.service;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ce.cgd.entity.Cartorio;
import br.gov.ce.cgd.repository.CartorioRepository;

@Service
public class CartorioService {

    private final CartorioRepository cartorioRepository;

    public CartorioService(CartorioRepository cartorioRepository) {
        this.cartorioRepository = cartorioRepository;
    }

//    public List<Cartorio> listarTodos() {
//        return cartorioRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
//    }
//    
    public Page<Cartorio> listarTodos(Pageable pageable) {
        return cartorioRepository.findAll(pageable);
    }

    public Page<Cartorio> listarPorNome(String nome, Pageable pageable) {
        return cartorioRepository.findByNomeContainingIgnoreCase(nome, pageable);
    }

    public List<Cartorio> listarAtivos() {
        return cartorioRepository.findByCartorioAtivoTrue(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Cartorio buscarPorId(Long id) {
        return cartorioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cartório não encontrado com ID: " + id));
    }

    @Transactional
    public Cartorio salvar(Cartorio cartorio) {
        cartorio.setCartorioAtivo(true);
        return cartorioRepository.save(cartorio);
    }

    @Transactional
    public Cartorio atualizar(Long id, Cartorio cartorioAtualizado) {
        Cartorio cartorio = buscarPorId(id);
        cartorio.setNome(cartorioAtualizado.getNome());
        cartorio.setCartorioAtivo(cartorioAtualizado.isCartorioAtivo());
        return cartorioRepository.save(cartorio);
    }

    @Transactional
    public void desativar(Long id) {
        Cartorio cartorio = buscarPorId(id);
        cartorio.setCartorioAtivo(false);
        cartorioRepository.save(cartorio);
    }

    @Transactional
    public void deletar(Long id) {
        if (!cartorioRepository.existsById(id)) {
            throw new EntityNotFoundException("Cartório não encontrado com ID: " + id);
        }
        cartorioRepository.deleteById(id);
    }
}
