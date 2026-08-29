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
