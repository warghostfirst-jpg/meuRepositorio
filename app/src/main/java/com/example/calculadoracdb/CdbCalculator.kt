package com.example.calculadoracdb

import kotlin.math.pow

/**
 * Tabela de IOF regressivo (Decreto 6.306/2007), aplicada apenas sobre o
 * rendimento quando o resgate ocorre antes de 30 dias corridos da aplicação.
 */
private val TABELA_IOF = intArrayOf(
    96, 93, 90, 86, 83, 80, 76, 73, 70, 66,
    63, 60, 56, 53, 50, 46, 43, 40, 36, 33,
    30, 26, 23, 20, 16, 13, 10, 6, 3
)

/** Alíquota de Imposto de Renda regressiva sobre o rendimento (Lei 11.033/2004). */
fun aliquotaIr(prazoDias: Int): Double = when {
    prazoDias <= 180 -> 0.225
    prazoDias <= 360 -> 0.20
    prazoDias <= 720 -> 0.175
    else -> 0.15
}

/** Percentual do rendimento retido como IOF; zero a partir do 30º dia. */
fun percentualIof(prazoDias: Int): Double = when {
    prazoDias >= 30 -> 0.0
    prazoDias <= 0 -> 1.0
    else -> TABELA_IOF[prazoDias - 1] / 100.0
}

data class ResultadoCdb(
    val valorBruto: Double,
    val rendimentoBruto: Double,
    val iofValor: Double,
    val aliquotaIr: Double,
    val irValor: Double,
    val valorLiquido: Double,
    val rendimentoLiquido: Double,
    val rentabilidadeLiquidaPercentual: Double
)

/**
 * @param taxaAnual taxa efetiva ao ano, em decimal (ex.: 0.12 para 12% a.a.)
 * @param prazoDias prazo total da aplicação em dias corridos
 */
fun calcularCdb(principal: Double, taxaAnual: Double, prazoDias: Int): ResultadoCdb {
    val taxaDiaria = (1 + taxaAnual).pow(1.0 / 365.0) - 1
    val valorBruto = principal * (1 + taxaDiaria).pow(prazoDias.toDouble())
    val rendimentoBruto = valorBruto - principal

    val iofValor = (rendimentoBruto * percentualIof(prazoDias)).coerceAtLeast(0.0)
    val rendimentoAposIof = rendimentoBruto - iofValor

    val aliquota = aliquotaIr(prazoDias)
    val irValor = (rendimentoAposIof * aliquota).coerceAtLeast(0.0)

    val rendimentoLiquido = rendimentoAposIof - irValor
    val valorLiquido = principal + rendimentoLiquido
    val rentabilidadeLiquidaPercentual = if (principal != 0.0) rendimentoLiquido / principal * 100.0 else 0.0

    return ResultadoCdb(
        valorBruto = valorBruto,
        rendimentoBruto = rendimentoBruto,
        iofValor = iofValor,
        aliquotaIr = aliquota,
        irValor = irValor,
        valorLiquido = valorLiquido,
        rendimentoLiquido = rendimentoLiquido,
        rentabilidadeLiquidaPercentual = rentabilidadeLiquidaPercentual
    )
}

/** Taxa efetiva anual (decimal) de um CDB pós-fixado indexado a um % do CDI. */
fun taxaAnualPosFixado(percentualCdi: Double, cdiAnual: Double): Double =
    (cdiAnual / 100.0) * (percentualCdi / 100.0)

enum class UnidadePrazo(val diasPorUnidade: Int) {
    DIAS(1),
    MESES(30),
    ANOS(365)
}

fun UnidadePrazo.paraDias(quantidade: Int): Int = quantidade * diasPorUnidade
