package br.ufg.inf.es.saep.sandbox.dominio.regra;

import br.ufg.inf.es.saep.sandbox.dominio.Avaliavel;
import br.ufg.inf.es.saep.sandbox.dominio.Valor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegraSomatorio_ArrowTest {

    @Test
    public void sumsExpressionForMatchingClass() {
        RegraSomatorio regra = new RegraSomatorio(
                "total", "Soma", 100f, 0f, "pontos", "relato");
        List<Avaliavel> avaliaveis = new ArrayList<Avaliavel>();
        avaliaveis.add(new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("relato");
                }
                if ("pontos".equals(nome)) {
                    return new Valor(3f);
                }
                return null;
            }
        });
        avaliaveis.add(new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("outra");
                }
                if ("pontos".equals(nome)) {
                    return new Valor(50f);
                }
                return null;
            }
        });
        avaliaveis.add(new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("relato");
                }
                if ("pontos".equals(nome)) {
                    return new Valor(4f);
                }
                return null;
            }
        });

        Valor resultado = regra.avalie(avaliaveis, new HashMap<String, Valor>());

        assertEquals(7f, resultado.getReal(), 0f);
    }

    @Test
    public void returnsZeroWhenNoEvaluablesMatch() {
        RegraSomatorio regra = new RegraSomatorio(
                "total", "Soma", 100f, 0f, "pontos", "relato");
        List<Avaliavel> avaliaveis = new ArrayList<Avaliavel>();
        avaliaveis.add(new Avaliavel() {
            @Override
            public Valor get(String nome) {
                return "classe".equals(nome) ? new Valor("outra") : new Valor(8f);
            }
        });

        Valor resultado = regra.avalie(avaliaveis, new HashMap<String, Valor>());

        assertEquals(0f, resultado.getReal(), 0f);
    }

    @Test
    public void returnsZeroForEmptyEvaluables() {
        RegraSomatorio regra = new RegraSomatorio(
                "total", "Soma", 100f, 0f, "pontos", "relato");

        Valor resultado = regra.avalie(
                new ArrayList<Avaliavel>(), new HashMap<String, Valor>());

        assertEquals(0f, resultado.getReal(), 0f);
    }

    @Test
    public void usesContextWhenMatchingEvaluableDoesNotProvideExpressionVariable() {
        RegraSomatorio regra = new RegraSomatorio(
                "total", "Soma", 100f, 0f, "pontos", "relato");
        List<Avaliavel> avaliaveis = new ArrayList<Avaliavel>();
        avaliaveis.add(new Avaliavel() {
            @Override
            public Valor get(String nome) {
                return "classe".equals(nome) ? new Valor("relato") : null;
            }
        });
        Map<String, Valor> contexto = new HashMap<String, Valor>();
        contexto.put("pontos", new Valor(6f));

        Valor resultado = regra.avalie(avaliaveis, contexto);

        assertEquals(6f, resultado.getReal(), 0f);
    }
}
