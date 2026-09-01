package com.example.calculadoracdb

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.calculadoracdb.ui.theme.CalculadoraCDBTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val contexto = LocalContext.current
            val preferencias = remember {
                contexto.getSharedPreferences(PREFERENCIAS_APP, Context.MODE_PRIVATE)
            }
            var podeCarregarAnuncios by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                solicitarConsentimentoEInicializarAnuncios(this@MainActivity) { pronto ->
                    podeCarregarAnuncios = pronto
                    if (pronto) GerenciadorInterstitial.carregar(this@MainActivity)
                }
            }
            val gerenciadorCompras = remember { GerenciadorCompras(contexto) }
            DisposableEffect(Unit) {
                gerenciadorCompras.iniciar()
                onDispose { gerenciadorCompras.encerrar() }
            }
            val anunciosRemovidos by gerenciadorCompras.anunciosRemovidos.collectAsState()
            val precoRemoverAnuncios by gerenciadorCompras.precoFormatado.collectAsState()
            val temaEscuroDoSistema = isSystemInDarkTheme()
            var temaEscuro by rememberSaveable {
                mutableStateOf(preferencias.getBoolean(CHAVE_TEMA_ESCURO, temaEscuroDoSistema))
            }
            var coresPersonalizadas by remember {
                mutableStateOf(
                    CoresPersonalizadas(
                        primaria = preferencias.corSalva(CHAVE_COR_PRIMARIA),
                        secundaria = preferencias.corSalva(CHAVE_COR_SECUNDARIA),
                        fundo = preferencias.corSalva(CHAVE_COR_FUNDO),
                        superficie = preferencias.corSalva(CHAVE_COR_SUPERFICIE),
                        letras = preferencias.corSalva(CHAVE_COR_LETRAS),
                        numeros = preferencias.corSalva(CHAVE_COR_NUMEROS),
                        icones = preferencias.corSalva(CHAVE_COR_ICONES),
                        borda = preferencias.corSalva(CHAVE_COR_BORDA),
                        erro = preferencias.corSalva(CHAVE_COR_ERRO)
                    )
                )
            }
            var coresGrafico by remember {
                mutableStateOf(
                    CoresGrafico(
                        capital = preferencias.corSalva(CHAVE_COR_GRAFICO_CAPITAL),
                        aportado = preferencias.corSalva(CHAVE_COR_GRAFICO_APORTADO),
                        rendimentoBruto = preferencias.corSalva(CHAVE_COR_GRAFICO_RENDIMENTO_BRUTO),
                        valorLiquido = preferencias.corSalva(CHAVE_COR_GRAFICO_VALOR_LIQUIDO),
                        rendimentoLiquido = preferencias.corSalva(CHAVE_COR_GRAFICO_RENDIMENTO_LIQUIDO),
                        iof = preferencias.corSalva(CHAVE_COR_GRAFICO_IOF),
                        ir = preferencias.corSalva(CHAVE_COR_GRAFICO_IR),
                        rentabilidadeLiquida = preferencias.corSalva(CHAVE_COR_GRAFICO_RENTABILIDADE_LIQUIDA),
                        aliquotaIr = preferencias.corSalva(CHAVE_COR_GRAFICO_ALIQUOTA_IR)
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
            var camposHistorico by remember {
                mutableStateOf(
                    CamposHistorico(
                        dataFimInvestimento = preferencias.getBoolean(CHAVE_HISTORICO_MOSTRAR_DATA_VENCIMENTO, false),
                        totalAportado = preferencias.getBoolean(CHAVE_HISTORICO_MOSTRAR_TOTAL_APORTADO, false),
                        valorBruto = preferencias.getBoolean(CHAVE_HISTORICO_MOSTRAR_VALOR_BRUTO, false),
                        rendimentoBruto = preferencias.getBoolean(CHAVE_HISTORICO_MOSTRAR_RENDIMENTO_BRUTO, false),
                        iofValor = preferencias.getBoolean(CHAVE_HISTORICO_MOSTRAR_IOF, false),
                        aliquotaIr = preferencias.getBoolean(CHAVE_HISTORICO_MOSTRAR_ALIQUOTA_IR, false),
                        irValor = preferencias.getBoolean(CHAVE_HISTORICO_MOSTRAR_IR, false),
                        rendimentoLiquido = preferencias.getBoolean(CHAVE_HISTORICO_MOSTRAR_RENDIMENTO_LIQUIDO, false),
                        valorLiquido = preferencias.getBoolean(CHAVE_HISTORICO_MOSTRAR_VALOR_LIQUIDO, true),
                        rentabilidadeLiquidaPercentual = preferencias.getBoolean(CHAVE_HISTORICO_MOSTRAR_RENTABILIDADE, false)
                    )
                )
            }

            CalculadoraCDBTheme(darkTheme = temaEscuro) {
                CdbCalculatorScreen(
                    temaEscuro = temaEscuro,
                    anunciosRemovidos = anunciosRemovidos,
                    podeCarregarAnuncios = podeCarregarAnuncios,
                    precoRemoverAnuncios = precoRemoverAnuncios,
                    onComprarRemoverAnuncios = { gerenciadorCompras.comprar(this@MainActivity) },
                    onAlternarTema = {
                        temaEscuro = !temaEscuro
                        preferencias.edit().putBoolean(CHAVE_TEMA_ESCURO, temaEscuro).apply()
                    },
                    coresPersonalizadas = coresPersonalizadas,
                    onCoresPersonalizadasChange = { novasCores ->
                        coresPersonalizadas = novasCores
                        preferencias.edit()
                            .salvarCor(CHAVE_COR_PRIMARIA, novasCores.primaria)
                            .salvarCor(CHAVE_COR_SECUNDARIA, novasCores.secundaria)
                            .salvarCor(CHAVE_COR_FUNDO, novasCores.fundo)
                            .salvarCor(CHAVE_COR_SUPERFICIE, novasCores.superficie)
                            .salvarCor(CHAVE_COR_LETRAS, novasCores.letras)
                            .salvarCor(CHAVE_COR_NUMEROS, novasCores.numeros)
                            .salvarCor(CHAVE_COR_ICONES, novasCores.icones)
                            .salvarCor(CHAVE_COR_BORDA, novasCores.borda)
                            .salvarCor(CHAVE_COR_ERRO, novasCores.erro)
                            .apply()
                    },
                    coresGrafico = coresGrafico,
                    onCoresGraficoChange = { novasCores ->
                        coresGrafico = novasCores
                        preferencias.edit()
                            .salvarCor(CHAVE_COR_GRAFICO_CAPITAL, novasCores.capital)
                            .salvarCor(CHAVE_COR_GRAFICO_APORTADO, novasCores.aportado)
                            .salvarCor(CHAVE_COR_GRAFICO_RENDIMENTO_BRUTO, novasCores.rendimentoBruto)
                            .salvarCor(CHAVE_COR_GRAFICO_VALOR_LIQUIDO, novasCores.valorLiquido)
                            .salvarCor(CHAVE_COR_GRAFICO_RENDIMENTO_LIQUIDO, novasCores.rendimentoLiquido)
                            .salvarCor(CHAVE_COR_GRAFICO_IOF, novasCores.iof)
                            .salvarCor(CHAVE_COR_GRAFICO_IR, novasCores.ir)
                            .salvarCor(CHAVE_COR_GRAFICO_RENTABILIDADE_LIQUIDA, novasCores.rentabilidadeLiquida)
                            .salvarCor(CHAVE_COR_GRAFICO_ALIQUOTA_IR, novasCores.aliquotaIr)
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
                    },
                    camposHistorico = camposHistorico,
                    onCamposHistoricoChange = { novosCampos ->
                        camposHistorico = novosCampos
                        preferencias.edit()
                            .putBoolean(CHAVE_HISTORICO_MOSTRAR_DATA_VENCIMENTO, novosCampos.dataFimInvestimento)
                            .putBoolean(CHAVE_HISTORICO_MOSTRAR_TOTAL_APORTADO, novosCampos.totalAportado)
                            .putBoolean(CHAVE_HISTORICO_MOSTRAR_VALOR_BRUTO, novosCampos.valorBruto)
                            .putBoolean(CHAVE_HISTORICO_MOSTRAR_RENDIMENTO_BRUTO, novosCampos.rendimentoBruto)
                            .putBoolean(CHAVE_HISTORICO_MOSTRAR_IOF, novosCampos.iofValor)
                            .putBoolean(CHAVE_HISTORICO_MOSTRAR_ALIQUOTA_IR, novosCampos.aliquotaIr)
                            .putBoolean(CHAVE_HISTORICO_MOSTRAR_IR, novosCampos.irValor)
                            .putBoolean(CHAVE_HISTORICO_MOSTRAR_RENDIMENTO_LIQUIDO, novosCampos.rendimentoLiquido)
                            .putBoolean(CHAVE_HISTORICO_MOSTRAR_VALOR_LIQUIDO, novosCampos.valorLiquido)
                            .putBoolean(CHAVE_HISTORICO_MOSTRAR_RENTABILIDADE, novosCampos.rentabilidadeLiquidaPercentual)
                            .apply()
                    }
                )
            }
        }
    }
}
