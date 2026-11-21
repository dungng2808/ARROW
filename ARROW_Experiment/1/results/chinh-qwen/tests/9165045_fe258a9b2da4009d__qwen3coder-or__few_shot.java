package org.aksw.rdfunit.services;

import com.google.common.collect.BiMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrefixNSService_ArrowTest {

    @Test
    public void testGetNSFromPrefix() {
        String ns = PrefixNSService.getNSFromPrefix("rdf");
        assertNotNull(ns);
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#", ns);
        
        String nullNs = PrefixNSService.getNSFromPrefix("nonexistent");
        assertNull(nullNs);
    }

    @Test
    public void testGetPrefixFromNS() {
        String prefix = PrefixNSService.getPrefixFromNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        assertNotNull(prefix);
        assertEquals("rdf", prefix);
        
        String nullPrefix = PrefixNSService.getPrefixFromNS("http://nonexistent.org/");
        assertNull(nullPrefix);
    }

    @Test
    public void testGetSparqlPrefixDecl() {
        String sparqlDecl = PrefixNSService.getSparqlPrefixDecl();
        assertNotNull(sparqlDecl);
        assertTrue(sparqlDecl.contains("PREFIX rdf:"));
    }

    @Test
    public void testGetURIFromAbbrev() {
        String uri = PrefixNSService.getURIFromAbbrev("rdf:type");
        assertNotNull(uri);
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", uri);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetURIFromAbbrevInvalid() {
        PrefixNSService.getURIFromAbbrev("invalid");
    }

    @Test
    public void testGetLocalName() {
        String localName = PrefixNSService.getLocalName(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "rdf"
        );
        assertNotNull(localName);
        assertEquals("type", localName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLocalNameInvalidPrefix() {
        PrefixNSService.getLocalName("http://example.org/test", "nonexistent");
    }

    @Test
    public void testSetNSPrefixesInModel() {
        Model model = ModelFactory.createDefaultModel();
        PrefixNSService.setNSPrefixesInModel(model);
        assertNotNull(model);
        assertTrue(model.getNsPrefixMap().size() > 0);
    }
}
