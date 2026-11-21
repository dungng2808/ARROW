package org.aksw.rdfunit.services;

import com.google.common.collect.BiMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import java.util.Map;

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
    public void testSetNSPrefixesInModel() {
        Model model = ModelFactory.createDefaultModel();
        PrefixNSService.setNSPrefixesInModel(model);
        
        Map<String, String> prefixMap = model.getNsPrefixMap();
        assertNotNull(prefixMap);
        assertTrue(prefixMap.size() > 0);
        assertTrue(prefixMap.containsKey("rdf"));
    }

    @Test
    public void testGetSparqlPrefixDecl() {
        String sparqlPrefixes = PrefixNSService.getSparqlPrefixDecl();
        assertNotNull(sparqlPrefixes);
        assertTrue(sparqlPrefixes.contains("PREFIX rdf:"));
        assertTrue(sparqlPrefixes.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
    }

    @Test
    public void testGetURIFromAbbrev() {
        String uri = PrefixNSService.getURIFromAbbrev("rdf:type");
        assertNotNull(uri);
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", uri);
        
        try {
            PrefixNSService.getURIFromAbbrev("invalid");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGetLocalName() {
        String localName = PrefixNSService.getLocalName(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "rdf"
        );
        assertNotNull(localName);
        assertEquals("type", localName);
        
        try {
            PrefixNSService.getLocalName("http://example.org/test", "nonexistent");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGetPrefixMap() {
        Map<String, String> prefixMap = PrefixNSService.getPrefixMap();
        assertNotNull(prefixMap);
        assertTrue(prefixMap.size() > 0);
        assertTrue(prefixMap.containsKey("rdf"));
        assertTrue(prefixMap instanceof java.util.Collections.UnmodifiableMap);
    }
}
