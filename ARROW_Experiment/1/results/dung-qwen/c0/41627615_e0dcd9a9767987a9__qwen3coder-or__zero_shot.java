package com.javiermoreno.bankof.model.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;

import com.javiermoreno.dominaspring.framework.BusinessException;

public class CuentaCorriente_ArrowTest {

    @Test
    public void testConstructorDefault() {
        CuentaCorriente cuenta = new CuentaCorriente();
        assertNotNull(cuenta);
    }

    @Test
    public void testConstructorWithParameters() {
        BigDecimal saldo = new BigDecimal("1000.00");
        BigDecimal interes = new BigDecimal("5.00");
        CuentaCorriente cuenta = new CuentaCorriente("CC001", saldo, interes);
        assertNotNull(cuenta);
        assertEquals("CC001", cuenta.getCodigo());
        assertEquals(saldo, cuenta.getSaldo());
        assertEquals(interes, cuenta.getInteresAnual());
    }

    @Test
    public void testTransferirMismoCuenta() {
        CuentaCorriente cuenta = new CuentaCorriente("CC001", new BigDecimal("1000.00"), new BigDecimal("5.00"));
        try {
            cuenta.transferir(cuenta, new BigDecimal("100.00"));
            fail("Debería haber lanzado IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Transferencia entre una única cuenta.", e.getMessage());
        }
    }

    @Test
    public void testTransferirImporteNegativo() {
        CuentaCorriente origen = new CuentaCorriente("CC001", new BigDecimal("1000.00"), new BigDecimal("5.00"));
        CuentaCorriente destino = new CuentaCorriente("CC002", new BigDecimal("500.00"), new BigDecimal("3.00"));
        try {
            origen.transferir(destino, new BigDecimal("-100.00"));
            fail("Debería haber lanzado BusinessException");
        } catch (BusinessException e) {
            assertEquals("Importe -100.00 debería de ser superior a 0.", e.getMessage());
        }
    }

    @Test
    public void testTransferirSaldoInsuficiente() {
        CuentaCorriente origen = new CuentaCorriente("CC001", new BigDecimal("100.00"), new BigDecimal("5.00"));
        CuentaCorriente destino = new CuentaCorriente("CC002", new BigDecimal("500.00"), new BigDecimal("3.00"));
        try {
            origen.transferir(destino, new BigDecimal("200.00"));
            fail("Debería haber lanzado BusinessException");
        } catch (BusinessException e) {
            assertEquals("Saldo insuficiente para transferir el importe 200.00.", e.getMessage());
        }
    }

    @Test
    public void testTransferirCorrecto() {
        CuentaCorriente origen = new CuentaCorriente("CC001", new BigDecimal("1000.00"), new BigDecimal("5.00"));
        CuentaCorriente destino = new CuentaCorriente("CC002", new BigDecimal("500.00"), new BigDecimal("3.00"));
        
        origen.transferir(destino, new BigDecimal("200.00"));
        
        assertEquals(new BigDecimal("800.00"), origen.getSaldo());
        assertEquals(new BigDecimal("700.00"), destino.getSaldo());
    }
}
