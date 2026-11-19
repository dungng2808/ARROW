package org.acelera.brasil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Contador_ArrowTest {

    @Test
    public void quantidadeDeProdutoNaoAdicionadoEhZero() {
        Contador contador = new Contador();

        assertEquals(0, contador.quantidade("arroz"));
    }

    @Test
    public void addRegistraQuantidadeUm() {
        Contador contador = new Contador();

        contador.add("arroz");

        assertEquals(1, contador.quantidade("arroz"));
    }

    @Test
    public void adicionarProdutoNovamenteMantemQuantidadeUm() {
        Contador contador = new Contador();

        contador.add("arroz");
        contador.add("arroz");

        assertEquals(1, contador.quantidade("arroz"));
    }

    @Test
    public void produtosSaoContadosSeparadamente() {
        Contador contador = new Contador();

        contador.add("arroz");
        contador.add("feijao");

        assertEquals(1, contador.quantidade("arroz"));
        assertEquals(1, contador.quantidade("feijao"));
    }

    @Test(expected = NullPointerException.class)
    public void addComProdutoNuloLancaExcecao() {
        Contador contador = new Contador();

        contador.add(null);
    }

    @Test(expected = NullPointerException.class)
    public void quantidadeComProdutoNuloLancaExcecao() {
        Contador contador = new Contador();

        contador.quantidade(null);
    }
}
