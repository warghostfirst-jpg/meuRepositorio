package com.example.calculadoracdb

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * IDs de TESTE oficiais do Google (não geram receita real e podem ser usados em qualquer app
 * durante o desenvolvimento). Antes de publicar, troque pelos IDs reais criados em
 * https://apps.admob.com para o seu app e para cada unidade de anúncio.
 */
private const val AD_UNIT_BANNER_TESTE = "ca-app-pub-3940256099942544/9214589741"
private const val AD_UNIT_INTERSTITIAL_TESTE = "ca-app-pub-3940256099942544/1033173712"

/** A cada quantas simulações concluídas um interstitial pode ser exibido. */
internal const val FREQUENCIA_INTERSTITIAL = 3

/**
 * Pede o consentimento de anúncios personalizados/não personalizados (obrigatório para usuários
 * no EEE/Reino Unido, e recomendado para todos os usuários) via UMP, e só então inicializa o SDK
 * de anúncios. Chame uma vez, na Activity principal.
 */
internal fun solicitarConsentimentoEInicializarAnuncios(activity: Activity, aoConcluir: (podeCarregarAnuncios: Boolean) -> Unit) {
    val parametros = ConsentRequestParameters.Builder().build()
    val informacoesConsentimento: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)

    fun inicializarSePermitido() {
        if (informacoesConsentimento.canRequestAds()) {
            MobileAds.initialize(activity)
            aoConcluir(true)
        }
    }

    informacoesConsentimento.requestConsentInfoUpdate(
        activity,
        parametros,
        {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                inicializarSePermitido()
            }
        },
        { inicializarSePermitido() }
    )

    inicializarSePermitido()
}

internal object GerenciadorInterstitial {
    private var anuncio: InterstitialAd? = null
    private var carregando = false

    fun carregar(context: Context) {
        if (anuncio != null || carregando) return
        carregando = true
        InterstitialAd.load(
            context,
            AD_UNIT_INTERSTITIAL_TESTE,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    carregando = false
                    anuncio = interstitialAd
                }

                override fun onAdFailedToLoad(erro: LoadAdError) {
                    carregando = false
                    anuncio = null
                }
            }
        )
    }

    fun mostrarSeDisponivel(activity: Activity, aoFechar: () -> Unit) {
        val anuncioAtual = anuncio
        if (anuncioAtual == null) {
            aoFechar()
            return
        }
        anuncioAtual.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                anuncio = null
                carregar(activity)
                aoFechar()
            }

            override fun onAdFailedToShowFullScreenContent(erro: AdError) {
                anuncio = null
                aoFechar()
            }
        }
        anuncioAtual.show(activity)
    }
}

@Composable
internal fun BannerAnuncio(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            adUnitId = AD_UNIT_BANNER_TESTE
            setAdSize(AdSize.BANNER)
        }
    }

    DisposableEffect(Unit) {
        adView.loadAd(AdRequest.Builder().build())
        onDispose { adView.destroy() }
    }

    AndroidView(modifier = modifier.fillMaxWidth(), factory = { adView })
}
