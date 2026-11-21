package com.coverity.security.pie.policy.csp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import org.junit.Test;

public class CspPolicy_ArrowTest {

    @Test
    public void getNameReturnsCsp() {
        CspPolicy policy = new CspPolicy();
        assertEquals("csp", policy.getName());
    }

    @Test
    public void getRootFactMetaDataReturnsInstance() {
        CspPolicy policy = new CspPolicy();
        assertNotNull(policy.getRootFactMetaData());
    }

    @Test
    public void policyForUriWithoutGrantsDeniesAllDirectives() {
        CspPolicy policy = new CspPolicy();
        assertEquals(
                "connect-src 'none'; font-src 'none'; frame-src 'none'; frame-src 'none'; img-src 'none'; media-src 'none'; object-src 'none'; script-src 'none'; style-src 'none'",
                policy.getPolicyForUri("https://example.com"));
    }

    @Test
    public void logViolationAcceptsDocumentUri() {
        CspPolicy policy = new CspPolicy();
        policy.logViolation(URI.create("https://example.com/page"), "script-src", "blocked.example");
    }
}
