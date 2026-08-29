package com.example.calculadoracdb

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
            titulo = "Cor de fundo",
            corSelecionada = coresPersonalizadas.fundo,
            aoSelecionar = { onCoresPersonalizadasChange(coresPersonalizadas.copy(fundo = it)) }
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
                    selecionada = cor == corSelecionada,
                    onClick = { aoSelecionar(cor) }
                )
            }
        }
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
