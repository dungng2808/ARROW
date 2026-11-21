package org.aksw.rdfunit.services;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PrefixNSService_ArrowTest {

    @Test
    public void getNSFromPrefixReturnsKnownNamespace() {
        assertThat(PrefixNSService.getNSFromPrefix("rdf"))
                .isEqualTo("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    }

    @Test
    public void getNSFromPrefixReturnsNullForUnknownPrefix() {
        assertThat(PrefixNSService.getNSFromPrefix("unknown-prefix")).isNull();
    }

    @Test
    public void getPrefixFromNSReturnsKnownPrefix() {
        assertThat(PrefixNSService.getPrefixFromNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))
                .isEqualTo("rdf");
    }

    @Test
    public void getPrefixFromNSReturnsNullForUnknownNamespace() {
        assertThat(PrefixNSService.getPrefixFromNS("http://example.com/unknown#")).isNull();
    }

    @Test
    public void getURIFromAbbrevExpandsKnownPrefix() {
        assertThat(PrefixNSService.getURIFromAbbrev("rdf:type"))
                .isEqualTo("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    }

    @Test
    public void getURIFromAbbrevWithUnknownPrefixReturnsNullConcatenatedWithLocalName() {
        assertThat(PrefixNSService.getURIFromAbbrev("unknown-prefix:local"))
                .isEqualTo("nulllocal");
    }

    @Test
    public void getURIFromAbbrevRejectsAbbreviationWithoutExactlyOneColon() {
        assertThatThrownBy(() -> PrefixNSService.getURIFromAbbrev("rdf:type:extra"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void getLocalNameRemovesNamespace() {
        assertThat(PrefixNSService.getLocalName(
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                "rdf")).isEqualTo("type");
    }

    @Test
    public void getLocalNameRejectsUnknownPrefix() {
        assertThatThrownBy(() -> PrefixNSService.getLocalName("http://example.com/resource", "unknown-prefix"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void setNSPrefixesInModelCopiesKnownPrefixes() {
        Model model = ModelFactory.createDefaultModel();

        PrefixNSService.setNSPrefixesInModel(model);

        assertThat(model.getNsPrefixURI("rdf"))
                .isEqualTo("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    }

    @Test
    public void getSparqlPrefixDeclContainsKnownPrefixDeclaration() {
        assertThat(PrefixNSService.getSparqlPrefixDecl())
                .contains(" PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
    }
}
