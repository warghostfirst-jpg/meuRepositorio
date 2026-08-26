package com.example.calculadoracdb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CdbCalculatorTest {

    @Test
    fun `taxaAnualPosFixado aplica percentual do CDI`() {
        val taxa = taxaAnualPosFixado(percentualCdi = 110.0, cdiAnual = 10.0)
        assertEquals(0.11, taxa, 1e-9)
    }

    @Test
    fun `aliquotaIr segue tabela regressiva`() {
        assertEquals(0.225, aliquotaIr(180), 1e-9)
        assertEquals(0.20, aliquotaIr(181), 1e-9)
        assertEquals(0.175, aliquotaIr(361), 1e-9)
        assertEquals(0.15, aliquotaIr(721), 1e-9)
    }

    @Test
    fun `percentualIof zera a partir do 30o dia`() {
        assertEquals(0.0, percentualIof(30), 1e-9)
        assertEquals(0.03, percentualIof(29), 1e-9)
        assertEquals(0.96, percentualIof(1), 1e-9)
    }

    @Test
    fun `calcularCdb gera rendimento positivo para taxa positiva e prazo acima de 30 dias`() {
        val resultado = calcularCdb(principal = 1000.0, aporteMensal = 0.0, taxaAnual = 0.12, prazoDias = 365)

        assertEquals(1000.0, resultado.totalAportado, 1e-9)
        assertTrue(resultado.valorBruto > 1000.0)
        assertTrue(resultado.rendimentoBruto > 0.0)
        assertEquals(0.0, resultado.iofValor, 1e-9)
        assertEquals(0.175, resultado.aliquotaIr, 1e-9)
        assertTrue(resultado.valorLiquido < resultado.valorBruto)
        assertTrue(resultado.valorLiquido > 1000.0)
    }

    @Test
    fun `calcularCdb aplica IOF em resgates antes de 30 dias`() {
        val resultado = calcularCdb(principal = 1000.0, aporteMensal = 0.0, taxaAnual = 0.12, prazoDias = 10)

        assertTrue(resultado.iofValor > 0.0)
        assertTrue(resultado.rendimentoLiquido < resultado.rendimentoBruto)
    }

    @Test
    fun `calcularCdb soma aportes mensais ao total aportado e ao valor bruto`() {
        val resultado = calcularCdb(principal = 1000.0, aporteMensal = 100.0, taxaAnual = 0.12, prazoDias = 42)

        assertEquals(1200.0, resultado.totalAportado, 1e-9)
        assertTrue(resultado.valorBruto > resultado.totalAportado)
    }
}
