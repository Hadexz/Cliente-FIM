package com.alex.fimportal.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.fimportal.data.*
import com.alex.fimportal.ui.theme.*
import com.alex.fimportal.utils.*

/** PANTALLA REESTRUCTURADA: NOTAS POR NIVELES */
@Composable
fun PantallaNotasComunidad(vm: FimViewModel) {
    BackHandler { vm.retrocederNivelNotas() }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val tituloBarra = when (vm.notasNavegacionNivel) {
        0 -> "Notas: Selecciona Año"
        1 -> "Notas: ${vm.anioSeleccionado}"
        2 -> vm.materiaSeleccionadaNotas
        else -> "Notas y Libros"
    }

    Scaffold(
        topBar = { TopBarPersonalizada(titulo = tituloBarra, onBack = { vm.retrocederNivelNotas() }, onRefresh = { /* Realtime */ }) },
        floatingActionButton = {
            if (vm.notasNavegacionNivel == 2) {
                FloatingActionButton(onClick = { mostrarDialogo = true }, containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar Nota", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Crossfade(targetState = vm.notasNavegacionNivel, label = "NavNotas") { nivel ->
                when (nivel) {
                    0 -> VistaSeleccionAnio(vm)
                    1 -> VistaSeleccionMateria(vm)
                    2 -> VistaListaNotas(vm)
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoAgregarNota(
            onDismiss = { mostrarDialogo = false },
            onConfirm = { titulo, desc, link ->
                vm.publicarNota(titulo, desc, link)
                mostrarDialogo = false
            }
        )
    }
}

@Composable
fun VistaSeleccionAnio(vm: FimViewModel) {
    val anios = CargaAcademica.aniosOrdenados
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        itemsIndexed(anios) { index, anio ->
             val icon = when (anio) {
                "Primer Año" -> Icons.Filled.LooksOne
                "Segundo Año" -> Icons.Filled.LooksTwo
                "Tercer Año" -> Icons.Filled.Looks3
                "Cuarto Año" -> Icons.Filled.Looks4
                "Quinto Año" -> Icons.Filled.Looks5
                else -> Icons.Filled.School
            }
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().height(90.dp).clickable { vm.seleccionarAnioNotas(anio) },
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(0.1f)), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(anio, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, null, tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun VistaSeleccionMateria(vm: FimViewModel) {
    val materias = CargaAcademica.mapa[vm.anioSeleccionado] ?: emptyList()
    var searchQuery by remember { mutableStateOf("") }
    
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Filtrar materias") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            singleLine = true
        )

        val filteredMaterias = materias.filter { normalizeText(it).contains(normalizeText(searchQuery)) }

        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(filteredMaterias) { index, materia ->
                val nuevas = vm.contarNuevas(vm.anioSeleccionado, materia)

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { vm.seleccionarMateriaNotas(materia) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Book, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(materia, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)

                        if (nuevas > 0) {
                            Box(modifier = Modifier.background(FimRed, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("+$nuevas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VistaListaNotas(vm: FimViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    
    val notasFiltradas = vm.notasComunidad.filter {
        it.anio == vm.anioSeleccionado && 
        it.materia == vm.materiaSeleccionadaNotas &&
        (normalizeText(it.titulo).contains(normalizeText(searchQuery)) || normalizeText(it.descripcion).contains(normalizeText(searchQuery)))
    }

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar en notas") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            singleLine = true
        )

        if (notasFiltradas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Book, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No hay resultados", color = Color.Gray)
                    if (searchQuery.isBlank()) Text("¡Sé el primero en aportar!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top=8.dp))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(notasFiltradas) { nota ->
                    CardNotaComunidad(nota)
                }
            }
        }
    }
}

@Composable
fun CardNotaComunidad(nota: NotaComunidad) {
    val uriHandler = LocalUriHandler.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(nota.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(nota.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (nota.link.isNotBlank()) {
                            val url = if (nota.link.startsWith("http")) nota.link else "https://${nota.link}"
                            try { uriHandler.openUri(url) } catch (e: Exception) { Log.e("LinkError", "Invalid URI: ${nota.link}") }
                        }
                    }
                    .background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.Link, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Abrir enlace", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Por: ${nota.matriculaAutor}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(nota.fecha, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun DialogoAgregarNota(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("Libro formato pdf") }
    var link by remember { mutableStateOf("") }
    var errorLink by remember { mutableStateOf(false) }
    var expandedDesc by remember { mutableStateOf(false) }
    val opcionesDesc = listOf("Libro formato pdf", "Presentación", "Notas escaneadas")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compartir Recurso", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("⚠️ AVISO IMPORTANTE:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "para compartir tus notas o libros subelos al drive tu correo institucional dale acceso a compartir con UNIVERSIDAD MICHOACANA DE SAN NICOLAS DE HIDALGO copia el enlace y pegalo aqui",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = FimDark, unfocusedTextColor = FimDark))
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de recurso") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, Modifier.clickable { expandedDesc = true }) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = FimDark, unfocusedTextColor = FimDark)
                    )
                    DropdownMenu(expanded = expandedDesc, onDismissRequest = { expandedDesc = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        opcionesDesc.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    descripcion = opcion
                                    expandedDesc = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = link,
                    onValueChange = {
                        link = it
                        errorLink = false
                    },
                    label = { Text("Enlace Drive") },
                    singleLine = true,
                    isError = errorLink,
                    supportingText = { if(errorLink) Text("Solo se aceptan enlaces de Google Drive", color = FimRed) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = FimDark, unfocusedTextColor = FimDark)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if(titulo.isNotBlank() && link.isNotBlank()) {
                        val lowerLink = link.lowercase()
                        if (lowerLink.contains("drive.google.com") || lowerLink.contains("docs.google.com")) {
                            onConfirm(titulo, descripcion, link)
                        } else {
                            errorLink = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Publicar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = FimRed) }
        },
        containerColor = FimSurface
    )
}

/** PANTALLA SUGERENCIAS */
@Composable
fun PantallaSugerencias(vm: FimViewModel) {
    BackHandler { vm.volverAlMenu() }
    var mostrarDialogo by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        vm.cargarSugerencias()
    }

    Scaffold(
        topBar = { TopBarPersonalizada("Sugerencias y Errores", { vm.volverAlMenu() }, { vm.cargarSugerencias() }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }, containerColor = FimGold) {
                Icon(Icons.Filled.Add, "Agregar", tint = FimDark)
            }
        },
        containerColor = FimBackground
    ) { padding ->
        if (vm.sugerencias.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.BugReport, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Text("No hay reportes aún", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(vm.sugerencias) { sug ->
                    CardSugerencia(sug) { vm.seleccionarSugerencia(sug) }
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoNuevaSugerencia(onDismiss = { mostrarDialogo = false }) { titulo, contenido, tipo ->
            vm.enviarSugerencia(titulo, contenido, tipo) { exito ->
                if (exito) mostrarDialogo = false
            }
        }
    }
}

@Composable
fun CardSugerencia(sug: Sugerencia, onClick: () -> Unit) {
    val icon = if (sug.tipo == "Error") Icons.Filled.BugReport else Icons.Filled.Lightbulb
    val colorIcon = if (sug.tipo == "Error") FimRed else FimGold
    val respuestasCount = sug.respuestas.size

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = FimSurface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(colorIcon.copy(alpha=0.2f)), contentAlignment=Alignment.Center) {
                 Icon(icon, null, tint = colorIcon)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sug.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FimDark)
                Text(sug.contenido, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                   Text(sug.fecha, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                   if (respuestasCount > 0) {
                       Spacer(modifier = Modifier.width(8.dp))
                       Text("$respuestasCount respuestas", style = MaterialTheme.typography.labelSmall, color = FimBlue, fontWeight = FontWeight.Bold)
                   }
                }
            }
        }
    }
}

@Composable
fun DialogoNuevaSugerencia(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Sugerencia") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Reporte", color = FimDark) },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(selected = tipo == "Sugerencia", onClick = { tipo = "Sugerencia" }, label = { Text("Sugerencia") })
                    FilterChip(selected = tipo == "Error", onClick = { tipo = "Error" }, label = { Text("Bug / Error") })
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = FimDark))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = contenido, onValueChange = { contenido = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = FimDark))
            }
        },
        confirmButton = {
            Button(onClick = { if (titulo.isNotBlank() && contenido.isNotBlank()) onConfirm(titulo, contenido, tipo) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Text("Enviar", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.error) }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun PantallaDetalleSugerencia(vm: FimViewModel) {
    BackHandler { vm.cerrarDetalleSugerencia() }
    val sugId = vm.sugerenciaSeleccionada?.id ?: return
    val sug = vm.sugerencias.find { it.id == sugId } ?: return

    var respuestaInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val respuestasOrdenadas = remember(sug.respuestas) {
        sug.respuestas.values.sortedByDescending { it.id } 
    }

    Scaffold(
        topBar = { TopBarPersonalizada("Detalle del Reporte", { vm.cerrarDetalleSugerencia() }, { vm.cargarSugerencias() }) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing, // Manejo safe area
        bottomBar = {
            // Input Bar con manejo de IME
            Surface(
                color = MaterialTheme.colorScheme.surface, 
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding() // Padding automatico teclado
                    .padding(8.dp),
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = respuestaInput, 
                        onValueChange = { respuestaInput = it }, 
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Escribir respuesta...", color = Color.Gray) },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background, 
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    IconButton(
                        onClick = { 
                            if (respuestaInput.isNotBlank()) {
                                vm.enviarRespuesta(respuestaInput)
                                respuestaInput = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState, 
            modifier = Modifier.padding(padding).fillMaxSize(), 
            contentPadding = PaddingValues(16.dp),
            reverseLayout = true 
        ) {
            items(respuestasOrdenadas) { resp ->
                CardRespuesta(resp)
            }
            
            item {
                Column {
                     HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)
                     Text("Comentarios", style = MaterialTheme.typography.titleMedium, color = FimBlue)
                     Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            item {
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp)).padding(20.dp)) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(if(sug.tipo == "Error") Icons.Filled.BugReport else Icons.Filled.Lightbulb, null, tint = if(sug.tipo == "Error") FimRed else FimGold)
                         Spacer(modifier = Modifier.width(8.dp))
                         Text(sug.titulo, style = MaterialTheme.typography.titleLarge, color = FimDark, fontWeight = FontWeight.Bold)
                     }
                     Spacer(modifier = Modifier.height(8.dp))
                     Text(sug.contenido, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                     Spacer(modifier = Modifier.height(12.dp))
                     Text("Por: ${sug.autor} • ${sug.fecha}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun CardRespuesta(resp: Respuesta) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
             Text(resp.autor.take(1), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp)).padding(16.dp)) {
            Text(resp.autor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(resp.contenido, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(resp.fecha, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.align(Alignment.End))
        }
    }
}
