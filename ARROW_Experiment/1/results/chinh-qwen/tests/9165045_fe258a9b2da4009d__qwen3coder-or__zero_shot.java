package org.aksw.rdfunit.services;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.aksw.rdfunit.Resources;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class PrefixNSService_ArrowTest {

    @Test
    public void testGetNSFromPrefix() {
        String ns = PrefixNSService.getNSFromPrefix("rdf");
        assertNotNull(ns);
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#", ns);
        
        String unknownNs = PrefixNSService.getNSFromPrefix("unknown");
        assertNull(unknownNs);
    }

    @Test
    public void testGetPrefixFromNS() {
        String prefix = PrefixNSService.getPrefixFromNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        assertNotNull(prefix);
        assertEquals("rdf", prefix);
        
        String unknownPrefix = PrefixNSService.getPrefixFromNS("http://unknown.namespace/");
        assertNull(unknownPrefix);
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
        
        try {
            PrefixNSService.getURIFromAbbrev("invalid");
            fail("Should have thrown IllegalArgumentException");
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
            PrefixNSService.getLocalName("http://example.com/test", "unknown");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testSetNSPrefixesInModel() {
        Model model = ModelFactory.createDefaultModel();
        PrefixNSService.setNSPrefixesInModel(model);
        assertNotNull(model);
        // Just checking that the method doesn't throw an exception
    }

    @Test
    public void testGetPrefixMap() {
        Map<String, String> prefixMap = PrefixNSService.getPrefixMap();
        assertNotNull(prefixMap);
        assertFalse(prefixMap.isEmpty());
        assertTrue(prefixMap instanceof Collections.UnmodifiableMap);
    }
}
