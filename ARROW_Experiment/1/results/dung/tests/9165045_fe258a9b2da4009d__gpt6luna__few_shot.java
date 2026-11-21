package org.aksw.rdfunit.services;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrefixNSService_ArrowTest {

    @Test
    public void getsNamespaceFromPrefix() {
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                PrefixNSService.getNSFromPrefix("rdf"));
    }

    @Test
    public void getsPrefixFromNamespace() {
        assertEquals("rdf",
                PrefixNSService.getPrefixFromNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
    }

    @Test
    public void setsPrefixesInModel() {
        Model model = ModelFactory.createDefaultModel();

        PrefixNSService.setNSPrefixesInModel(model);

        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                model.getNsPrefixURI("rdf"));
    }

    @Test
    public void getsSparqlPrefixDeclaration() {
        String declaration = PrefixNSService.getSparqlPrefixDecl();

        assertTrue(declaration.contains(
                " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"));
    }

    @Test
    public void expandsAbbreviation() {
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                PrefixNSService.getURIFromAbbrev("rdf:type"));
    }

    @Test
    public void unknownPrefixInAbbreviationProducesNullNamespaceText() {
        assertEquals("nullvalue", PrefixNSService.getURIFromAbbrev("unknown:value"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsAbbreviationWithMoreThanOneColon() {
        PrefixNSService.getURIFromAbbrev("rdf:type:extra");
    }

    @Test
    public void getsLocalNameByRemovingNamespace() {
        assertEquals("type",
                PrefixNSService.getLocalName(
                        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "rdf"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnknownPrefixWhenGettingLocalName() {
        PrefixNSService.getLocalName("http://example.org/value", "unknown");
    }
}
