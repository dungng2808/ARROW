package br.ufg.inf.es.saep.sandbox.dominio.regra;

import br.ufg.inf.es.saep.sandbox.dominio.Avaliavel;
import br.ufg.inf.es.saep.sandbox.dominio.Valor;
import br.ufg.inf.es.saep.sandbox.dominio.excecoes.CampoExigidoNaoFornecido;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RegraSomatorio_ArrowTest {

    @Test
    public void testConstructor() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "expr", "classe");
        assertNotNull(regra);
    }

    @Test
    public void testAvalieWithMatchingClass() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");

        List<Avaliavel> avaliaveis = new ArrayList<>();
        Map<String, Valor> contexto = new HashMap<>();

        // Create an Avaliavel with matching class
        Avaliavel avaliavel1 = new Avaliavel() {
            @Override
            public Valor get(String atributo) {
                if ("classe".equals(atributo)) {
                    return new Valor("classe1");
                }
                return null;
            }
        };

        avaliaveis.add(avaliavel1);

        Valor result = regra.avalie(avaliaveis, contexto);
        assertEquals(10f, result.getReal(), 0.001f);
    }

    @Test
    public void testAvalieWithNonMatchingClass() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");

        List<Avaliavel> avaliaveis = new ArrayList<>();
        Map<String, Valor> contexto = new HashMap<>();

        // Create an Avaliavel with non-matching class
        Avaliavel avaliavel1 = new Avaliavel() {
            @Override
            public Valor get(String atributo) {
                if ("classe".equals(atributo)) {
                    return new Valor("classe2");
                }
                return null;
            }
        };

        avaliaveis.add(avaliavel1);

        Valor result = regra.avalie(avaliaveis, contexto);
        assertEquals(0f, result.getReal(), 0.001f);
    }

    @Test
    public void testAvalieWithNullClassInAvaliavel() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");

        List<Avaliavel> avaliaveis = new ArrayList<>();
        Map<String, Valor> contexto = new HashMap<>();

        // Create an Avaliavel with null class
        Avaliavel avaliavel1 = new Avaliavel() {
            @Override
            public Valor get(String atributo) {
                if ("classe".equals(atributo)) {
                    return null;
                }
                return null;
            }
        };

        avaliaveis.add(avaliavel1);

        Valor result = regra.avalie(avaliaveis, contexto);
        assertEquals(0f, result.getReal(), 0.001f);
    }

    @Test
    public void testAvalieWithMultipleMatchingAvaliaveis() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");

        List<Avaliavel> avaliaveis = new ArrayList<>();
        Map<String, Valor> contexto = new HashMap<>();

        // Create two Avaliaveis with matching class
        Avaliavel avaliavel1 = new Avaliavel() {
            @Override
            public Valor get(String atributo) {
                if ("classe".equals(atributo)) {
                    return new Valor("classe1");
                }
                return null;
            }
        };

        Avaliavel avaliavel2 = new Avaliavel() {
            @Override
            public Valor get(String atributo) {
                if ("classe".equals(atributo)) {
                    return new Valor("classe1");
                }
                return null;
            }
        };

        avaliaveis.add(avaliavel1);
        avaliaveis.add(avaliavel2);

        Valor result = regra.avalie(avaliaveis, contexto);
        assertEquals(20f, result.getReal(), 0.001f);
    }

    @Test
    public void testAvalieWithNoMatchingAvaliaveis() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");

        List<Avaliavel> avaliaveis = new ArrayList<>();
        Map<String, Valor> contexto = new HashMap<>();

        // Create Avaliaveis with non-matching class
        Avaliavel avaliavel1 = new Avaliavel() {
            @Override
            public Valor get(String atributo) {
                if ("classe".equals(atributo)) {
                    return new Valor("classe2");
                }
                return null;
            }
        };

        Avaliavel avaliavel2 = new Avaliavel() {
            @Override
            public Valor get(String atributo) {
                if ("classe".equals(atributo)) {
                    return new Valor("classe3");
                }
                return null;
            }
        };

        avaliaveis.add(avaliavel1);
        avaliaveis.add(avaliavel2);

        Valor result = regra.avalie(avaliaveis, contexto);
        assertEquals(0f, result.getReal(), 0.001f);
    }

    @Test
    public void testAvalieWithNullClassName() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", null);

        List<Avaliavel> avaliaveis = new ArrayList<>();
        Map<String, Valor> contexto = new HashMap<>();

        // Create an Avaliavel with any class
        Avaliavel avaliavel1 = new Avaliavel() {
            @Override
            public Valor get(String atributo) {
                if ("classe".equals(atributo)) {
                    return new Valor("classe1");
                }
                return null;
            }
        };

        avaliaveis.add(avaliavel1);

        Valor result = regra.avalie(avaliaveis, contexto);
        assertEquals(10f, result.getReal(), 0.001f);
    }

    @Test
    public void testPrioridadeWithAvaliavelValue() {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");

        Avaliavel avaliavel = new Avaliavel() {
            @Override
            public Valor get(String atributo) {
                if ("testVar".equals(atributo)) {
                    return new Valor(5f);
                }
                return null;
            }
        };

        Map<String, Valor> contexto = new HashMap<>();
        float result = regra.prioridade("testVar", avaliavel, contexto);
        assertEquals(5f, result, 0.001f);
    }

    @Test
    public void testPrioridadeWithContextValue() {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");

        Avaliavel avaliavel = new Avaliavel() {
            @Override
            public Valor get(String atributo) {
                return null;
            }
        };

        Map<String, Valor> contexto = new HashMap<>();
        contexto.put("testVar", new Valor(7f));

        float result = regra.prioridade("testVar", avaliavel, contexto);
        assertEquals(7f, result, 0.001f);
    }

    @Test
    public void testPrioridadeWithZeroDefault() {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");

        Avaliavel avaliavel = new Avaliavel() {
            @Override
            public Valor get(String atributo) {
                return null;
            }
        };

        Map<String, Valor> contexto = new HashMap<>();

        float result = regra.prioridade("nonexistent", avaliavel, contexto);
        assertEquals(0f, result, 0.001f);
    }
}
