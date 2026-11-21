package org.aksw.rdfunit.services;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PrefixNSService_ArrowTest {

    @Test
    public void getNSFromPrefixReturnsKnownNamespace() {
        String namespace = PrefixNSService.getNSFromPrefix("rdf");

        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#", namespace);
    }

    @Test
    public void getNSFromPrefixReturnsNullForUnknownPrefix() {
        assertNull(PrefixNSService.getNSFromPrefix("unknownPrefix"));
    }

    @Test
    public void getPrefixFromNSReturnsKnownPrefix() {
        String prefix = PrefixNSService.getPrefixFromNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        assertEquals("rdf", prefix);
    }

    @Test
    public void getPrefixFromNSReturnsNullForUnknownNamespace() {
        assertNull(PrefixNSService.getPrefixFromNS("http://example.com/unknown#"));
    }

    @Test
    public void getURIFromAbbrevExpandsKnownPrefix() {
        String uri = PrefixNSService.getURIFromAbbrev("rdf:type");

        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", uri);
    }

    @Test
    public void getURIFromAbbrevRejectsAbbreviationWithoutOneColon() {
        try {
            PrefixNSService.getURIFromAbbrev("rdf");
        } catch (IllegalArgumentException expected) {
            assertEquals("Undefined prefix in rdf", expected.getMessage());
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void getLocalNameRemovesKnownNamespace() {
        String localName = PrefixNSService.getLocalName(
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                "rdf");

        assertEquals("type", localName);
    }

    @Test
    public void getLocalNameRejectsUnknownPrefix() {
        try {
            PrefixNSService.getLocalName("http://example.com/resource", "unknownPrefix");
        } catch (IllegalArgumentException expected) {
            assertEquals("Undefined prefix (unknownPrefix) in URI: http://example.com/resource",
                    expected.getMessage());
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void getSparqlPrefixDeclContainsRdfDeclaration() {
        String declarations = PrefixNSService.getSparqlPrefixDecl();

        assertNotNull(declarations);
        assertTrue(declarations.contains(" PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"));
    }

    @Test
    public void setNSPrefixesInModelCopiesKnownPrefixes() {
        Model model = ModelFactory.createDefaultModel();

        PrefixNSService.setNSPrefixesInModel(model);

        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                model.getNsPrefixURI("rdf"));
    }
}
