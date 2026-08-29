package com.example.calculadoracdb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun BotaoInfo(
    descricao: String,
    titulo: String,
    conteudo: @Composable ColumnScope.() -> Unit
) {
    var mostrarInfo by remember { mutableStateOf(false) }

    IconButton(onClick = { mostrarInfo = true }) {
        Icon(
            Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = descricao,
            modifier = Modifier.size(20.dp)
        )
    }

    if (mostrarInfo) {
        AlertDialog(
            onDismissRequest = { mostrarInfo = false },
            confirmButton = {
                TextButton(onClick = { mostrarInfo = false }) { Text("Entendi") }
            },
            icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null) },
            title = { Text(titulo) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    content = conteudo
                )
            }
        )
    }
}

@Composable
internal fun BotaoInfoDiasUteis() {
    BotaoInfo(
        descricao = "Como o prazo é contado em dias úteis",
        titulo = "Como o prazo é contado"
    ) {
        Text(
            "O CDB rende apenas em dias úteis. Este app segue a convenção do " +
                "mercado financeiro para a contagem do prazo:\n\n" +
                "• 1 mês = 21 dias úteis\n" +
                "• 1 ano = 252 dias úteis\n\n" +
                "A taxa anual informada é capitalizada nessa mesma base de 252 dias úteis."
        )
    }
}

@Composable
internal fun BotaoInfoTaxaIr() {
    BotaoInfo(
        descricao = "Como funciona a tabela regressiva de IR",
        titulo = "Tabela regressiva de IR"
    ) {
        Text(
            "O Imposto de Renda incide sobre o rendimento do CDB conforme o prazo da " +
                "aplicação, segundo a tabela regressiva (Lei 11.033/2004):"
        )
        Text("• Até 180 dias: 22,5%")
        Text("• De 181 a 360 dias: 20%")
        Text("• De 361 a 720 dias: 17,5%")
        Text("• Acima de 720 dias: 15%")
    }
}

@Composable
internal fun BotaoInfoTaxaIof() {
    BotaoInfo(
        descricao = "Como funciona a tabela regressiva de IOF",
        titulo = "Tabela regressiva de IOF"
    ) {
        Text(
            "O IOF incide sobre o rendimento apenas quando o resgate ocorre antes de 30 " +
                "dias corridos da aplicação (Decreto 6.306/2007):"
        )
        for (dia in 1..29) {
            Text("• Dia $dia: ${"%.0f".format(percentualIof(dia) * 100)}%")
        }
        Text("• A partir do dia 30: isento (0%)")
    }
}
