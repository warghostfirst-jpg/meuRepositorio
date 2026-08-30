package com.example.calculadoracdb

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Rola o campo para ficar visível acima do teclado. Necessário porque o scroll automático
 * do Compose ao focar um campo é calculado antes da animação do teclado terminar, então sem
 * isso o campo focado pode continuar coberto quando o teclado aparece.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Modifier.trazerParaVisivelAoFocar(): Modifier {
    val requisitante = remember { BringIntoViewRequester() }
    var focado by remember { mutableStateOf(false) }
    val tecladoVisivel = WindowInsets.isImeVisible

    LaunchedEffect(tecladoVisivel, focado) {
        if (tecladoVisivel && focado) {
            requisitante.bringIntoView()
        }
    }

    return this
        .bringIntoViewRequester(requisitante)
        .onFocusEvent { focado = it.isFocused }
}

@Composable
internal fun CartaoSecao(
    titulo: String,
    icone: ImageVector,
    acaoTitulo: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            icone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                acaoTitulo?.invoke()
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SecaoValorInvestido(
    valorInvestido: String,
    onValorChange: (String) -> Unit,
    aporteMensal: String,
    onAporteMensalChange: (String) -> Unit
) {
    CartaoSecao(titulo = "Valor investido", icone = Icons.Filled.Savings) {
        OutlinedTextField(
            value = valorInvestido,
            onValueChange = onValorChange,
            label = { Text("Valor investido (R$)") },
            leadingIcon = { Icon(Icons.Filled.AttachMoney, contentDescription = null) },
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().trazerParaVisivelAoFocar()
        )
        OutlinedTextField(
            value = aporteMensal,
            onValueChange = onAporteMensalChange,
            label = { Text("Aporte mensal (R$)") },
            leadingIcon = { Icon(Icons.Filled.Savings, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().trazerParaVisivelAoFocar()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SecaoRentabilidade(
    tipoRentabilidade: TipoRentabilidade,
    onTipoChange: (TipoRentabilidade) -> Unit,
    percentualCdi: String,
    onPercentualCdiChange: (String) -> Unit,
    taxaCdi: String,
    onTaxaCdiChange: (String) -> Unit,
    taxaPrefixada: String,
    onTaxaPrefixadaChange: (String) -> Unit,
    carregandoCdi: Boolean,
    onAtualizarCdi: () -> Unit
) {
    CartaoSecao(titulo = "Tipo de rentabilidade", icone = Icons.AutoMirrored.Filled.TrendingUp) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = tipoRentabilidade == TipoRentabilidade.POS_FIXADO,
                onClick = { onTipoChange(TipoRentabilidade.POS_FIXADO) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Pós-fixado") }
            SegmentedButton(
                selected = tipoRentabilidade == TipoRentabilidade.PRE_FIXADO,
                onClick = { onTipoChange(TipoRentabilidade.PRE_FIXADO) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Pré-fixado") }
        }

        if (tipoRentabilidade == TipoRentabilidade.POS_FIXADO) {
            OutlinedTextField(
                value = percentualCdi,
                onValueChange = onPercentualCdiChange,
                label = { Text("% do CDI") },
                leadingIcon = { Icon(Icons.Filled.Percent, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().trazerParaVisivelAoFocar()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = taxaCdi,
                    onValueChange = onTaxaCdiChange,
                    label = { Text("Taxa CDI ao ano (%)") },
                    leadingIcon = { Icon(Icons.Filled.Payments, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).trazerParaVisivelAoFocar()
                )
                if (carregandoCdi) {
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else {
                    FilledIconButton(
                        onClick = onAtualizarCdi,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Atualizar taxa CDI")
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = taxaPrefixada,
                onValueChange = onTaxaPrefixadaChange,
                label = { Text("Taxa ao ano (%)") },
                leadingIcon = { Icon(Icons.Filled.Percent, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().trazerParaVisivelAoFocar()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SecaoPrazo(
    prazoQuantidade: String,
    onPrazoQuantidadeChange: (String) -> Unit,
    unidadePrazo: UnidadePrazo,
    onUnidadeChange: (UnidadePrazo) -> Unit,
    unidadeExpandida: Boolean,
    onUnidadeExpandidaChange: (Boolean) -> Unit
) {
    CartaoSecao(
        titulo = "Prazo da aplicação",
        icone = Icons.Filled.CalendarMonth,
        acaoTitulo = { BotaoInfoDiasUteis() }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = prazoQuantidade,
                onValueChange = onPrazoQuantidadeChange,
                label = { Text("Prazo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).trazerParaVisivelAoFocar()
            )
            ExposedDropdownMenuBox(
                expanded = unidadeExpandida,
                onExpandedChange = onUnidadeExpandidaChange,
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = unidadePrazo.rotulo(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unidade") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unidadeExpandida) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = unidadeExpandida,
                    onDismissRequest = { onUnidadeExpandidaChange(false) }
                ) {
                    UnidadePrazo.entries.forEach { unidade ->
                        DropdownMenuItem(
                            text = { Text(unidade.rotulo()) },
                            onClick = {
                                onUnidadeChange(unidade)
                                onUnidadeExpandidaChange(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CartaoErro(mensagem: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                mensagem,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
