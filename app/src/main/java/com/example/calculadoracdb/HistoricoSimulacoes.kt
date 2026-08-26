package com.example.calculadoracdb

import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

private const val CHAVE_HISTORICO = "historico_simulacoes"

data class ItemHistorico(
    val id: Long,
    val dataHoraCalculo: LocalDateTime,
    val descricaoEntrada: String,
    val resultado: ResultadoCdb
)

/** Persiste o histórico de simulações de CDB em SharedPreferences, como JSON. */
class HistoricoRepositorio(private val preferencias: SharedPreferences) {

    fun carregarTodos(): List<ItemHistorico> {
        val json = preferencias.getString(CHAVE_HISTORICO, null) ?: return emptyList()
        val array = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
        return (0 until array.length())
            .mapNotNull { indice -> runCatching { array.getJSONObject(indice).paraItemHistorico() }.getOrNull() }
            .sortedByDescending { it.dataHoraCalculo }
    }

    fun adicionar(item: ItemHistorico) {
        salvarTodos(listOf(item) + carregarTodos())
    }

    fun remover(id: Long) {
        salvarTodos(carregarTodos().filterNot { it.id == id })
    }

    fun limparTudo() {
        preferencias.edit { remove(CHAVE_HISTORICO) }
    }

    private fun salvarTodos(itens: List<ItemHistorico>) {
        val array = JSONArray()
        itens.forEach { array.put(it.paraJson()) }
        preferencias.edit { putString(CHAVE_HISTORICO, array.toString()) }
    }
}

private fun ItemHistorico.paraJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("dataHoraCalculo", dataHoraCalculo.toString())
    put("descricaoEntrada", descricaoEntrada)
    put("resultado", resultado.paraJson())
}

private fun ResultadoCdb.paraJson(): JSONObject = JSONObject().apply {
    put("totalAportado", totalAportado)
    put("valorBruto", valorBruto)
    put("rendimentoBruto", rendimentoBruto)
    put("iofValor", iofValor)
    put("aliquotaIr", aliquotaIr)
    put("irValor", irValor)
    put("valorLiquido", valorLiquido)
    put("rendimentoLiquido", rendimentoLiquido)
    put("rentabilidadeLiquidaPercentual", rentabilidadeLiquidaPercentual)
    put("dataFimInvestimento", dataFimInvestimento.toString())
}

private fun JSONObject.paraItemHistorico(): ItemHistorico = ItemHistorico(
    id = getLong("id"),
    dataHoraCalculo = LocalDateTime.parse(getString("dataHoraCalculo")),
    descricaoEntrada = getString("descricaoEntrada"),
    resultado = getJSONObject("resultado").paraResultadoCdb()
)

private fun JSONObject.paraResultadoCdb(): ResultadoCdb = ResultadoCdb(
    totalAportado = getDouble("totalAportado"),
    valorBruto = getDouble("valorBruto"),
    rendimentoBruto = getDouble("rendimentoBruto"),
    iofValor = getDouble("iofValor"),
    aliquotaIr = getDouble("aliquotaIr"),
    irValor = getDouble("irValor"),
    valorLiquido = getDouble("valorLiquido"),
    rendimentoLiquido = getDouble("rendimentoLiquido"),
    rentabilidadeLiquidaPercentual = getDouble("rentabilidadeLiquidaPercentual"),
    dataFimInvestimento = LocalDate.parse(getString("dataFimInvestimento"))
)
