package com.example.calculadoracdb

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoMesAno: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM/yy", Locale.Builder().setLanguage("pt").setRegion("BR").build())

private const val ALTURA_GRAFICO_DP = 220
private const val LARGURA_EIXO_Y_DP = 64
private const val QUANTIDADE_LINHAS_GUIA = 4
private const val QUANTIDADE_MAXIMA_ROTULOS_X = 5

private enum class ModoGrafico(val rotulo: String) {
    VALORES("Valores (R$)"),
    PERCENTUAL("Rentabilidade (%)")
}

private enum class SerieValor(val rotulo: String, val padraoAtiva: Boolean, val valor: (PontoEvolucao) -> Double) {
    CAPITAL("Capital (bruto)", true, { it.valorBruto }),
    APORTADO("Total aportado", true, { it.totalAportado }),
    RENDIMENTO_BRUTO("Rendimento bruto", true, { it.rendimentoBruto }),
    VALOR_LIQUIDO("Valor líquido", false, { it.valorLiquido }),
    RENDIMENTO_LIQUIDO("Rendimento líquido", false, { it.rendimentoLiquido }),
    IOF("IOF", false, { it.iofValor }),
    IR("IR", false, { it.irValor })
}

private enum class SeriePercentual(val rotulo: String, val padraoAtiva: Boolean, val valor: (PontoEvolucao) -> Double) {
    RENTABILIDADE_LIQUIDA("Rentabilidade líquida", true, { it.rentabilidadeLiquidaPercentual }),
    ALIQUOTA_IR("Alíquota de IR", false, { it.aliquotaIr * 100.0 })
}

@Composable
private fun corDaSerieValor(serie: SerieValor): Color = when (serie) {
    SerieValor.CAPITAL -> MaterialTheme.colorScheme.primary
    SerieValor.APORTADO -> MaterialTheme.colorScheme.tertiary
    SerieValor.RENDIMENTO_BRUTO -> MaterialTheme.colorScheme.secondary
    SerieValor.VALOR_LIQUIDO -> Color(0xFF00BFA5)
    SerieValor.RENDIMENTO_LIQUIDO -> Color(0xFF7C4DFF)
    SerieValor.IOF -> MaterialTheme.colorScheme.error
    SerieValor.IR -> Color(0xFFFFB300)
}

@Composable
private fun corDaSeriePercentual(serie: SeriePercentual): Color = when (serie) {
    SeriePercentual.RENTABILIDADE_LIQUIDA -> MaterialTheme.colorScheme.primary
    SeriePercentual.ALIQUOTA_IR -> Color(0xFFFFB300)
}

@Composable
internal fun ModalGraficoEvolucao(resultado: ResultadoCdb, onFechar: () -> Unit) {
    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Evolução da simulação",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onFechar) {
                        Icon(Icons.Filled.Close, contentDescription = "Fechar gráfico")
                    }
                }

                if (resultado.evolucao.size < 2) {
                    Text(
                        "Sem dados de evolução para esta simulação. Refaça o cálculo para " +
                            "visualizar o gráfico.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    GraficoEvolucao(resultado.evolucao)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GraficoEvolucao(pontos: List<PontoEvolucao>) {
    var modo by remember { mutableStateOf(ModoGrafico.VALORES) }
    var seriesValorAtivas by remember {
        mutableStateOf(SerieValor.entries.filter { it.padraoAtiva }.toSet())
    }
    var seriesPercentualAtivas by remember {
        mutableStateOf(SeriePercentual.entries.filter { it.padraoAtiva }.toSet())
    }

    val corEixo = MaterialTheme.colorScheme.onSurfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ModoGrafico.entries.forEachIndexed { indice, opcao ->
                SegmentedButton(
                    selected = modo == opcao,
                    onClick = { modo = opcao },
                    shape = SegmentedButtonDefaults.itemShape(index = indice, count = ModoGrafico.entries.size)
                ) { Text(opcao.rotulo) }
            }
        }

        when (modo) {
            ModoGrafico.VALORES -> {
                val seriesComCores = SerieValor.entries.associateWith { corDaSerieValor(it) }
                val valorMaximo = pontos.maxOf { it.valorBruto }.coerceAtLeast(1.0)
                val valoresEixoY = (QUANTIDADE_LINHAS_GUIA downTo 0).map { i -> valorMaximo * i / QUANTIDADE_LINHAS_GUIA }

                EixosEGrafico(
                    pontos = pontos,
                    valorMaximo = valorMaximo,
                    rotulosEixoY = valoresEixoY.map(::formatarValorCompacto),
                    corEixo = corEixo,
                    linhas = SerieValor.entries
                        .filter { it in seriesValorAtivas }
                        .map { serie -> Triple(serie.rotulo, seriesComCores.getValue(serie), serie.valor) }
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    SerieValor.entries.forEach { serie ->
                        ChipLegenda(
                            cor = seriesComCores.getValue(serie),
                            rotulo = serie.rotulo,
                            ativo = serie in seriesValorAtivas,
                            onToggle = {
                                seriesValorAtivas = if (serie in seriesValorAtivas) {
                                    seriesValorAtivas - serie
                                } else {
                                    seriesValorAtivas + serie
                                }
                            }
                        )
                    }
                }
            }

            ModoGrafico.PERCENTUAL -> {
                val seriesComCores = SeriePercentual.entries.associateWith { corDaSeriePercentual(it) }
                val valorMaximo = pontos.maxOf { p -> SeriePercentual.entries.maxOf { it.valor(p) } }
                    .coerceAtLeast(1.0)
                val valoresEixoY = (QUANTIDADE_LINHAS_GUIA downTo 0).map { i -> valorMaximo * i / QUANTIDADE_LINHAS_GUIA }

                EixosEGrafico(
                    pontos = pontos,
                    valorMaximo = valorMaximo,
                    rotulosEixoY = valoresEixoY.map { "%.1f%%".format(it) },
                    corEixo = corEixo,
                    linhas = SeriePercentual.entries
                        .filter { it in seriesPercentualAtivas }
                        .map { serie -> Triple(serie.rotulo, seriesComCores.getValue(serie), serie.valor) }
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    SeriePercentual.entries.forEach { serie ->
                        ChipLegenda(
                            cor = seriesComCores.getValue(serie),
                            rotulo = serie.rotulo,
                            ativo = serie in seriesPercentualAtivas,
                            onToggle = {
                                seriesPercentualAtivas = if (serie in seriesPercentualAtivas) {
                                    seriesPercentualAtivas - serie
                                } else {
                                    seriesPercentualAtivas + serie
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EixosEGrafico(
    pontos: List<PontoEvolucao>,
    valorMaximo: Double,
    rotulosEixoY: List<String>,
    corEixo: Color,
    linhas: List<Triple<String, Color, (PontoEvolucao) -> Double>>
) {
    val quantidadeRotulosX = minOf(QUANTIDADE_MAXIMA_ROTULOS_X, pontos.size)
    val indicesRotulosX = if (quantidadeRotulosX <= 1) {
        listOf(0)
    } else {
        (0 until quantidadeRotulosX).map { i -> i * (pontos.size - 1) / (quantidadeRotulosX - 1) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .width(LARGURA_EIXO_Y_DP.dp)
                    .height(ALTURA_GRAFICO_DP.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                rotulosEixoY.forEach { rotulo ->
                    Text(rotulo, style = MaterialTheme.typography.labelSmall, color = corEixo)
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(ALTURA_GRAFICO_DP.dp)
                    .padding(start = 8.dp)
            ) {
                val alturaUtil = size.height
                val larguraUtil = size.width

                fun x(indice: Int) = larguraUtil * indice / (pontos.size - 1).toFloat()
                fun y(valor: Double) = alturaUtil - (alturaUtil * (valor / valorMaximo)).toFloat()

                repeat(QUANTIDADE_LINHAS_GUIA + 1) { i ->
                    val yGuia = alturaUtil * i / QUANTIDADE_LINHAS_GUIA
                    drawLine(
                        color = corEixo.copy(alpha = 0.15f),
                        start = Offset(0f, yGuia),
                        end = Offset(larguraUtil, yGuia),
                        strokeWidth = 1f
                    )
                }

                linhas.forEach { (_, cor, valorDoPonto) ->
                    for (i in 0 until pontos.size - 1) {
                        drawLine(
                            color = cor,
                            start = Offset(x(i), y(valorDoPonto(pontos[i]))),
                            end = Offset(x(i + 1), y(valorDoPonto(pontos[i + 1]))),
                            strokeWidth = 5f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(LARGURA_EIXO_Y_DP.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                indicesRotulosX.forEach { indice ->
                    Text(
                        pontos[indice].data.format(formatoMesAno),
                        style = MaterialTheme.typography.labelSmall,
                        color = corEixo
                    )
                }
            }
        }
    }
}

private fun formatarValorCompacto(valor: Double): String = when {
    valor >= 1_000_000 -> "%.1f mi".format(valor / 1_000_000).replace(".", ",")
    valor >= 1_000 -> "%.1f mil".format(valor / 1_000).replace(".", ",")
    else -> formatoMoeda.format(valor)
}

@Composable
private fun ChipLegenda(cor: Color, rotulo: String, ativo: Boolean, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(50),
        color = if (ativo) cor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (ativo) cor else MaterialTheme.colorScheme.outline)
            )
            Text(
                rotulo,
                style = MaterialTheme.typography.labelMedium,
                color = if (ativo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
