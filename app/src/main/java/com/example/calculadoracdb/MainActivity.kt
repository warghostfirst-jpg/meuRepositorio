package com.example.calculadoracdb

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.calculadoracdb.ui.theme.CalculadoraCDBTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private const val PREFERENCIAS_APP = "calculadora_cdb_preferencias"
private const val CHAVE_TEMA_ESCURO = "tema_escuro"
private const val CHAVE_COR_FUNDO = "cor_fundo"
private const val CHAVE_COR_TEXTO = "cor_texto"
private const val CHAVE_COR_BORDA = "cor_borda"

private data class CoresPersonalizadas(
    val fundo: Color? = null,
    val texto: Color? = null,
    val borda: Color? = null
)

private fun SharedPreferences.corSalva(chave: String): Color? =
    if (contains(chave)) Color(getInt(chave, 0)) else null

private fun SharedPreferences.Editor.salvarCor(chave: String, cor: Color?): SharedPreferences.Editor =
    if (cor != null) putInt(chave, cor.toArgb()) else remove(chave)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val contexto = LocalContext.current
            val preferencias = remember {
                contexto.getSharedPreferences(PREFERENCIAS_APP, Context.MODE_PRIVATE)
            }
            val temaEscuroDoSistema = isSystemInDarkTheme()
            var temaEscuro by rememberSaveable {
                mutableStateOf(preferencias.getBoolean(CHAVE_TEMA_ESCURO, temaEscuroDoSistema))
            }
            var coresPersonalizadas by remember {
                mutableStateOf(
                    CoresPersonalizadas(
                        fundo = preferencias.corSalva(CHAVE_COR_FUNDO),
                        texto = preferencias.corSalva(CHAVE_COR_TEXTO),
                        borda = preferencias.corSalva(CHAVE_COR_BORDA)
                    )
                )
            }

            CalculadoraCDBTheme(darkTheme = temaEscuro) {
                CdbCalculatorScreen(
                    temaEscuro = temaEscuro,
                    onAlternarTema = {
                        temaEscuro = !temaEscuro
                        preferencias.edit().putBoolean(CHAVE_TEMA_ESCURO, temaEscuro).apply()
                    },
                    coresPersonalizadas = coresPersonalizadas,
                    onCoresPersonalizadasChange = { novasCores ->
                        coresPersonalizadas = novasCores
                        preferencias.edit()
                            .salvarCor(CHAVE_COR_FUNDO, novasCores.fundo)
                            .salvarCor(CHAVE_COR_TEXTO, novasCores.texto)
                            .salvarCor(CHAVE_COR_BORDA, novasCores.borda)
                            .apply()
                    }
                )
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
private fun CdbCalculatorScreen(
    temaEscuro: Boolean,
    onAlternarTema: () -> Unit,
    coresPersonalizadas: CoresPersonalizadas,
    onCoresPersonalizadasChange: (CoresPersonalizadas) -> Unit
) {
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val corSchemeBase = MaterialTheme.colorScheme
    val corSchemePersonalizado = corSchemeBase.copy(
        background = coresPersonalizadas.fundo ?: corSchemeBase.background,
        surface = coresPersonalizadas.fundo ?: corSchemeBase.surface,
        onBackground = coresPersonalizadas.texto ?: corSchemeBase.onBackground,
        onSurface = coresPersonalizadas.texto ?: corSchemeBase.onSurface,
        outline = coresPersonalizadas.borda ?: corSchemeBase.outline
    )

    MaterialTheme(colorScheme = corSchemePersonalizado, typography = MaterialTheme.typography) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    drawerContentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    MenuPersonalizarCores(
                        coresPersonalizadas = coresPersonalizadas,
                        onCoresPersonalizadasChange = onCoresPersonalizadasChange,
                        onFechar = { coroutineScope.launch { drawerState.close() } }
                    )
                }
            }
        ) {
            val fundoDegrade = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.background
                ),
                endY = 900f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fundoDegrade)
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Abrir menu de personalização")
                                }
                            },
                            title = {
                                Column {
                                    Text(
                                        "Calculadora CDB",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Simule seu investimento em segundos",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = onAlternarTema) {
                                    Icon(
                                        if (temaEscuro) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                        contentDescription = if (temaEscuro) "Ativar tema claro" else "Ativar tema escuro"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SecaoValorInvestido(
                            valorInvestido = valorInvestido,
                            onValorChange = { valorInvestido = it }
                        )

                        SecaoRentabilidade(
                            tipoRentabilidade = tipoRentabilidade,
                            onTipoChange = { tipoRentabilidade = it },
                            percentualCdi = percentualCdi,
                            onPercentualCdiChange = { percentualCdi = it },
                            taxaCdi = taxaCdi,
                            onTaxaCdiChange = { taxaCdi = it },
                            taxaPrefixada = taxaPrefixada,
                            onTaxaPrefixadaChange = { taxaPrefixada = it },
                            carregandoCdi = carregandoCdi,
                            onAtualizarCdi = { coroutineScope.launch { atualizarTaxaCdi() } }
                        )

                        SecaoPrazo(
                            prazoQuantidade = prazoQuantidade,
                            onPrazoQuantidadeChange = { prazoQuantidade = it },
                            unidadePrazo = unidadePrazo,
                            onUnidadeChange = { unidadePrazo = it },
                            unidadeExpandida = unidadeExpandida,
                            onUnidadeExpandidaChange = { unidadeExpandida = it }
                        )

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
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(Icons.Filled.Calculate, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Calcular rendimento", style = MaterialTheme.typography.titleMedium)
                        }

                        erro?.let { mensagem -> CartaoErro(mensagem) }

                        resultado?.let { valor ->
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                ResultadoHero(valor)
                                ResultadoDetalhes(valor)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val paletaCores: List<Color?> = listOf(
    null,
    Color(0xFFFFFFFF),
    Color(0xFF121212),
    Color(0xFFF5F5F5),
    Color(0xFF37474F),
    Color(0xFF0B7A3D),
    Color(0xFF1565C0),
    Color(0xFF6A1B9A),
    Color(0xFFC62828),
    Color(0xFFE65100),
    Color(0xFFF9A825),
    Color(0xFF00838F)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuPersonalizarCores(
    coresPersonalizadas: CoresPersonalizadas,
    onCoresPersonalizadasChange: (CoresPersonalizadas) -> Unit,
    onFechar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Personalizar aparência", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onFechar) {
                Icon(Icons.Filled.Close, contentDescription = "Fechar menu")
            }
        }

        SeletorDeCor(
            titulo = "Cor de fundo",
            corSelecionada = coresPersonalizadas.fundo,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(fundo = it)) }
        )

        SeletorDeCor(
            titulo = "Cor do texto",
            corSelecionada = coresPersonalizadas.texto,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(texto = it)) }
        )

        SeletorDeCor(
            titulo = "Cor das bordas",
            corSelecionada = coresPersonalizadas.borda,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(borda = it)) }
        )

        HorizontalDivider()

        TextButton(
            onClick = { onCoresPersonalizadasChange(CoresPersonalizadas()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restaurar cores padrão")
        }
    }
}

@Composable
private fun SeletorDeCor(
    titulo: String,
    corSelecionada: Color?,
    aoSelecionar: (Color?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            paletaCores.forEach { cor ->
                AmostraDeCor(
                    cor = cor,
                    selecionada = cor == corSelecionada,
                    onClick = { aoSelecionar(cor) }
                )
            }
        }
    }
}

@Composable
private fun AmostraDeCor(cor: Color?, selecionada: Boolean, onClick: () -> Unit) {
    val corExibida = cor ?: MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(corExibida)
            .border(
                width = if (selecionada) 3.dp else 1.dp,
                color = if (selecionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val corDoIcone = if (corExibida.luminance() > 0.5f) Color.Black else Color.White
        if (cor == null) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Padrão",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (selecionada) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = corDoIcone
            )
        }
    }
}

@Composable
private fun CartaoSecao(
    titulo: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
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
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecaoValorInvestido(
    valorInvestido: String,
    onValorChange: (String) -> Unit
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
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecaoRentabilidade(
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
                modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.weight(1f)
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecaoPrazo(
    prazoQuantidade: String,
    onPrazoQuantidadeChange: (String) -> Unit,
    unidadePrazo: UnidadePrazo,
    onUnidadeChange: (UnidadePrazo) -> Unit,
    unidadeExpandida: Boolean,
    onUnidadeExpandidaChange: (Boolean) -> Unit
) {
    CartaoSecao(titulo = "Prazo da aplicação", icone = Icons.Filled.CalendarMonth) {
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
                modifier = Modifier.weight(1f)
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
private fun CartaoErro(mensagem: String) {
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

private fun UnidadePrazo.rotulo(): String = when (this) {
    UnidadePrazo.DIAS -> "Dias"
    UnidadePrazo.MESES -> "Meses"
    UnidadePrazo.ANOS -> "Anos"
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

@Preview(showBackground = true)
@Composable
private fun CdbCalculatorScreenPreview() {
    var temaEscuro by remember { mutableStateOf(false) }
    var coresPersonalizadas by remember { mutableStateOf(CoresPersonalizadas()) }
    CalculadoraCDBTheme(darkTheme = temaEscuro) {
        CdbCalculatorScreen(
            temaEscuro = temaEscuro,
            onAlternarTema = { temaEscuro = !temaEscuro },
            coresPersonalizadas = coresPersonalizadas,
            onCoresPersonalizadasChange = { coresPersonalizadas = it }
        )
    }
}
