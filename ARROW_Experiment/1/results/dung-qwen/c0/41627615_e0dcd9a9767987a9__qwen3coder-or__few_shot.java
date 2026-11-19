package com.javiermoreno.bankof.model.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;

import com.javiermoreno.dominaspring.framework.BusinessException;

public class CuentaCorriente_ArrowTest {

    @Test
    public void testTransferirMismoProductoFinanciero() {
        CuentaCorriente cuenta = new CuentaCorriente("1234567890", new BigDecimal("1000"), new BigDecimal("2.5"));
        try {
            cuenta.transferir(cuenta, new BigDecimal("100"));
            fail("Debería haber lanzado IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Transferencia entre una única cuenta.", e.getMessage());
        }
    }

    @Test
    public void testTransferirImporteNegativo() {
        CuentaCorriente cuentaOrigen = new CuentaCorriente("1234567890", new BigDecimal("1000"), new BigDecimal("2.5"));
        CuentaCorriente cuentaDestino = new CuentaCorriente("0987654321", new BigDecimal("500"), new BigDecimal("2.5"));
        try {
            cuentaOrigen.transferir(cuentaDestino, new BigDecimal("-100"));
            fail("Debería haber lanzado BusinessException");
        } catch (BusinessException e) {
            assertEquals("Importe -100.0 debería de ser superior a 0.", e.getMessage());
        }
    }

    @Test
    public void testTransferirSaldoInsuficiente() {
        CuentaCorriente cuentaOrigen = new CuentaCorriente("1234567890", new BigDecimal("100"), new BigDecimal("2.5"));
        CuentaCorriente cuentaDestino = new CuentaCorriente("0987654321", new BigDecimal("500"), new BigDecimal("2.5"));
        try {
            cuentaOrigen.transferir(cuentaDestino, new BigDecimal("200"));
            fail("Debería haber lanzado BusinessException");
        } catch (BusinessException e) {
            assertEquals("Saldo insuficiente para transferir el importe 200.0.", e.getMessage());
        }
    }

    @Test
    public void testTransferirCorrecto() {
        CuentaCorriente cuentaOrigen = new CuentaCorriente("1234567890", new BigDecimal("1000"), new BigDecimal("2.5"));
        CuentaCorriente cuentaDestino = new CuentaCorriente("0987654321", new BigDecimal("500"), new BigDecimal("2.5"));
        
        cuentaOrigen.transferir(cuentaDestino, new BigDecimal("200"));
        
        assertEquals(new BigDecimal("800"), cuentaOrigen.getSaldo());
        assertEquals(new BigDecimal("700"), cuentaDestino.getSaldo());
    }
}
