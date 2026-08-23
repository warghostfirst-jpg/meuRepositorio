package com.example.calculadoracdb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.calculadoracdb.ui.theme.CalculadoraCDBTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculadoraCDBTheme {
                CdbCalculatorScreen()
            }
        }
    }
}

private enum class TipoRentabilidade { POS_FIXADO, PRE_FIXADO }

private val formatoMoeda: NumberFormat =
    NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

private fun String.paraDoubleOuNulo(): Double? =
    replace(",", ".").toDoubleOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CdbCalculatorScreen() {
    var valorInvestido by rememberSaveable { mutableStateOf("1000") }
    var tipoRentabilidade by rememberSaveable { mutableStateOf(TipoRentabilidade.POS_FIXADO) }
    var percentualCdi by rememberSaveable { mutableStateOf("102") }
    var taxaCdi by rememberSaveable { mutableStateOf("13,90") }
    var taxaPrefixada by rememberSaveable { mutableStateOf("12") }
    var prazoQuantidade by rememberSaveable { mutableStateOf("12") }
    var unidadePrazo by rememberSaveable { mutableStateOf(UnidadePrazo.MESES) }
    var unidadeExpandida by remember { mutableStateOf(false) }

    var resultado by remember { mutableStateOf<ResultadoCdb?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var carregandoCdi by remember { mutableStateOf(false) }

    val cdiRateService = remember { CdiRateService() }
    val coroutineScope = rememberCoroutineScope()

    suspend fun atualizarTaxaCdi() {
        erro = null
        carregandoCdi = true
        cdiRateService.buscarTaxaCdiAnual()
            .onSuccess { taxaCdi = "%.2f".format(it).replace(".", ",") }
            .onFailure { erro = "Não foi possível obter a taxa CDI: ${it.message}" }
        carregandoCdi = false
    }

    LaunchedEffect(Unit) {
        atualizarTaxaCdi()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Calculadora de CDB") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = valorInvestido,
                onValueChange = { valorInvestido = it },
                label = { Text("Valor investido (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = tipoRentabilidade == TipoRentabilidade.POS_FIXADO,
                    onClick = { tipoRentabilidade = TipoRentabilidade.POS_FIXADO },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Pós-fixado") }
                SegmentedButton(
                    selected = tipoRentabilidade == TipoRentabilidade.PRE_FIXADO,
                    onClick = { tipoRentabilidade = TipoRentabilidade.PRE_FIXADO },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Pré-fixado") }
            }

            if (tipoRentabilidade == TipoRentabilidade.POS_FIXADO) {
                OutlinedTextField(
                    value = percentualCdi,
                    onValueChange = { percentualCdi = it },
                    label = { Text("% do CDI") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = taxaCdi,
                        onValueChange = { taxaCdi = it },
                        label = { Text("Taxa CDI ao ano (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    if (carregandoCdi) {
                        CircularProgressIndicator(modifier = Modifier.padding(horizontal = 16.dp))
                    } else {
                        TextButton(
                            onClick = {
                                coroutineScope.launch { atualizarTaxaCdi() }
                            }
                        ) {
                            Text("Atualizar")
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = taxaPrefixada,
                    onValueChange = { taxaPrefixada = it },
                    label = { Text("Taxa ao ano (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = prazoQuantidade,
                    onValueChange = { prazoQuantidade = it },
                    label = { Text("Prazo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                ExposedDropdownMenuBox(
                    expanded = unidadeExpandida,
                    onExpandedChange = { unidadeExpandida = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = unidadePrazo.rotulo(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidade") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unidadeExpandida) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = unidadeExpandida,
                        onDismissRequest = { unidadeExpandida = false }
                    ) {
                        UnidadePrazo.entries.forEach { unidade ->
                            DropdownMenuItem(
                                text = { Text(unidade.rotulo()) },
                                onClick = {
                                    unidadePrazo = unidade
                                    unidadeExpandida = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    erro = null
                    resultado = null
                    val principal = valorInvestido.paraDoubleOuNulo()
                    val prazoQtd = prazoQuantidade.toIntOrNull()
                    val taxaAnual = when (tipoRentabilidade) {
                        TipoRentabilidade.PRE_FIXADO -> taxaPrefixada.paraDoubleOuNulo()?.div(100.0)
                        TipoRentabilidade.POS_FIXADO -> {
                            val cdi = percentualCdi.paraDoubleOuNulo()
                            val taxa = taxaCdi.paraDoubleOuNulo()
                            if (cdi != null && taxa != null) taxaAnualPosFixado(cdi, taxa) else null
                        }
                    }

                    if (principal == null || principal <= 0.0) {
                        erro = "Informe um valor investido válido."
                    } else if (prazoQtd == null || prazoQtd <= 0) {
                        erro = "Informe um prazo válido."
                    } else if (taxaAnual == null || taxaAnual < 0.0) {
                        erro = "Informe uma taxa válida."
                    } else {
                        val prazoDias = unidadePrazo.paraDias(prazoQtd)
                        resultado = calcularCdb(principal, taxaAnual, prazoDias)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calcular")
            }

            erro?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            resultado?.let { ResultadoCard(it) }
        }
    }
}

private fun UnidadePrazo.rotulo(): String = when (this) {
    UnidadePrazo.DIAS -> "Dias"
    UnidadePrazo.MESES -> "Meses"
    UnidadePrazo.ANOS -> "Anos"
}

@Composable
private fun ResultadoCard(resultado: ResultadoCdb) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Resultado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            LinhaResultado("Valor bruto no vencimento", formatoMoeda.format(resultado.valorBruto))
            LinhaResultado("Rendimento bruto", formatoMoeda.format(resultado.rendimentoBruto))
            LinhaResultado("IOF", formatoMoeda.format(resultado.iofValor))
            LinhaResultado(
                "IR (${(resultado.aliquotaIr * 100).let { "%.1f".format(it) }}%)",
                formatoMoeda.format(resultado.irValor)
            )

            HorizontalDivider()

            LinhaResultado(
                "Valor líquido final",
                formatoMoeda.format(resultado.valorLiquido),
                enfase = true
            )
            LinhaResultado(
                "Rentabilidade líquida",
                "%.2f%%".format(resultado.rentabilidadeLiquidaPercentual),
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
        Text(rotulo, style = if (enfase) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium)
        Text(
            valor,
            style = if (enfase) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (enfase) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CdbCalculatorScreenPreview() {
    CalculadoraCDBTheme {
        CdbCalculatorScreen()
    }
}
