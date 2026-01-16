package com.alex.fimportal.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.fimportal.data.Materia
import com.alex.fimportal.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarPersonalizada(titulo: String, onBack: () -> Unit, onRefresh: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(titulo, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onBackground) } },
        actions = { IconButton(onClick = onRefresh) { Icon(Icons.Filled.Refresh, contentDescription = "Recargar", tint = MaterialTheme.colorScheme.onBackground) } },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun AnimatableMenuItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        visible = true
    }
    AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })) {
        content()
    }
}

/** 
 * WIDGET STYLE BUTTON (Bento Grid Item)
 * Replaces the old horizontal MenuButton
 */
@Composable
fun DashboardWidget(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorFondo: Color,
    colorIcono: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(24.dp), // Highly rounded
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)), // Semi-transparent for dark mode adaptation
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = colorIcono, modifier = Modifier.size(24.dp))
            }
            
            Column {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** KEEPING OLD MENU BUTTON FOR LIST COMPATIBILITY IF NEEDED (Renamed) */
@Composable
fun MenuButtonList(titulo: String, subtitulo: String, icono: ImageVector, color: Color, onClick: () -> Unit) {
    // Legacy implementation, can be removed if strictly grid. Keeping for fallback.
     DashboardWidget(titulo, subtitulo, icono, Color.White, color, onClick, Modifier.fillMaxWidth().height(90.dp))
}


@Composable
fun ErrorCard(materia: Materia) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = FimRed.copy(alpha = 0.15f), contentColor = FimRed), border = BorderStroke(1.dp, FimRed.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) { Text(materia.nombre, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(4.dp)); Text(materia.profesor, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
fun MateriaCard(materia: Materia) {
    val promNum = materia.promedio.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 0f
    val nombreLimpio = materia.nombre.replace(Regex("\\[.*?\\]"), "").trim()

    val statusColor = when {
        materia.promedio == "--" -> Color.Gray
        promNum >= 8.0 -> FimGreen
        promNum >= 6.0 -> FimYellow
        else -> FimRed
    }

    // New Style: White card, rounded, soft shadow
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Box(modifier = Modifier.width(4.dp).height(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                Spacer(modifier = Modifier.width(12.dp)) 
                Column(modifier = Modifier.weight(1f)) { 
                    Text(nombreLimpio, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(materia.profesor, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis) 
                } 
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.background)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { 
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { 
                    materia.parciales.forEachIndexed { i, nota -> CajaCalif("P${i + 1}", nota) } 
                }
                Spacer(modifier = Modifier.weight(1f))
                CajaCalif("Prom", materia.promedio, isPromedio = true) 
            }
        }
    }
}

@Composable
fun CajaCalif(titulo: String, nota: String, isPromedio: Boolean = false) {
    val califNum = nota.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 0f

    val colorTexto = when {
        nota == "--" -> Color.Gray
        nota == "A" -> Color(0xFF4CAF50) // Green
        califNum >= 8.0 -> Color(0xFF4CAF50) // Green
        califNum >= 6.0 -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336) // Red
    }

    val backgroundModifier = if (isPromedio) { 
        Modifier.clip(RoundedCornerShape(12.dp)).background(colorTexto.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 6.dp) 
    } else { Modifier }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = backgroundModifier) { 
        Text(titulo, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
        Text(nota, style = MaterialTheme.typography.titleMedium, fontWeight = if (isPromedio) FontWeight.Black else FontWeight.Bold, color = colorTexto) 
    }
}
