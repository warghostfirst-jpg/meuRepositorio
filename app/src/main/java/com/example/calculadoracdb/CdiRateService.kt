package com.example.calculadoracdb

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Busca taxas anualizadas (% a.a.) publicadas pelo Banco Central do Brasil
 * através da API de Séries Temporais (SGS).
 */
class CdiRateService {

    /** Série SGS 4389: "CDI acumulada no mês, anualizada base 252". */
    suspend fun buscarTaxaCdiAnual(): Result<Double> = buscarTaxaAnualizada(SERIE_CDI)

    /** Série SGS 432: "Meta da taxa Selic definida pelo Copom". */
    suspend fun buscarTaxaSelicAnual(): Result<Double> = buscarTaxaAnualizada(SERIE_SELIC)

    private suspend fun buscarTaxaAnualizada(codigoSerie: Int): Result<Double> = withContext(Dispatchers.IO) {
        val endpoint = "https://api.bcb.gov.br/dados/serie/bcdata.sgs.$codigoSerie/dados/ultimos/1?formato=json"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
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
        const val SERIE_CDI = 4389
        const val SERIE_SELIC = 432
        const val TIMEOUT_MS = 10_000
    }
}
