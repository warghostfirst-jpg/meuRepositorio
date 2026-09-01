package com.example.calculadoracdb

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.PendingPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ID do produto "remover anúncios" (compra única, não consumível). Precisa ser criado no
 * Play Console (Monetizar > Produtos > Produtos no app) com este mesmo ID antes de funcionar
 * em produção ou em testes internos/fechados.
 */
internal const val PRODUTO_REMOVER_ANUNCIOS = "remover_anuncios"

/**
 * Gerencia a compra única "remover anúncios" via Play Billing. Mantém o estado de compra em
 * memória (consultado do Google Play a cada conexão) e cacheado em [Preferencias] para a UI não
 * "piscar" anúncio antes da consulta terminar.
 */
internal class GerenciadorCompras(private val context: Context) {

    private val preferencias = context.getSharedPreferences(PREFERENCIAS_APP, Context.MODE_PRIVATE)

    private val _anunciosRemovidos = MutableStateFlow(preferencias.getBoolean(CHAVE_ANUNCIOS_REMOVIDOS, false))
    val anunciosRemovidos: StateFlow<Boolean> = _anunciosRemovidos.asStateFlow()

    private val _precoFormatado = MutableStateFlow<String?>(null)
    val precoFormatado: StateFlow<String?> = _precoFormatado.asStateFlow()

    private var detalhesProduto: com.android.billingclient.api.ProductDetails? = null

    private val listenerCompras = PurchasesUpdatedListener { resultado, compras ->
        if (resultado.responseCode == BillingClient.BillingResponseCode.OK && compras != null) {
            compras.forEach(::processarCompra)
        }
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(listenerCompras)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    fun iniciar() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(resultado: BillingResult) {
                if (resultado.responseCode == BillingClient.BillingResponseCode.OK) {
                    consultarProduto()
                    consultarComprasExistentes()
                }
            }

            override fun onBillingServiceDisconnected() {
                // O próprio BillingClient tenta reconectar na próxima chamada.
            }
        })
    }

    private fun consultarProduto() {
        val produto = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUTO_REMOVER_ANUNCIOS)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(produto)).build()

        billingClient.queryProductDetailsAsync(params) { _, resultadoConsulta ->
            val encontrado = resultadoConsulta.productDetailsList.firstOrNull()
            detalhesProduto = encontrado
            _precoFormatado.value = encontrado?.oneTimePurchaseOfferDetails?.formattedPrice
        }
    }

    private fun consultarComprasExistentes() {
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        billingClient.queryPurchasesAsync(params) { _, compras ->
            compras.forEach(::processarCompra)
        }
    }

    private fun processarCompra(compra: Purchase) {
        if (compra.products.contains(PRODUTO_REMOVER_ANUNCIOS) && compra.purchaseState == Purchase.PurchaseState.PURCHASED) {
            marcarAnunciosComoRemovidos()
            if (!compra.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(compra.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { }
            }
        }
    }

    private fun marcarAnunciosComoRemovidos() {
        _anunciosRemovidos.value = true
        preferencias.edit().putBoolean(CHAVE_ANUNCIOS_REMOVIDOS, true).apply()
    }

    fun comprar(activity: Activity) {
        val produto = detalhesProduto ?: return
        val paramsProduto = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(produto)
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(paramsProduto))
            .build()
        billingClient.launchBillingFlow(activity, params)
    }

    fun encerrar() {
        billingClient.endConnection()
    }
}
