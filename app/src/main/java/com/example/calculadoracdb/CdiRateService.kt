package com.example.calculadoracdb

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Busca a taxa CDI anualizada (% a.a.) publicada pelo Banco Central do Brasil,
 * série SGS 4389 ("CDI acumulada no mês, anualizada base 252").
 */
class CdiRateService {

    suspend fun buscarTaxaCdiAnual(): Result<Double> = withContext(Dispatchers.IO) {
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
        }
        try {
            val corpo = connection.inputStream.bufferedReader().use { it.readText() }
            val itens = JSONArray(corpo)
            if (itens.length() == 0) {
                Result.failure(IllegalStateException("Nenhum dado retornado pelo Banco Central."))
            } else {
                val valor = itens.getJSONObject(0).getString("valor")
                    .replace(",", ".")
                    .toDouble()
                Result.success(valor)
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val ENDPOINT =
            "https://api.bcb.gov.br/dados/serie/bcdata.sgs.4389/dados/ultimos/1?formato=json"
        const val TIMEOUT_MS = 10_000
    }
}
