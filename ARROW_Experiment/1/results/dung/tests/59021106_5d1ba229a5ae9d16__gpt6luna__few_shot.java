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
    public void sumsExpressionForMatchingClass() {
        RegraSomatorio regra = new RegraSomatorio(
                "total", "Soma", 100f, 0f, "pontos", "relato");

        List<Avaliavel> avaliaveis = new ArrayList<Avaliavel>();
        Map<String, Valor> primeiro = new HashMap<String, Valor>();
        primeiro.put("classe", new Valor("relato"));
        primeiro.put("pontos", new Valor(3f));
        avaliaveis.add(new Avaliavel(primeiro));

        Map<String, Valor> segundo = new HashMap<String, Valor>();
        segundo.put("classe", new Valor("outra"));
        segundo.put("pontos", new Valor(10f));
        avaliaveis.add(new Avaliavel(segundo));

        Map<String, Valor> contexto = new HashMap<String, Valor>();
        assertEquals(3f, regra.avalie(avaliaveis, contexto).getReal(), 0f);
    }

    @Test
    public void returnsZeroWhenNoEvaluableMatches() {
        RegraSomatorio regra = new RegraSomatorio(
                "total", "Soma", 100f, 0f, "pontos", "relato");

        List<Avaliavel> avaliaveis = new ArrayList<Avaliavel>();
        Map<String, Valor> dados = new HashMap<String, Valor>();
        dados.put("classe", new Valor("outra"));
        dados.put("pontos", new Valor(10f));
        avaliaveis.add(new Avaliavel(dados));

        assertEquals(0f, regra.avalie(avaliaveis, new HashMap<String, Valor>()).getReal(), 0f);
    }

    @Test
    public void usesContextValueWhenEvaluableDoesNotContainVariable() {
        RegraSomatorio regra = new RegraSomatorio(
                "total", "Soma", 100f, 0f, "pontos", "relato");

        List<Avaliavel> avaliaveis = new ArrayList<Avaliavel>();
        Map<String, Valor> dados = new HashMap<String, Valor>();
        dados.put("classe", new Valor("relato"));
        avaliaveis.add(new Avaliavel(dados));

        Map<String, Valor> contexto = new HashMap<String, Valor>();
        contexto.put("pontos", new Valor(4f));

        assertEquals(4f, regra.avalie(avaliaveis, contexto).getReal(), 0f);
    }
}
