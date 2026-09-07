package com.example.calculadoracdb

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val formatoMoeda: NumberFormat =
    NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())

internal val formatoData: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
internal val formatoDataHora: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

internal fun String.paraDoubleOuNulo(): Double? =
    replace(",", ".").toDoubleOrNull()

private fun agruparMilhares(numero: Long): String {
    val texto = numero.toString()
    val builder = StringBuilder()
    for ((indice, caractere) in texto.withIndex()) {
        val posicaoDireita = texto.length - indice
        if (indice != 0 && posicaoDireita % 3 == 0) builder.append('.')
        builder.append(caractere)
    }
    return builder.toString()
}

/** Formata uma sequência de dígitos (interpretados como centavos) em "R$ 1.234,56". */
internal fun formatarValorMonetario(digitos: String): String {
    val valorCentavos = digitos.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val reais = valorCentavos / 100
    val centavos = valorCentavos % 100
    return "R$ ${agruparMilhares(reais)},${centavos.toString().padStart(2, '0')}"
}

/** Converte os dígitos brutos (interpretados como centavos) para Double. */
internal fun String.valorMonetarioParaDouble(): Double {
    val valorCentavos = filter { it.isDigit() }.toLongOrNull() ?: 0L
    return valorCentavos / 100.0
}

/**
 * Exibe os dígitos brutos digitados (ex.: "1000") como "R$ 1.000,00", mantendo o valor
 * do campo em texto puro (sem "R$", pontos ou vírgula) para evitar que a própria máscara
 * seja relida como dígito digitado.
 */
internal class MascaraValorMonetarioTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.filter { it.isDigit() }
        val formatado = formatarValorMonetario(digitos)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formatado.length
            override fun transformedToOriginal(offset: Int): Int = digitos.length
        }
        return TransformedText(AnnotatedString(formatado), offsetMapping)
    }
}

/** Formata uma sequência de dígitos (interpretados como centésimos) em "10,33%". */
internal fun formatarPercentual(digitos: String): String {
    val valorCentesimos = digitos.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val inteiro = valorCentesimos / 100
    val decimal = valorCentesimos % 100
    return "${agruparMilhares(inteiro)},${decimal.toString().padStart(2, '0')}%"
}

/** Converte os dígitos brutos (interpretados como centésimos) para Double. */
internal fun String.percentualParaDouble(): Double {
    val valorCentesimos = filter { it.isDigit() }.toLongOrNull() ?: 0L
    return valorCentesimos / 100.0
}

/** Mesma ideia de [MascaraValorMonetarioTransformation], mas exibindo "10,33%". */
internal class MascaraPercentualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text.filter { it.isDigit() }
        val formatado = formatarPercentual(digitos)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formatado.length
            override fun transformedToOriginal(offset: Int): Int = digitos.length
        }
        return TransformedText(AnnotatedString(formatado), offsetMapping)
    }
}
