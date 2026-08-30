package com.example.calculadoracdb

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.calculadoracdb.ui.theme.CalculadoraCDBTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime

internal enum class TipoRentabilidade { POS_FIXADO, PRE_FIXADO }

internal fun UnidadePrazo.rotulo(): String = when (this) {
    UnidadePrazo.DIAS -> "Dias úteis"
    UnidadePrazo.MESES -> "Meses"
    UnidadePrazo.ANOS -> "Anos"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CdbCalculatorScreen(
    temaEscuro: Boolean,
    onAlternarTema: () -> Unit,
    coresPersonalizadas: CoresPersonalizadas,
    onCoresPersonalizadasChange: (CoresPersonalizadas) -> Unit,
    configuracoesImposto: ConfiguracoesImposto,
    onConfiguracoesImpostoChange: (ConfiguracoesImposto) -> Unit,
    camposHistorico: CamposHistorico,
    onCamposHistoricoChange: (CamposHistorico) -> Unit
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
        primary = coresPersonalizadas.primaria ?: corSchemeBase.primary,
        onPrimary = coresPersonalizadas.primaria?.corDeContraste() ?: corSchemeBase.onPrimary,
        primaryContainer = coresPersonalizadas.primaria ?: corSchemeBase.primaryContainer,
        onPrimaryContainer = coresPersonalizadas.primaria?.corDeContraste() ?: corSchemeBase.onPrimaryContainer,
        secondary = coresPersonalizadas.secundaria ?: corSchemeBase.secondary,
        onSecondary = coresPersonalizadas.secundaria?.corDeContraste() ?: corSchemeBase.onSecondary,
        secondaryContainer = coresPersonalizadas.secundaria ?: corSchemeBase.secondaryContainer,
        onSecondaryContainer = coresPersonalizadas.secundaria?.corDeContraste() ?: corSchemeBase.onSecondaryContainer,
        tertiary = coresPersonalizadas.terciaria ?: corSchemeBase.tertiary,
        onTertiary = coresPersonalizadas.terciaria?.corDeContraste() ?: corSchemeBase.onTertiary,
        tertiaryContainer = coresPersonalizadas.terciaria ?: corSchemeBase.tertiaryContainer,
        onTertiaryContainer = coresPersonalizadas.terciaria?.corDeContraste() ?: corSchemeBase.onTertiaryContainer,
        background = coresPersonalizadas.fundo ?: corSchemeBase.background,
        onBackground = coresPersonalizadas.texto ?: corSchemeBase.onBackground,
        surface = coresPersonalizadas.superficie ?: corSchemeBase.surface,
        onSurface = coresPersonalizadas.texto ?: corSchemeBase.onSurface,
        surfaceVariant = coresPersonalizadas.superficie ?: corSchemeBase.surfaceVariant,
        onSurfaceVariant = coresPersonalizadas.texto ?: corSchemeBase.onSurfaceVariant,
        outline = coresPersonalizadas.borda ?: corSchemeBase.outline,
        error = coresPersonalizadas.erro ?: corSchemeBase.error,
        onError = coresPersonalizadas.erro?.corDeContraste() ?: corSchemeBase.onError,
        errorContainer = coresPersonalizadas.erro ?: corSchemeBase.errorContainer,
        onErrorContainer = coresPersonalizadas.erro?.corDeContraste() ?: corSchemeBase.onErrorContainer
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
                                    Spacer(modifier = Modifier.size(10.dp))
                                    Column {
                                        Text(
                                            "Calculadora CDB",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
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
                            .imePadding()
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
                camposHistorico = camposHistorico,
                onCamposHistoricoChange = onCamposHistoricoChange,
                onFechar = { mostrarConfiguracoes = false }
            )
        }

        if (mostrarHistorico) {
            ModalHistorico(
                itens = historico,
                camposHistorico = camposHistorico,
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

@Preview(showBackground = true)
@Composable
private fun CdbCalculatorScreenPreview() {
    var temaEscuro by remember { mutableStateOf(false) }
    var coresPersonalizadas by remember { mutableStateOf(CoresPersonalizadas()) }
    var configuracoesImposto by remember { mutableStateOf(ConfiguracoesImposto()) }
    var camposHistorico by remember { mutableStateOf(CamposHistorico()) }
    CalculadoraCDBTheme(darkTheme = temaEscuro) {
        CdbCalculatorScreen(
            temaEscuro = temaEscuro,
            onAlternarTema = { temaEscuro = !temaEscuro },
            coresPersonalizadas = coresPersonalizadas,
            onCoresPersonalizadasChange = { coresPersonalizadas = it },
            configuracoesImposto = configuracoesImposto,
            onConfiguracoesImpostoChange = { configuracoesImposto = it },
            camposHistorico = camposHistorico,
            onCamposHistoricoChange = { camposHistorico = it }
        )
    }
}
