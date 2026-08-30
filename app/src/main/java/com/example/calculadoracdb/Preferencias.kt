package com.example.calculadoracdb

import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

internal const val PREFERENCIAS_APP = "calculadora_cdb_preferencias"

internal const val CHAVE_TEMA_ESCURO = "tema_escuro"
internal const val CHAVE_COR_PRIMARIA = "cor_primaria"
internal const val CHAVE_COR_SECUNDARIA = "cor_secundaria"
internal const val CHAVE_COR_TERCIARIA = "cor_terciaria"
internal const val CHAVE_COR_FUNDO = "cor_fundo"
internal const val CHAVE_COR_SUPERFICIE = "cor_superficie"
internal const val CHAVE_COR_TEXTO = "cor_texto"
internal const val CHAVE_COR_BORDA = "cor_borda"
internal const val CHAVE_COR_ERRO = "cor_erro"
internal const val CHAVE_IMPOSTO_PERSONALIZAR_IR = "imposto_personalizar_ir"
internal const val CHAVE_IMPOSTO_ALIQUOTA_IR = "imposto_aliquota_ir"
internal const val CHAVE_IMPOSTO_PERSONALIZAR_IOF = "imposto_personalizar_iof"
internal const val CHAVE_IMPOSTO_ALIQUOTA_IOF = "imposto_aliquota_iof"
internal const val CHAVE_HISTORICO_MOSTRAR_DATA_VENCIMENTO = "historico_mostrar_data_vencimento"
internal const val CHAVE_HISTORICO_MOSTRAR_TOTAL_APORTADO = "historico_mostrar_total_aportado"
internal const val CHAVE_HISTORICO_MOSTRAR_VALOR_BRUTO = "historico_mostrar_valor_bruto"
internal const val CHAVE_HISTORICO_MOSTRAR_RENDIMENTO_BRUTO = "historico_mostrar_rendimento_bruto"
internal const val CHAVE_HISTORICO_MOSTRAR_IOF = "historico_mostrar_iof"
internal const val CHAVE_HISTORICO_MOSTRAR_ALIQUOTA_IR = "historico_mostrar_aliquota_ir"
internal const val CHAVE_HISTORICO_MOSTRAR_IR = "historico_mostrar_ir"
internal const val CHAVE_HISTORICO_MOSTRAR_VALOR_LIQUIDO = "historico_mostrar_valor_liquido"
internal const val CHAVE_HISTORICO_MOSTRAR_RENDIMENTO_LIQUIDO = "historico_mostrar_rendimento_liquido"
internal const val CHAVE_HISTORICO_MOSTRAR_RENTABILIDADE = "historico_mostrar_rentabilidade"

internal data class CoresPersonalizadas(
    val primaria: Color? = null,
    val secundaria: Color? = null,
    val terciaria: Color? = null,
    val fundo: Color? = null,
    val superficie: Color? = null,
    val texto: Color? = null,
    val borda: Color? = null,
    val erro: Color? = null
)

/** Cor de texto/ícone com bom contraste sobre [this], usada ao aplicar cores personalizadas. */
internal fun Color.corDeContraste(): Color = if (luminance() > 0.5f) Color.Black else Color.White

/** Por padrão, IR e IOF seguem a tabela regressiva automática conforme o prazo; ambos podem ser customizados. */
internal const val ALIQUOTA_IR_PADRAO_PERCENTUAL = 15.0

internal data class ConfiguracoesImposto(
    val personalizarIr: Boolean = false,
    val aliquotaIrPercentual: String = "%.0f".format(ALIQUOTA_IR_PADRAO_PERCENTUAL),
    val personalizarIof: Boolean = false,
    val aliquotaIofPercentual: String = "0"
)

/** Controla quais campos do resultado da simulação aparecem nos cartões do histórico. */
internal data class CamposHistorico(
    val dataFimInvestimento: Boolean = false,
    val totalAportado: Boolean = false,
    val valorBruto: Boolean = false,
    val rendimentoBruto: Boolean = false,
    val iofValor: Boolean = false,
    val aliquotaIr: Boolean = false,
    val irValor: Boolean = false,
    val rendimentoLiquido: Boolean = false,
    val valorLiquido: Boolean = true,
    val rentabilidadeLiquidaPercentual: Boolean = false
)

internal fun SharedPreferences.corSalva(chave: String): Color? =
    if (contains(chave)) Color(getInt(chave, 0)) else null

internal fun SharedPreferences.Editor.salvarCor(chave: String, cor: Color?): SharedPreferences.Editor =
    if (cor != null) putInt(chave, cor.toArgb()) else remove(chave)
