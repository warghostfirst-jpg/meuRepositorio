package com.example.calculadoracdb

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.calculadoracdb.ui.theme.CalculadoraCDBTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val PREFERENCIAS_APP = "calculadora_cdb_preferencias"
private const val CHAVE_TEMA_ESCURO = "tema_escuro"
private const val CHAVE_COR_FUNDO = "cor_fundo"
private const val CHAVE_COR_TEXTO = "cor_texto"
private const val CHAVE_COR_BORDA = "cor_borda"
private const val CHAVE_IMPOSTO_PERSONALIZAR_IR = "imposto_personalizar_ir"
private const val CHAVE_IMPOSTO_ALIQUOTA_IR = "imposto_aliquota_ir"
private const val CHAVE_IMPOSTO_PERSONALIZAR_IOF = "imposto_personalizar_iof"
private const val CHAVE_IMPOSTO_ALIQUOTA_IOF = "imposto_aliquota_iof"

private data class CoresPersonalizadas(
    val fundo: Color? = null,
    val texto: Color? = null,
    val borda: Color? = null
)

/** Por padrão, IR e IOF seguem a tabela regressiva automática conforme o prazo; ambos podem ser customizados. */
private const val ALIQUOTA_IR_PADRAO_PERCENTUAL = 15.0

private data class ConfiguracoesImposto(
    val personalizarIr: Boolean = false,
    val aliquotaIrPercentual: String = "%.0f".format(ALIQUOTA_IR_PADRAO_PERCENTUAL),
    val personalizarIof: Boolean = false,
    val aliquotaIofPercentual: String = "0"
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
            var configuracoesImposto by remember {
                mutableStateOf(
                    ConfiguracoesImposto(
                        personalizarIr = preferencias.getBoolean(CHAVE_IMPOSTO_PERSONALIZAR_IR, false),
                        aliquotaIrPercentual = preferencias.getFloat(
                            CHAVE_IMPOSTO_ALIQUOTA_IR,
                            ALIQUOTA_IR_PADRAO_PERCENTUAL.toFloat()
                        ).toString().replace(".", ","),
                        personalizarIof = preferencias.getBoolean(CHAVE_IMPOSTO_PERSONALIZAR_IOF, false),
                        aliquotaIofPercentual = preferencias.getFloat(CHAVE_IMPOSTO_ALIQUOTA_IOF, 0.0f)
                            .toString().replace(".", ",")
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
                    },
                    configuracoesImposto = configuracoesImposto,
                    onConfiguracoesImpostoChange = { novasConfiguracoes ->
                        configuracoesImposto = novasConfiguracoes
                        preferencias.edit()
                            .putBoolean(CHAVE_IMPOSTO_PERSONALIZAR_IR, novasConfiguracoes.personalizarIr)
                            .putFloat(
                                CHAVE_IMPOSTO_ALIQUOTA_IR,
                                (novasConfiguracoes.aliquotaIrPercentual.paraDoubleOuNulo() ?: 0.0).toFloat()
                            )
                            .putBoolean(CHAVE_IMPOSTO_PERSONALIZAR_IOF, novasConfiguracoes.personalizarIof)
                            .putFloat(
                                CHAVE_IMPOSTO_ALIQUOTA_IOF,
                                (novasConfiguracoes.aliquotaIofPercentual.paraDoubleOuNulo() ?: 0.0).toFloat()
                            )
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

private val formatoData: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val formatoDataHora: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

private fun String.paraDoubleOuNulo(): Double? =
    replace(",", ".").toDoubleOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CdbCalculatorScreen(
    temaEscuro: Boolean,
    onAlternarTema: () -> Unit,
    coresPersonalizadas: CoresPersonalizadas,
    onCoresPersonalizadasChange: (CoresPersonalizadas) -> Unit,
    configuracoesImposto: ConfiguracoesImposto,
    onConfiguracoesImpostoChange: (ConfiguracoesImposto) -> Unit
) {
    var valorInvestido by rememberSaveable { mutableStateOf("1000") }
    var aporteMensal by rememberSaveable { mutableStateOf("0") }
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
    var mostrarHistorico by remember { mutableStateOf(false) }
    var mostrarConfiguracoes by remember { mutableStateOf(false) }

    val contexto = LocalContext.current
    val historicoRepositorio = remember {
        HistoricoRepositorio(contexto.getSharedPreferences(PREFERENCIAS_APP, Context.MODE_PRIVATE))
    }
    var historico by remember { mutableStateOf(historicoRepositorio.carregarTodos()) }

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
                                    Icon(Icons.Filled.Palette, contentDescription = "Abrir personalização de cores")
                                }
                            },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                        contentDescription = "Logo Calculadora CDB",
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.size(10.dp))
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
                                }
                            },
                            actions = {
                                IconButton(onClick = { mostrarConfiguracoes = true }) {
                                    Icon(Icons.Filled.Settings, contentDescription = "Configurações de IR e IOF")
                                }
                                IconButton(onClick = { mostrarHistorico = true }) {
                                    Icon(Icons.Filled.History, contentDescription = "Ver histórico de simulações")
                                }
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
                            onValorChange = { valorInvestido = it },
                            aporteMensal = aporteMensal,
                            onAporteMensalChange = { aporteMensal = it }
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
                                val aporteMensalValor =
                                    if (aporteMensal.isBlank()) 0.0 else aporteMensal.paraDoubleOuNulo()
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
                                } else if (aporteMensalValor == null || aporteMensalValor < 0.0) {
                                    erro = "Informe um aporte mensal válido."
                                } else if (prazoQtd == null || prazoQtd <= 0) {
                                    erro = "Informe um prazo válido."
                                } else if (taxaAnual == null || taxaAnual < 0.0) {
                                    erro = "Informe uma taxa válida."
                                } else {
                                    val prazoDias = unidadePrazo.paraDias(prazoQtd)
                                    val novoResultado = calcularCdb(
                                        principal = principal,
                                        aporteMensal = aporteMensalValor,
                                        taxaAnual = taxaAnual,
                                        prazoDias = prazoDias,
                                        aliquotaIrPersonalizada = if (configuracoesImposto.personalizarIr) {
                                            (configuracoesImposto.aliquotaIrPercentual.paraDoubleOuNulo() ?: 0.0) / 100.0
                                        } else null,
                                        percentualIofPersonalizado = if (configuracoesImposto.personalizarIof) {
                                            (configuracoesImposto.aliquotaIofPercentual.paraDoubleOuNulo() ?: 0.0) / 100.0
                                        } else null
                                    )
                                    resultado = novoResultado

                                    val descricaoTaxa = when (tipoRentabilidade) {
                                        TipoRentabilidade.POS_FIXADO -> "$percentualCdi% do CDI"
                                        TipoRentabilidade.PRE_FIXADO -> "$taxaPrefixada% a.a."
                                    }
                                    val descricaoEntrada = buildString {
                                        append(formatoMoeda.format(principal))
                                        if (aporteMensalValor > 0.0) {
                                            append(" + ").append(formatoMoeda.format(aporteMensalValor)).append("/mês")
                                        }
                                        append(" · ").append(prazoQtd).append(" ")
                                        append(unidadePrazo.rotulo().lowercase())
                                        append(" · ").append(descricaoTaxa)
                                    }
                                    val item = ItemHistorico(
                                        id = System.currentTimeMillis(),
                                        dataHoraCalculo = LocalDateTime.now(),
                                        descricaoEntrada = descricaoEntrada,
                                        resultado = novoResultado
                                    )
                                    historicoRepositorio.adicionar(item)
                                    historico = listOf(item) + historico
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
                    }
                }
            }
        }

        resultado?.let { valor ->
            ModalResultado(resultado = valor, onFechar = { resultado = null })
        }

        if (mostrarConfiguracoes) {
            ModalConfiguracoes(
                configuracoes = configuracoesImposto,
                onConfiguracoesChange = onConfiguracoesImpostoChange,
                onFechar = { mostrarConfiguracoes = false }
            )
        }

        if (mostrarHistorico) {
            ModalHistorico(
                itens = historico,
                onFechar = { mostrarHistorico = false },
                onVerItem = { item ->
                    resultado = item.resultado
                    mostrarHistorico = false
                },
                onRemoverItem = { item ->
                    historicoRepositorio.remover(item.id)
                    historico = historico.filterNot { it.id == item.id }
                },
                onLimparTudo = {
                    historicoRepositorio.limparTudo()
                    historico = emptyList()
                }
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalConfiguracoes(
    configuracoes: ConfiguracoesImposto,
    onConfiguracoesChange: (ConfiguracoesImposto) -> Unit,
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
                        Icon(Icons.Filled.Close, contentDescription = "Fechar configurações")
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
                        leadingIcon = { Icon(Icons.Filled.Percent, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
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
                        leadingIcon = { Icon(Icons.Filled.Percent, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Deixe o IOF em 0% se o resgate ocorrer 30 dias ou mais após a aplicação.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
private fun SecaoValorInvestido(
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
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = aporteMensal,
            onValueChange = onAporteMensalChange,
            label = { Text("Aporte mensal (R$)") },
            leadingIcon = { Icon(Icons.Filled.Savings, contentDescription = null) },
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
    UnidadePrazo.DIAS -> "Dias úteis"
    UnidadePrazo.MESES -> "Meses"
    UnidadePrazo.ANOS -> "Anos"
}

@Composable
private fun BotaoInfo(
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
private fun BotaoInfoDiasUteis() {
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
private fun BotaoInfoTaxaIr() {
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
private fun BotaoInfoTaxaIof() {
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

@Composable
private fun ModalResultado(resultado: ResultadoCdb, onFechar: () -> Unit) {
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
private fun ModalHistorico(
    itens: List<ItemHistorico>,
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
private fun CartaoItemHistorico(item: ItemHistorico, onClick: () -> Unit, onRemover: () -> Unit) {
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
                Text(
                    "Líquido: ${formatoMoeda.format(item.resultado.valorLiquido)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
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

@Preview(showBackground = true)
@Composable
private fun CdbCalculatorScreenPreview() {
    var temaEscuro by remember { mutableStateOf(false) }
    var coresPersonalizadas by remember { mutableStateOf(CoresPersonalizadas()) }
    var configuracoesImposto by remember { mutableStateOf(ConfiguracoesImposto()) }
    CalculadoraCDBTheme(darkTheme = temaEscuro) {
        CdbCalculatorScreen(
            temaEscuro = temaEscuro,
            onAlternarTema = { temaEscuro = !temaEscuro },
            coresPersonalizadas = coresPersonalizadas,
            onCoresPersonalizadasChange = { coresPersonalizadas = it },
            configuracoesImposto = configuracoesImposto,
            onConfiguracoesImpostoChange = { configuracoesImposto = it }
        )
    }
}
