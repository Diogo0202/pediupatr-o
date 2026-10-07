package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto() {
        Produto p = new Produto();
        p.setNome("Calabresa");
        p.setTipo("Pizza");
        p.setPreco(45.0);
        p.setAtivo(true);
        return p;
    }

    @Test
    void novoProduto_salvaDireto() {
        Produto p = produto();
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        assertNotNull(produtoService.novoProduto(p));
        verify(produtoRepository).save(p);
    }

    @Test
    void listarProdutos_devolveResultadoDoRepositorio() {
        List<Produto> produtos = List.of(produto());
        when(produtoRepository.findAll()).thenReturn(produtos);

        assertEquals(produtos, produtoService.listarProdutos());
    }

    @Test
    void excluir_delegaParaRepositorio() {
        produtoService.excluir("1");
        verify(produtoRepository).deleteById("1");
    }

    @Test
    void salvar_produtoNovoCriaRegistro() {
        Produto p = produto();
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        assertNotNull(produtoService.salvar(p));
        verify(produtoRepository).save(p);
    }

    @Test
    void salvar_produtoExistenteAtualiza() {
        Produto p = produto();
        p.setId("1");
        when(produtoRepository.existsById("1")).thenReturn(true);
        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> inv.getArgument(0));

        assertNotNull(produtoService.salvar(p));
        verify(produtoRepository).save(p);
    }

    @Test
    void salvar_produtoInexistenteLancaExcecao() {
        Produto p = produto();
        p.setId("404");
        when(produtoRepository.existsById("404")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> produtoService.salvar(p));
        verify(produtoRepository, never()).save(any());
    }
}
