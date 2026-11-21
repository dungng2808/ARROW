package br.ufg.inf.es.saep.sandbox.dominio.regra;

import br.ufg.inf.es.saep.sandbox.dominio.Avaliavel;
import br.ufg.inf.es.saep.sandbox.dominio.Valor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RegraSomatorio_ArrowTest {

    @Test
    public void avalieSomaExpressoesDosAvaliaveisDaClasse() {
        RegraSomatorio regra = new RegraSomatorio(
                "total", "soma", 100f, 0f, "pontos", "Relato");

        List<Avaliavel> avaliaveis = new ArrayList<Avaliavel>();
        Map<String, Valor> primeiro = new HashMap<String, Valor>();
        primeiro.put("classe", new Valor("Relato"));
        primeiro.put("pontos", new Valor(2f));
        avaliaveis.add(new Avaliavel(primeiro));

        Map<String, Valor> segundo = new HashMap<String, Valor>();
        segundo.put("classe", new Valor("Relato"));
        segundo.put("pontos", new Valor(3f));
        avaliaveis.add(new Avaliavel(segundo));

        Map<String, Valor> ignorado = new HashMap<String, Valor>();
        ignorado.put("classe", new Valor("Outro"));
        ignorado.put("pontos", new Valor(10f));
        avaliaveis.add(new Avaliavel(ignorado));

        Valor resultado = regra.avalie(avaliaveis, new HashMap<String, Valor>());

        assertEquals(5f, resultado.getReal(), 0f);
    }

    @Test
    public void avalieRetornaZeroQuandoNenhumAvaliavelPertenceAClasse() {
        RegraSomatorio regra = new RegraSomatorio(
                "total", "soma", 100f, 0f, "pontos", "Relato");

        List<Avaliavel> avaliaveis = new ArrayList<Avaliavel>();
        Map<String, Valor> dados = new HashMap<String, Valor>();
        dados.put("classe", new Valor("Outro"));
        dados.put("pontos", new Valor(10f));
        avaliaveis.add(new Avaliavel(dados));

        Valor resultado = regra.avalie(avaliaveis, new HashMap<String, Valor>());

        assertEquals(0f, resultado.getReal(), 0f);
    }

    @Test
    public void avalieRetornaZeroParaListaVazia() {
        RegraSomatorio regra = new RegraSomatorio(
                "total", "soma", 100f, 0f, "pontos", "Relato");

        Valor resultado = regra.avalie(
                new ArrayList<Avaliavel>(), new HashMap<String, Valor>());

        assertEquals(0f, resultado.getReal(), 0f);
    }
}
