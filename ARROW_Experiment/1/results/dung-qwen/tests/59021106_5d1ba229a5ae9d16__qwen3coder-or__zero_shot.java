package br.ufg.inf.es.saep.sandbox.dominio.regra;

import br.ufg.inf.es.saep.sandbox.dominio.Avaliavel;
import br.ufg.inf.es.saep.sandbox.dominio.Valor;
import br.ufg.inf.es.saep.sandbox.dominio.excecoes.CampoExigidoNaoFornecido;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RegraSomatorio_ArrowTest {

    @Test
    public void testConstrutor() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "expr", "classe");
        assertNotNull(regra);
    }

    @Test
    public void testAvalieComAvaliaveisVazios() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "expr", "classe");
        List<Avaliavel> avaliaveis = new ArrayList<>();
        Map<String, Valor> contexto = new HashMap<>();

        Valor resultado = regra.avalie(avaliaveis, contexto);
        assertEquals(0f, resultado.getReal(), 0.001f);
    }

    @Test
    public void testAvalieComAvaliavelDaClasseCorreta() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "expr", "classe");
        List<Avaliavel> avaliaveis = new ArrayList<>();
        
        Avaliavel avaliavel = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("classe");
                }
                if ("valor".equals(nome)) {
                    return new Valor(5f);
                }
                return null;
            }
        };
        
        avaliaveis.add(avaliavel);
        Map<String, Valor> contexto = new HashMap<>();
        contexto.put("valor", new Valor(10f));

        Valor resultado = regra.avalie(avaliaveis, contexto);
        assertEquals(5f, resultado.getReal(), 0.001f);
    }

    @Test
    public void testAvalieComAvaliavelDeClasseDiferente() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "expr", "classe");
        List<Avaliavel> avaliaveis = new ArrayList<>();
        
        Avaliavel avaliavel = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("outraClasse");
                }
                return null;
            }
        };
        
        avaliaveis.add(avaliavel);
        Map<String, Valor> contexto = new HashMap<>();

        Valor resultado = regra.avalie(avaliaveis, contexto);
        assertEquals(0f, resultado.getReal(), 0.001f);
    }

    @Test
    public void testAvalieComMultiplosAvaliaveis() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "expr", "classe");
        List<Avaliavel> avaliaveis = new ArrayList<>();
        
        Avaliavel avaliavel1 = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("classe");
                }
                if ("valor".equals(nome)) {
                    return new Valor(5f);
                }
                return null;
            }
        };
        
        Avaliavel avaliavel2 = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("classe");
                }
                if ("valor".equals(nome)) {
                    return new Valor(3f);
                }
                return null;
            }
        };
        
        Avaliavel avaliavel3 = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("classe".equals(nome)) {
                    return new Valor("outraClasse");
                }
                return null;
            }
        };
        
        avaliaveis.add(avaliavel1);
        avaliaveis.add(avaliavel2);
        avaliaveis.add(avaliavel3);
        Map<String, Valor> contexto = new HashMap<>();
        contexto.put("valor", new Valor(10f));

        Valor resultado = regra.avalie(avaliaveis, contexto);
        assertEquals(8f, resultado.getReal(), 0.001f);
    }

    @Test
    public void testPrioridadeComVariavelNoAvaliavel() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "expr", "classe");
        Avaliavel avaliavel = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                if ("valor".equals(nome)) {
                    return new Valor(7f);
                }
                return null;
            }
        };
        Map<String, Valor> contexto = new HashMap<>();
        contexto.put("valor", new Valor(10f));

        float resultado = regra.prioridade("valor", avaliavel, contexto);
        assertEquals(7f, resultado, 0.001f);
    }

    @Test
    public void testPrioridadeComVariavelNoContexto() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "expr", "classe");
        Avaliavel avaliavel = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                return null;
            }
        };
        Map<String, Valor> contexto = new HashMap<>();
        contexto.put("valor", new Valor(10f));

        float resultado = regra.prioridade("valor", avaliavel, contexto);
        assertEquals(10f, resultado, 0.001f);
    }

    @Test
    public void testPrioridadeComVariavelNaoEncontrada() throws CampoExigidoNaoFornecido {
        RegraSomatorio regra = new RegraSomatorio("var", "desc", 100f, 0f, "expr", "classe");
        Avaliavel avaliavel = new Avaliavel() {
            @Override
            public Valor get(String nome) {
                return null;
            }
        };
        Map<String, Valor> contexto = new HashMap<>();

        float resultado = regra.prioridade("valor", avaliavel, contexto);
        assertEquals(0f, resultado, 0.001f);
    }
}
