package com.example.calculadoracdb

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
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
