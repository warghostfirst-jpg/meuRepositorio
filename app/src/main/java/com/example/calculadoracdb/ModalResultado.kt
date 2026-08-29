package com.example.calculadoracdb

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun ModalResultado(resultado: ResultadoCdb, onFechar: () -> Unit) {
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
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Resultado da simulação",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onFechar) {
                        Icon(Icons.Filled.Close, contentDescription = "Fechar resultado")
                    }
                }

                ResultadoHero(resultado)
                ResultadoDetalhes(resultado)
            }
        }
    }
}

@Composable
private fun ResultadoHero(resultado: ResultadoCdb) {
    val degrade = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(degrade)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Valor líquido no vencimento",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            Text(
                formatoMoeda.format(resultado.valorLiquido),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(4.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "%.2f%% líquido no período".format(resultado.rentabilidadeLiquidaPercentual),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultadoDetalhes(resultado: ResultadoCdb) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Detalhamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            LinhaResultado("Data de vencimento", resultado.dataFimInvestimento.format(formatoData))
            LinhaResultado("Total aportado", formatoMoeda.format(resultado.totalAportado))
            LinhaResultado("Valor bruto no vencimento", formatoMoeda.format(resultado.valorBruto))
            LinhaResultado("Rendimento bruto", formatoMoeda.format(resultado.rendimentoBruto))
            LinhaResultado("IOF", formatoMoeda.format(resultado.iofValor))
            LinhaResultado(
                "IR (${(resultado.aliquotaIr * 100).let { "%.1f".format(it) }}%)",
                formatoMoeda.format(resultado.irValor)
            )

            HorizontalDivider()

            LinhaResultado(
                "Rendimento líquido",
                formatoMoeda.format(resultado.rendimentoLiquido),
                enfase = true
            )
        }
    }
}

@Composable
private fun LinhaResultado(rotulo: String, valor: String, enfase: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            rotulo,
            style = if (enfase) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            color = if (enfase) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            valor,
            style = if (enfase) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (enfase) FontWeight.Bold else FontWeight.Normal,
            color = if (enfase) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
