package com.javiermoreno.bankof.model.domain;

import static org.junit.Assert.assertEquals;

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

    @Test(expected = IllegalArgumentException.class)
    public void transferirToSameAccountThrows() {
        CuentaCorriente cuenta = new CuentaCorriente("cuenta", new BigDecimal("100.00"), BigDecimal.ZERO);

        cuenta.transferir(cuenta, new BigDecimal("10.00"));
    }

    @Test(expected = BusinessException.class)
    public void transferirNegativeAmountThrows() {
        CuentaCorriente origen = new CuentaCorriente("origen", new BigDecimal("100.00"), BigDecimal.ZERO);
        CuentaCorriente destino = new CuentaCorriente("destino", BigDecimal.ZERO, BigDecimal.ZERO);

        origen.transferir(destino, new BigDecimal("-1.00"));
    }

    @Test(expected = BusinessException.class)
    public void transferirAmountGreaterThanBalanceThrows() {
        CuentaCorriente origen = new CuentaCorriente("origen", new BigDecimal("100.00"), BigDecimal.ZERO);
        CuentaCorriente destino = new CuentaCorriente("destino", BigDecimal.ZERO, BigDecimal.ZERO);

        origen.transferir(destino, new BigDecimal("100.01"));
    }

    @Test
    public void transferirExactBalance() {
        CuentaCorriente origen = new CuentaCorriente("origen", new BigDecimal("100.00"), BigDecimal.ZERO);
        CuentaCorriente destino = new CuentaCorriente("destino", new BigDecimal("25.00"), BigDecimal.ZERO);

        origen.transferir(destino, new BigDecimal("100.00"));

        assertEquals(new BigDecimal("0.00"), origen.getSaldo());
        assertEquals(new BigDecimal("125.00"), destino.getSaldo());
    }
}
