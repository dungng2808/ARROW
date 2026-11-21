package com.coverity.security.pie.policy.csp;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CspPolicy_ArrowTest {

    @Test
    public void getNameReturnsCsp() {
        CspPolicy policy = new CspPolicy();

        assertEquals(policy.getName(), "csp");
    }

    @Test
    public void getRootFactMetaDataReturnsUriFactMetaDataSingleton() {
        CspPolicy policy = new CspPolicy();

        assertEquals(policy.getRootFactMetaData(),
                com.coverity.security.pie.policy.csp.fact.UriFactMetaData.getInstance());
    }

    @Test
    public void getPolicyForUriWithoutGrantsDeniesEveryDirective() {
        CspPolicy policy = new CspPolicy();

        assertEquals(policy.getPolicyForUri("https://example.com/page"),
                "connect-src 'none'; font-src 'none'; frame-src 'none'; img-src 'none'; "
                        + "media-src 'none'; object-src 'none'; script-src 'none'; style-src 'none'");
    }

    @Test
    public void logViolationAcceptsDocumentUri() {
        CspPolicy policy = new CspPolicy();

        policy.logViolation(URI.create("https://example.com/page"), "script-src", "blocked.example");
    }
}
