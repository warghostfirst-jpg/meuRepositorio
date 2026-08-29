package com.example.calculadoracdb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun ModalHistorico(
    itens: List<ItemHistorico>,
    camposHistorico: CamposHistorico,
    onFechar: () -> Unit,
    onVerItem: (ItemHistorico) -> Unit,
    onRemoverItem: (ItemHistorico) -> Unit,
    onLimparTudo: () -> Unit
) {
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
                .fillMaxHeight(0.85f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Histórico de simulações",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onFechar) {
                        Icon(Icons.Filled.Close, contentDescription = "Fechar histórico")
                    }
                }

                if (itens.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Nenhuma simulação salva ainda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(itens, key = { it.id }) { item ->
                            CartaoItemHistorico(
                                item = item,
                                camposHistorico = camposHistorico,
                                onClick = { onVerItem(item) },
                                onRemover = { onRemoverItem(item) }
                            )
                        }
                    }

                    TextButton(
                        onClick = onLimparTudo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Limpar histórico")
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoItemHistorico(
    item: ItemHistorico,
    camposHistorico: CamposHistorico,
    onClick: () -> Unit,
    onRemover: () -> Unit
) {
    var mostrarGrafico by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    item.dataHoraCalculo.format(formatoDataHora),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    item.descricaoEntrada,
                    style = MaterialTheme.typography.bodyMedium
                )
                camposDoResultado(item.resultado, camposHistorico).forEach { (rotulo, valor) ->
                    Text(
                        "$rotulo: $valor",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = { mostrarGrafico = true }) {
                Icon(
                    Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = "Ver gráfico de evolução"
                )
            }
            IconButton(onClick = onRemover) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Remover do histórico",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (mostrarGrafico) {
        ModalGraficoEvolucao(resultado = item.resultado, onFechar = { mostrarGrafico = false })
    }
}

private fun camposDoResultado(resultado: ResultadoCdb, campos: CamposHistorico): List<Pair<String, String>> =
    buildList {
        if (campos.dataFimInvestimento) add("Vencimento" to resultado.dataFimInvestimento.format(formatoData))
        if (campos.totalAportado) add("Total aportado" to formatoMoeda.format(resultado.totalAportado))
        if (campos.valorBruto) add("Valor bruto" to formatoMoeda.format(resultado.valorBruto))
        if (campos.rendimentoBruto) add("Rendimento bruto" to formatoMoeda.format(resultado.rendimentoBruto))
        if (campos.iofValor) add("IOF" to formatoMoeda.format(resultado.iofValor))
        if (campos.aliquotaIr) add("Alíquota de IR" to "%.1f%%".format(resultado.aliquotaIr * 100))
        if (campos.irValor) add("IR" to formatoMoeda.format(resultado.irValor))
        if (campos.rendimentoLiquido) add("Rendimento líquido" to formatoMoeda.format(resultado.rendimentoLiquido))
        if (campos.valorLiquido) add("Líquido" to formatoMoeda.format(resultado.valorLiquido))
        if (campos.rentabilidadeLiquidaPercentual) {
            add("Rentabilidade líquida" to "%.2f%%".format(resultado.rentabilidadeLiquidaPercentual))
        }
    }
