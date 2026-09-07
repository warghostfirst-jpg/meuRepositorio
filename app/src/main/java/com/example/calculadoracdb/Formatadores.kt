package com.example.calculadoracdb

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

/** Converte um valor já mascarado (ex.: "R$ 1.234,56") para Double, considerando os dígitos como centavos. */
internal fun String.valorMonetarioParaDouble(): Double {
    val valorCentavos = filter { it.isDigit() }.toLongOrNull() ?: 0L
    return valorCentavos / 100.0
}
