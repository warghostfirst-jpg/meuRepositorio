package com.example.calculadoracdb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModalConfiguracoes(
    configuracoes: ConfiguracoesImposto,
    onConfiguracoesChange: (ConfiguracoesImposto) -> Unit,
    camposHistorico: CamposHistorico,
    onCamposHistoricoChange: (CamposHistorico) -> Unit,
    onFechar: () -> Unit
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
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configurações", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onFechar) {
                        Icon(Icons.Filled.Close, contentDescription = "Fechar configurações", tint = LocalCorIcones.current)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Personalizar taxa de IR",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Por padrão, o IR segue a tabela regressiva automaticamente conforme o prazo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    BotaoInfoTaxaIr()
                    Switch(
                        checked = configuracoes.personalizarIr,
                        onCheckedChange = { onConfiguracoesChange(configuracoes.copy(personalizarIr = it)) }
                    )
                }

                if (configuracoes.personalizarIr) {
                    OutlinedTextField(
                        value = configuracoes.aliquotaIrPercentual,
                        onValueChange = { texto ->
                            onConfiguracoesChange(configuracoes.copy(aliquotaIrPercentual = texto))
                        },
                        label = { Text("Alíquota de IR (%)") },
                        leadingIcon = { Icon(Icons.Filled.Percent, contentDescription = null, tint = LocalCorIcones.current) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LocalCorNumeros.current,
                            unfocusedTextColor = LocalCorNumeros.current
                        ),
                        modifier = Modifier.fillMaxWidth().trazerParaVisivelAoFocar()
                    )
                }

                HorizontalDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Personalizar taxa de IOF",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Por padrão, o IOF segue a tabela regressiva automaticamente conforme o prazo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    BotaoInfoTaxaIof()
                    Switch(
                        checked = configuracoes.personalizarIof,
                        onCheckedChange = { onConfiguracoesChange(configuracoes.copy(personalizarIof = it)) }
                    )
                }

                if (configuracoes.personalizarIof) {
                    OutlinedTextField(
                        value = configuracoes.aliquotaIofPercentual,
                        onValueChange = { texto ->
                            onConfiguracoesChange(configuracoes.copy(aliquotaIofPercentual = texto))
                        },
                        label = { Text("Alíquota de IOF (%)") },
                        leadingIcon = { Icon(Icons.Filled.Percent, contentDescription = null, tint = LocalCorIcones.current) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LocalCorNumeros.current,
                            unfocusedTextColor = LocalCorNumeros.current
                        ),
                        modifier = Modifier.fillMaxWidth().trazerParaVisivelAoFocar()
                    )
                    Text(
                        "Deixe o IOF em 0% se o resgate ocorrer 30 dias ou mais após a aplicação.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                Text(
                    "Campos exibidos no histórico",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Escolha quais campos do resultado da simulação aparecem nos cartões do histórico.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LinhaSwitchCampo(
                    titulo = "Data de vencimento",
                    marcado = camposHistorico.dataFimInvestimento,
                    aoAlterar = { onCamposHistoricoChange(camposHistorico.copy(dataFimInvestimento = it)) }
                )
                LinhaSwitchCampo(
                    titulo = "Total aportado",
                    marcado = camposHistorico.totalAportado,
                    aoAlterar = { onCamposHistoricoChange(camposHistorico.copy(totalAportado = it)) }
                )
                LinhaSwitchCampo(
                    titulo = "Valor bruto no vencimento",
                    marcado = camposHistorico.valorBruto,
                    aoAlterar = { onCamposHistoricoChange(camposHistorico.copy(valorBruto = it)) }
                )
                LinhaSwitchCampo(
                    titulo = "Rendimento bruto",
                    marcado = camposHistorico.rendimentoBruto,
                    aoAlterar = { onCamposHistoricoChange(camposHistorico.copy(rendimentoBruto = it)) }
                )
                LinhaSwitchCampo(
                    titulo = "IOF",
                    marcado = camposHistorico.iofValor,
                    aoAlterar = { onCamposHistoricoChange(camposHistorico.copy(iofValor = it)) }
                )
                LinhaSwitchCampo(
                    titulo = "Alíquota de IR",
                    marcado = camposHistorico.aliquotaIr,
                    aoAlterar = { onCamposHistoricoChange(camposHistorico.copy(aliquotaIr = it)) }
                )
                LinhaSwitchCampo(
                    titulo = "IR",
                    marcado = camposHistorico.irValor,
                    aoAlterar = { onCamposHistoricoChange(camposHistorico.copy(irValor = it)) }
                )
                LinhaSwitchCampo(
                    titulo = "Rendimento líquido",
                    marcado = camposHistorico.rendimentoLiquido,
                    aoAlterar = { onCamposHistoricoChange(camposHistorico.copy(rendimentoLiquido = it)) }
                )
                LinhaSwitchCampo(
                    titulo = "Valor líquido",
                    marcado = camposHistorico.valorLiquido,
                    aoAlterar = { onCamposHistoricoChange(camposHistorico.copy(valorLiquido = it)) }
                )
                LinhaSwitchCampo(
                    titulo = "Rentabilidade líquida",
                    marcado = camposHistorico.rentabilidadeLiquidaPercentual,
                    aoAlterar = { onCamposHistoricoChange(camposHistorico.copy(rentabilidadeLiquidaPercentual = it)) }
                )
            }
        }
    }
}

@Composable
private fun LinhaSwitchCampo(titulo: String, marcado: Boolean, aoAlterar: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(titulo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = marcado, onCheckedChange = aoAlterar)
    }
}
