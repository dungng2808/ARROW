package com.amazonaws.secretsmanager.sql;

import java.sql.SQLException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AWSSecretsManagerPostgreSQLDriver_ArrowTest {

    @Test
    public void getPropertySubprefixReturnsPostgresql() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();

        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void authenticationErrorRecognizesPasswordFailureState() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();

        assertTrue(driver.isExceptionDueToAuthenticationError(
                new SQLException("invalid password", "28P01")));
    }

    @Test
    public void authenticationErrorRejectsOtherSqlState() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();

        assertFalse(driver.isExceptionDueToAuthenticationError(
                new SQLException("other error", "08006")));
    }

    @Test
    public void authenticationErrorRejectsNonSqlException() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();

        assertFalse(driver.isExceptionDueToAuthenticationError(new Exception("other error")));
    }

    @Test
    public void constructUrlIncludesPortAndDatabase() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();

        assertEquals("jdbc:postgresql://localhost:5432/app",
                driver.constructUrlFromEndpointPortDatabase("localhost", "5432", "app"));
    }

    @Test
    public void constructUrlOmitsEmptyPortAndDatabase() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();

        assertEquals("jdbc:postgresql://localhost/",
                driver.constructUrlFromEndpointPortDatabase("localhost", "", ""));
    }

    @Test
    public void getDefaultDriverClassReturnsPostgresqlDriver() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();

        assertEquals("org.postgresql.Driver", driver.getDefaultDriverClass());
    }
}
