package br.ufg.inf.es.saep.sandbox.dominio.regra;

import br.ufg.inf.es.saep.sandbox.dominio.Avaliavel;
import br.ufg.inf.es.saep.sandbox.dominio.Valor;
import br.ufg.inf.es.saep.sandbox.dominio.excecoes.CampoExigidoNaoFornecido;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RegraSomatorio_ArrowTest {

    @Test
    public void testConstructor() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "expr", "classe");
        assertNotNull(regra);
    }

    @Test
    public void testAvalieWithMatchingClass() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");
        
        Avaliavel avaliavel1 = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("classe1");
                }
                if ("var".equals(nome)) {
                    return new Valor(5f);
                }
                return null;
            }
        };
        
        Avaliavel avaliavel2 = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("classe2");
                }
                if ("var".equals(nome)) {
                    return new Valor(10f);
                }
                return null;
            }
        };
        
        List<Avaliavel> avaliaveis = Arrays.asList(avaliavel1, avaliavel2);
        Map<String, Valor> contexto = new HashMap<>();
        
        Valor resultado = regra.avalie(avaliaveis, contexto);
        assertEquals(10f, resultado.getReal(), 0.001f);
    }

    @Test
    public void testAvalieWithNoMatchingClass() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");
        
        Avaliavel avaliavel1 = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("classe2");
                }
                if ("var".equals(nome)) {
                    return new Valor(5f);
                }
                return null;
            }
        };
        
        List<Avaliavel> avaliaveis = Collections.singletonList(avaliavel1);
        Map<String, Valor> contexto = new HashMap<>();
        
        Valor resultado = regra.avalie(avaliaveis, contexto);
        assertEquals(0f, resultado.getReal(), 0.001f);
    }

    @Test
    public void testAvalieWithNullClassInAvailavel() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");
        
        Avaliavel avaliavel1 = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return null;
                }
                if ("var".equals(nome)) {
                    return new Valor(5f);
                }
                return null;
            }
        };
        
        List<Avaliavel> avaliaveis = Collections.singletonList(avaliavel1);
        Map<String, Valor> contexto = new HashMap<>();
        
        Valor resultado = regra.avalie(avaliaveis, contexto);
        assertEquals(0f, resultado.getReal(), 0.001f);
    }

    @Test
    public void testAvalieWithNullClassName() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", null);
        
        Avaliavel avaliavel1 = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("classe1");
                }
                if ("var".equals(nome)) {
                    return new Valor(5f);
                }
                return null;
            }
        };
        
        List<Avaliavel> avaliaveis = Collections.singletonList(avaliavel1);
        Map<String, Valor> contexto = new HashMap<>();
        
        Valor resultado = regra.avalie(avaliaveis, contexto);
        assertEquals(10f, resultado.getReal(), 0.001f);
    }

    @Test
    public void testPrioridadeWithAvaliavelValue() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");
        
        Avaliavel avaliavel = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("var".equals(nome)) {
                    return new Valor(5f);
                }
                return null;
            }
        };
        
        Map<String, Valor> contexto = new HashMap<>();
        float resultado = regra.prioridade("var", avaliavel, contexto);
        assertEquals(5f, resultado, 0.001f);
    }

    @Test
    public void testPrioridadeWithContextValue() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");
        
        Avaliavel avaliavel = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                return null;
            }
        };
        
        Map<String, Valor> contexto = new HashMap<>();
        contexto.put("var", new Valor(7f));
        
        float resultado = regra.prioridade("var", avaliavel, contexto);
        assertEquals(7f, resultado, 0.001f);
    }

    @Test
    public void testPrioridadeWithZeroDefault() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "10", "classe1");
        
        Avaliavel avaliavel = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                return null;
            }
        };
        
        Map<String, Valor> contexto = new HashMap<>();
        
        float resultado = regra.prioridade("var", avaliavel, contexto);
        assertEquals(0f, resultado, 0.001f);
    }
}
