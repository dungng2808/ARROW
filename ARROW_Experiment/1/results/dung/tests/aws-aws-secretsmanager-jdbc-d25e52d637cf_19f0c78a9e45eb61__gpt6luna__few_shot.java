package com.amazonaws.secretsmanager.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import org.junit.Test;

public class AWSSecretsManagerPostgreSQLDriver_ArrowTest {

    @Test
    public void returnsPostgreSQLPropertySubprefix() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void recognizesAuthenticationErrorSqlState() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertTrue(driver.isExceptionDueToAuthenticationError(
                new SQLException("authentication failed", "28P01")));
    }

    @Test
    public void doesNotRecognizeOtherSqlStateAsAuthenticationError() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertFalse(driver.isExceptionDueToAuthenticationError(
                new SQLException("other error", "08006")));
    }

    @Test
    public void doesNotRecognizeNonSqlExceptionAsAuthenticationError() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertFalse(driver.isExceptionDueToAuthenticationError(new Exception("other error")));
    }

    @Test
    public void constructsUrlWithEndpointPortAndDatabase() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("jdbc:postgresql://localhost:5432/example",
                driver.constructUrlFromEndpointPortDatabase("localhost", "5432", "example"));
    }

    @Test
    public void constructsUrlWithoutPortOrDatabase() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("jdbc:postgresql://localhost/",
                driver.constructUrlFromEndpointPortDatabase("localhost", "", ""));
    }

    @Test
    public void constructsUrlWithoutPort() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("jdbc:postgresql://localhost/example",
                driver.constructUrlFromEndpointPortDatabase("localhost", null, "example"));
    }

    @Test
    public void constructsUrlWithoutDatabase() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("jdbc:postgresql://localhost:5432/",
                driver.constructUrlFromEndpointPortDatabase("localhost", "5432", null));
    }

    @Test
    public void returnsDefaultDriverClass() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("org.postgresql.Driver", driver.getDefaultDriverClass());
    }
}
