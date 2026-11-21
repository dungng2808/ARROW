package com.javiermoreno.bankof.model.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;

import com.javiermoreno.dominaspring.framework.BusinessException;

public class CuentaCorriente_ArrowTest {

    @Test
    public void transferirMovesAmountBetweenAccounts() {
        CuentaCorriente origen = new CuentaCorriente("origen", new BigDecimal("100.00"), BigDecimal.ZERO);
        CuentaCorriente destino = new CuentaCorriente("destino", new BigDecimal("25.00"), BigDecimal.ZERO);

        origen.transferir(destino, new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), origen.getSaldo());
        assertEquals(new BigDecimal("55.00"), destino.getSaldo());
    }

    @Test
    public void transferirAllowsZeroAmount() {
        CuentaCorriente origen = new CuentaCorriente("origen", new BigDecimal("100.00"), BigDecimal.ZERO);
        CuentaCorriente destino = new CuentaCorriente("destino", new BigDecimal("25.00"), BigDecimal.ZERO);

        origen.transferir(destino, BigDecimal.ZERO);

        assertEquals(new BigDecimal("100.00"), origen.getSaldo());
        assertEquals(new BigDecimal("25.00"), destino.getSaldo());
    }

    @Test
    public void transferirRejectsTransferToSameAccount() {
        CuentaCorriente cuenta = new CuentaCorriente("cuenta", new BigDecimal("100.00"), BigDecimal.ZERO);

        try {
            cuenta.transferir(cuenta, BigDecimal.ONE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("Transferencia entre una única cuenta.", expected.getMessage());
        }
    }

    @Test
    public void transferirRejectsNegativeAmount() {
        CuentaCorriente origen = new CuentaCorriente("origen", new BigDecimal("100.00"), BigDecimal.ZERO);
        CuentaCorriente destino = new CuentaCorriente("destino", BigDecimal.ZERO, BigDecimal.ZERO);

        try {
            origen.transferir(destino, new BigDecimal("-1"));
            fail("Expected BusinessException");
        } catch (BusinessException expected) {
            assertEquals("Importe {0} debería de ser superior a 0.", expected.getMessage());
        }
    }

    @Test
    public void transferirRejectsAmountGreaterThanBalance() {
        CuentaCorriente origen = new CuentaCorriente("origen", new BigDecimal("10.00"), BigDecimal.ZERO);
        CuentaCorriente destino = new CuentaCorriente("destino", BigDecimal.ZERO, BigDecimal.ZERO);

        try {
            origen.transferir(destino, new BigDecimal("11.00"));
            fail("Expected BusinessException");
        } catch (BusinessException expected) {
            assertEquals("Saldo insuficiente para transferir el importe {0}.", expected.getMessage());
        }
    }
}
