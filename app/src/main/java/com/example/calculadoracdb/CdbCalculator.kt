package com.example.calculadoracdb

import java.time.DayOfWeek
import java.time.LocalDate
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
    val totalAportado: Double,
    val valorBruto: Double,
    val rendimentoBruto: Double,
    val iofValor: Double,
    val aliquotaIr: Double,
    val irValor: Double,
    val valorLiquido: Double,
    val rendimentoLiquido: Double,
    val rentabilidadeLiquidaPercentual: Double,
    val dataFimInvestimento: LocalDate,
    val evolucao: List<PontoEvolucao> = emptyList()
)

/**
 * Um ponto da evolução do investimento ao longo do prazo. Os valores líquidos consideram
 * IOF e IR como se o resgate ocorresse naquele dia (regime regressivo, salvo alíquotas
 * personalizadas).
 */
data class PontoEvolucao(
    val diaUtil: Int,
    val data: LocalDate,
    val totalAportado: Double,
    val valorBruto: Double,
    val rendimentoBruto: Double,
    val iofValor: Double,
    val aliquotaIr: Double,
    val irValor: Double,
    val valorLiquido: Double,
    val rendimentoLiquido: Double,
    val rentabilidadeLiquidaPercentual: Double
)

/** Número máximo de pontos gerados para o gráfico de evolução, para manter o traçado legível. */
private const val MAX_PONTOS_EVOLUCAO = 48

/** Gera os pontos de evolução de todos os atributos calculados, do início ao vencimento. */
private fun gerarEvolucao(
    principal: Double,
    aporteMensal: Double,
    taxaDiaria: Double,
    prazoDias: Int,
    dataInicio: LocalDate,
    aliquotaIrPersonalizada: Double?,
    percentualIofPersonalizado: Double?
): List<PontoEvolucao> {
    if (prazoDias <= 0) return emptyList()

    val passoDias = maxOf(1, (prazoDias + MAX_PONTOS_EVOLUCAO - 1) / MAX_PONTOS_EVOLUCAO)
    val diasDosPontos = buildList {
        var dia = 0
        while (dia < prazoDias) {
            add(dia)
            dia += passoDias
        }
        add(prazoDias)
    }

    return diasDosPontos.map { dia ->
        val valorBrutoPrincipal = principal * (1 + taxaDiaria).pow(dia.toDouble())
        val numeroDeAportesAteODia = dia / DIAS_UTEIS_POR_MES
        var valorBrutoAportes = 0.0
        for (mes in 1..numeroDeAportesAteODia) {
            val diasRestantes = dia - mes * DIAS_UTEIS_POR_MES
            valorBrutoAportes += aporteMensal * (1 + taxaDiaria).pow(diasRestantes.toDouble())
        }
        val totalAportadoNoDia = principal + aporteMensal * numeroDeAportesAteODia
        val valorBrutoNoDia = valorBrutoPrincipal + valorBrutoAportes
        val rendimentoBrutoNoDia = valorBrutoNoDia - totalAportadoNoDia

        val percentualIofNoDia = percentualIofPersonalizado ?: percentualIof(dia)
        val iofValorNoDia = (rendimentoBrutoNoDia * percentualIofNoDia).coerceAtLeast(0.0)
        val rendimentoAposIofNoDia = rendimentoBrutoNoDia - iofValorNoDia

        val aliquotaIrNoDia = aliquotaIrPersonalizada ?: aliquotaIr(dia)
        val irValorNoDia = (rendimentoAposIofNoDia * aliquotaIrNoDia).coerceAtLeast(0.0)

        val rendimentoLiquidoNoDia = rendimentoAposIofNoDia - irValorNoDia
        val valorLiquidoNoDia = totalAportadoNoDia + rendimentoLiquidoNoDia
        val rentabilidadeLiquidaNoDia =
            if (totalAportadoNoDia != 0.0) rendimentoLiquidoNoDia / totalAportadoNoDia * 100.0 else 0.0

        PontoEvolucao(
            diaUtil = dia,
            data = adicionarDiasUteis(dataInicio, dia),
            totalAportado = totalAportadoNoDia,
            valorBruto = valorBrutoNoDia,
            rendimentoBruto = rendimentoBrutoNoDia,
            iofValor = iofValorNoDia,
            aliquotaIr = aliquotaIrNoDia,
            irValor = irValorNoDia,
            valorLiquido = valorLiquidoNoDia,
            rendimentoLiquido = rendimentoLiquidoNoDia,
            rentabilidadeLiquidaPercentual = rentabilidadeLiquidaNoDia
        )
    }
}

/** Base de dias úteis usada pelo mercado para capitalização de CDB/CDI. */
const val DIAS_UTEIS_POR_ANO = 252

private val DIAS_UTEIS_POR_MES = UnidadePrazo.MESES.diasPorUnidade

/**
 * @param principal valor investido no início da aplicação
 * @param aporteMensal valor aportado ao final de cada mês (21 dias úteis) até o vencimento
 * @param taxaAnual taxa efetiva ao ano, em decimal (ex.: 0.12 para 12% a.a.)
 * @param prazoDias prazo total da aplicação em dias úteis (o CDB rende apenas
 * em dias úteis; a taxa anual é capitalizada na base de 252 dias úteis)
 * @param dataInicio data de início da aplicação, usada para calcular a data de vencimento
 */
fun calcularCdb(
    principal: Double,
    aporteMensal: Double,
    taxaAnual: Double,
    prazoDias: Int,
    dataInicio: LocalDate = LocalDate.now(),
    aliquotaIrPersonalizada: Double? = null,
    percentualIofPersonalizado: Double? = null
): ResultadoCdb {
    val taxaDiaria = (1 + taxaAnual).pow(1.0 / DIAS_UTEIS_POR_ANO) - 1

    val valorBrutoPrincipal = principal * (1 + taxaDiaria).pow(prazoDias.toDouble())

    val numeroDeAportes = prazoDias / DIAS_UTEIS_POR_MES
    var valorBrutoAportes = 0.0
    for (mes in 1..numeroDeAportes) {
        val diasRestantes = prazoDias - mes * DIAS_UTEIS_POR_MES
        valorBrutoAportes += aporteMensal * (1 + taxaDiaria).pow(diasRestantes.toDouble())
    }

    val valorBruto = valorBrutoPrincipal + valorBrutoAportes
    val totalAportado = principal + aporteMensal * numeroDeAportes
    val rendimentoBruto = valorBruto - totalAportado

    val percentualIofAplicado = percentualIofPersonalizado ?: percentualIof(prazoDias)
    val iofValor = (rendimentoBruto * percentualIofAplicado).coerceAtLeast(0.0)
    val rendimentoAposIof = rendimentoBruto - iofValor

    val aliquota = aliquotaIrPersonalizada ?: aliquotaIr(prazoDias)
    val irValor = (rendimentoAposIof * aliquota).coerceAtLeast(0.0)

    val rendimentoLiquido = rendimentoAposIof - irValor
    val valorLiquido = totalAportado + rendimentoLiquido
    val rentabilidadeLiquidaPercentual =
        if (totalAportado != 0.0) rendimentoLiquido / totalAportado * 100.0 else 0.0

    return ResultadoCdb(
        totalAportado = totalAportado,
        valorBruto = valorBruto,
        rendimentoBruto = rendimentoBruto,
        iofValor = iofValor,
        aliquotaIr = aliquota,
        irValor = irValor,
        valorLiquido = valorLiquido,
        rendimentoLiquido = rendimentoLiquido,
        rentabilidadeLiquidaPercentual = rentabilidadeLiquidaPercentual,
        dataFimInvestimento = adicionarDiasUteis(dataInicio, prazoDias),
        evolucao = gerarEvolucao(
            principal,
            aporteMensal,
            taxaDiaria,
            prazoDias,
            dataInicio,
            aliquotaIrPersonalizada,
            percentualIofPersonalizado
        )
    )
}

/** Soma [diasUteis] dias úteis (sem contar sábados e domingos) a [dataInicio]. */
fun adicionarDiasUteis(dataInicio: LocalDate, diasUteis: Int): LocalDate {
    var data = dataInicio
    var diasRestantes = diasUteis
    while (diasRestantes > 0) {
        data = data.plusDays(1)
        if (data.dayOfWeek != DayOfWeek.SATURDAY && data.dayOfWeek != DayOfWeek.SUNDAY) {
            diasRestantes--
        }
    }
    return data
}

/** Taxa efetiva anual (decimal) de um CDB pós-fixado indexado a um % do CDI. */
fun taxaAnualPosFixado(percentualCdi: Double, cdiAnual: Double): Double =
    (cdiAnual / 100.0) * (percentualCdi / 100.0)

/**
 * Unidades de prazo convertidas em dias úteis, seguindo a convenção do
 * mercado financeiro para ativos que rendem em dias úteis (ex.: CDI/CDB):
 * 1 mês = 21 dias úteis e 1 ano = 252 dias úteis.
 */
enum class UnidadePrazo(val diasPorUnidade: Int) {
    DIAS(1),
    MESES(21),
    ANOS(DIAS_UTEIS_POR_ANO)
}

fun UnidadePrazo.paraDias(quantidade: Int): Int = quantidade * diasPorUnidade
