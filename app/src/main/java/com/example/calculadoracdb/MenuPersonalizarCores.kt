package com.example.calculadoracdb

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val paletaCores: List<Color?> = listOf(
    null,
    Color(0xFFFFFFFF),
    Color(0xFF121212),
    Color(0xFFF5F5F5),
    Color(0xFF37474F),
    Color(0xFF0B7A3D),
    Color(0xFF1565C0),
    Color(0xFF6A1B9A),
    Color(0xFFC62828),
    Color(0xFFE65100),
    Color(0xFFF9A825),
    Color(0xFF00838F)
)

@Composable
internal fun MenuPersonalizarCores(
    coresPersonalizadas: CoresPersonalizadas,
    onCoresPersonalizadasChange: (CoresPersonalizadas) -> Unit,
    onFechar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Personalizar aparência", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onFechar) {
                Icon(Icons.Filled.Close, contentDescription = "Fechar menu")
            }
        }

        SeletorDeCor(
            titulo = "Cor primária",
            corSelecionada = coresPersonalizadas.primaria,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(primaria = it)) }
        )

        SeletorDeCor(
            titulo = "Cor secundária",
            corSelecionada = coresPersonalizadas.secundaria,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(secundaria = it)) }
        )

        SeletorDeCor(
            titulo = "Cor terciária",
            corSelecionada = coresPersonalizadas.terciaria,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(terciaria = it)) }
        )

        SeletorDeCor(
            titulo = "Cor de fundo",
            corSelecionada = coresPersonalizadas.fundo,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(fundo = it)) }
        )

        SeletorDeCor(
            titulo = "Cor dos cartões (superfície)",
            corSelecionada = coresPersonalizadas.superficie,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(superficie = it)) }
        )

        SeletorDeCor(
            titulo = "Cor do texto",
            corSelecionada = coresPersonalizadas.texto,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(texto = it)) }
        )

        SeletorDeCor(
            titulo = "Cor das bordas",
            corSelecionada = coresPersonalizadas.borda,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(borda = it)) }
        )

        SeletorDeCor(
            titulo = "Cor de erro",
            corSelecionada = coresPersonalizadas.erro,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(erro = it)) }
        )

        HorizontalDivider()

        TextButton(
            onClick = { onCoresPersonalizadasChange(CoresPersonalizadas()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restaurar cores padrão")
        }
    }
}

@Composable
private fun SeletorDeCor(
    titulo: String,
    corSelecionada: Color?,
    aoSelecionar: (Color?) -> Unit
) {
    var mostrarSeletorCustomizado by remember { mutableStateOf(false) }
    val corEhCustomizada = corSelecionada != null && paletaCores.none { it == corSelecionada }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            paletaCores.forEach { cor ->
                AmostraDeCor(
                    cor = cor,
                    selecionada = !corEhCustomizada && cor == corSelecionada,
                    onClick = { aoSelecionar(cor) }
                )
            }
            AmostraDeCorCustomizada(
                corAtual = corSelecionada.takeIf { corEhCustomizada },
                selecionada = corEhCustomizada,
                onClick = { mostrarSeletorCustomizado = true }
            )
        }
    }

    if (mostrarSeletorCustomizado) {
        DialogoCorCustomizada(
            corInicial = corSelecionada ?: Color(0xFF0B7A3D),
            onConfirmar = {
                aoSelecionar(it)
                mostrarSeletorCustomizado = false
            },
            onCancelar = { mostrarSeletorCustomizado = false }
        )
    }
}

@Composable
private fun AmostraDeCor(cor: Color?, selecionada: Boolean, onClick: () -> Unit) {
    val corExibida = cor ?: MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(corExibida)
            .border(
                width = if (selecionada) 3.dp else 1.dp,
                color = if (selecionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val corDoIcone = if (corExibida.luminance() > 0.5f) Color.Black else Color.White
        if (cor == null) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Padrão",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (selecionada) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = corDoIcone
            )
        }
    }
}

private val coresArcoIris: List<Color> =
    (0..360 step 60).map { hsvParaCor(it.toFloat(), 1f, 1f) }

@Composable
private fun AmostraDeCorCustomizada(corAtual: Color?, selecionada: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(
                if (corAtual != null) Modifier.background(corAtual)
                else Modifier.background(Brush.sweepGradient(coresArcoIris))
            )
            .border(
                width = if (selecionada) 3.dp else 1.dp,
                color = if (selecionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val corDoIcone = corAtual?.let { if (it.luminance() > 0.5f) Color.Black else Color.White } ?: Color.White
        Icon(
            Icons.Filled.Palette,
            contentDescription = "Cor personalizada",
            modifier = Modifier.size(18.dp),
            tint = corDoIcone
        )
    }
}

private fun hsvParaCor(matiz: Float, saturacao: Float, valor: Float): Color =
    Color(android.graphics.Color.HSVToColor(floatArrayOf(matiz, saturacao, valor)))

private fun Color.paraHsv(): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsv)
    return hsv
}

private fun Color.paraHex(): String = "#%06X".format(0xFFFFFF and toArgb())

private fun String.paraCorHex(): Color? {
    val limpo = removePrefix("#").trim()
    if (limpo.length != 6 || limpo.any { it !in "0123456789abcdefABCDEF" }) return null
    return runCatching { Color((0xFF000000 or limpo.toLong(16)).toInt()) }.getOrNull()
}

@Composable
private fun DialogoCorCustomizada(
    corInicial: Color,
    onConfirmar: (Color) -> Unit,
    onCancelar: () -> Unit
) {
    var hsv by remember { mutableStateOf(corInicial.paraHsv()) }
    var textoHex by remember { mutableStateOf(corInicial.paraHex()) }
    val corAtual = hsvParaCor(hsv[0], hsv[1], hsv[2])

    fun atualizarHsv(novoHsv: FloatArray) {
        hsv = novoHsv
        textoHex = hsvParaCor(novoHsv[0], novoHsv[1], novoHsv[2]).paraHex()
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Cor personalizada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(corAtual)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                )

                QuadradoSaturacaoValor(
                    matiz = hsv[0],
                    saturacao = hsv[1],
                    valor = hsv[2],
                    onAlterar = { s, v -> atualizarHsv(floatArrayOf(hsv[0], s, v)) }
                )

                SeletorMatiz(
                    matiz = hsv[0],
                    onAlterar = { h -> atualizarHsv(floatArrayOf(h, hsv[1], hsv[2])) }
                )

                OutlinedTextField(
                    value = textoHex,
                    onValueChange = { texto ->
                        textoHex = texto
                        texto.paraCorHex()?.let { hsv = it.paraHsv() }
                    },
                    label = { Text("Código hex (RRGGBB)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(corAtual) }) { Text("Aplicar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@Composable
private fun QuadradoSaturacaoValor(
    matiz: Float,
    saturacao: Float,
    valor: Float,
    onAlterar: (saturacao: Float, valor: Float) -> Unit
) {
    val corMatizPura = hsvParaCor(matiz, 1f, 1f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures { deslocamento ->
                    onAlterar(
                        (deslocamento.x / size.width).coerceIn(0f, 1f),
                        1f - (deslocamento.y / size.height).coerceIn(0f, 1f)
                    )
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { alteracao, _ ->
                    alteracao.consume()
                    onAlterar(
                        (alteracao.position.x / size.width).coerceIn(0f, 1f),
                        1f - (alteracao.position.y / size.height).coerceIn(0f, 1f)
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(brush = Brush.horizontalGradient(listOf(Color.White, corMatizPura)))
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        }

        val deslocamentoX = maxWidth * saturacao
        val deslocamentoY = maxHeight * (1f - valor)
        Box(
            modifier = Modifier
                .offset(x = deslocamentoX - 10.dp, y = deslocamentoY - 10.dp)
                .size(20.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .border(3.dp, Color.Black.copy(alpha = 0.25f), CircleShape)
        )
    }
}

@Composable
private fun SeletorMatiz(matiz: Float, onAlterar: (Float) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .pointerInput(Unit) {
                detectTapGestures { deslocamento ->
                    onAlterar((deslocamento.x / size.width).coerceIn(0f, 1f) * 360f)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { alteracao, _ ->
                    alteracao.consume()
                    onAlterar((alteracao.position.x / size.width).coerceIn(0f, 1f) * 360f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(brush = Brush.horizontalGradient(coresArcoIris))
        }

        val deslocamentoX = (maxWidth * (matiz / 360f)).coerceIn(0.dp, maxWidth - 6.dp)
        Box(
            modifier = Modifier
                .offset(x = deslocamentoX)
                .fillMaxHeight()
                .width(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White)
                .border(1.dp, Color.Black.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
        )
    }
}
