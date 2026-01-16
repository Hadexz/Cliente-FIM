package com.alex.fimportal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.fimportal.data.*
import com.alex.fimportal.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun PantallaCredencial(vm: FimViewModel) {
    BackHandler { vm.volverAlMenu() }
    
    // Light Theme
    Scaffold(
        topBar = { TopBarPersonalizada(titulo = "Credencial Digital", onBack = { vm.volverAlMenu() }, onRefresh = { vm.cargarCredencial() }) }, 
        containerColor = MaterialTheme.colorScheme.background // Light
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            if (vm.isLoading) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } else {
                val bitmap = vm.credencialBitmap
                if (bitmap != null) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(0.9f).aspectRatio(0.7f), 
                        shape = RoundedCornerShape(24.dp), 
                        elevation = CardDefaults.cardElevation(10.dp), 
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) { 
                        Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Credencial PDF", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit) 
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                        Icon(Icons.Filled.Warning, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No se pudo cargar la credencial", color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { vm.cargarCredencial() }, 
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(24.dp)
                        ) { 
                            Text("Reintentar", color = Color.White) 
                        } 
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaNotas(vm: FimViewModel, titulo: String, listaMaterias: List<Materia>, onRefresh: () -> Unit) {
    BackHandler { vm.volverAlMenu() }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopBarPersonalizada(titulo = titulo, onBack = { vm.volverAlMenu() }, onRefresh = onRefresh) }, 
        containerColor = FimBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar materia") },
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

            if (vm.isLoading) { 
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } 
            } else {
                val filteredList = listaMaterias.filter { 
                    com.alex.fimportal.utils.normalizeText(it.nombre).contains(com.alex.fimportal.utils.normalizeText(searchQuery)) || 
                    com.alex.fimportal.utils.normalizeText(it.profesor).contains(com.alex.fimportal.utils.normalizeText(searchQuery)) 
                }
                
                if (filteredList.isEmpty()) { 
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) { Text("No hay datos disponibles", color = Color.Gray) } 
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        itemsIndexed(filteredList) { index, materia ->
                            if (materia.nombre == "SIN DATOS" || materia.nombre == "ERROR") ErrorCard(materia) else MateriaCard(materia)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaHorario(vm: FimViewModel) {
    BackHandler { vm.volverAlMenu() }
    val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
    val clasesDelDia = vm.horario.filter { it.dia == vm.diaSeleccionado }.sortedBy { it.hora }
    
    Scaffold(
        topBar = { TopBarPersonalizada(titulo = "Mi Horario", onBack = { vm.volverAlMenu() }, onRefresh = { vm.cargarHorario() }) }, 
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                dias.forEach { dia ->
                    val seleccionado = vm.diaSeleccionado == dia
                    val conflictivo = vm.horario.any { it.dia == dia && it.hayEmpalme }
                    val colorFondo = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val colorTexto = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface 
                    val border = if (seleccionado) null else BorderStroke(1.dp, Color.LightGray)

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { vm.diaSeleccionado = dia },
                        color = colorFondo,
                        shape = RoundedCornerShape(16.dp),
                        border = border,
                        shadowElevation = if(seleccionado) 4.dp else 0.dp
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (conflictivo) {
                                Icon(Icons.Filled.Warning, contentDescription = "Conflicto", tint = FimRed, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(text = dia.take(3).uppercase(), color = colorTexto, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
            
            if (vm.isLoading) { 
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } 
            } else {
                if (clasesDelDia.isEmpty()) { 
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                            Icon(Icons.Filled.DateRange, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Día libre", style = MaterialTheme.typography.titleLarge, color = Color.Gray) 
                        } 
                    } 
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(clasesDelDia) { clase ->
                            val esHoraActual = try { val partes = clase.hora.split("-"); if (partes.size == 2) { val inicio = LocalTime.parse(partes[0].trim(), DateTimeFormatter.ofPattern("HH:mm")); val fin = LocalTime.parse(partes[1].trim(), DateTimeFormatter.ofPattern("HH:mm")); vm.diaSeleccionado == obtenerDiaHoy(vm) && LocalTime.now().isAfter(inicio) && LocalTime.now().isBefore(fin) } else false } catch (e: Exception) { false }
                            AnimatedVisibility(visible = true, enter = fadeIn()) {
                                HorarioCard(clase, esActual = esHoraActual)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun obtenerDiaHoy(vm: FimViewModel): String {
    return try { val hoy = LocalDate.now().dayOfWeek; when(hoy) { DayOfWeek.MONDAY -> "Lunes"; DayOfWeek.TUESDAY -> "Martes"; DayOfWeek.WEDNESDAY -> "Miércoles"; DayOfWeek.THURSDAY -> "Jueves"; DayOfWeek.FRIDAY -> "Viernes"; else -> "Lunes" } } catch (e: Exception) { "Lunes" }
}

@Composable
fun HorarioCard(clase: ClaseHorario, esActual: Boolean) {
    val containerColor = if (esActual) MaterialTheme.colorScheme.primary.copy(alpha=0.2f) else MaterialTheme.colorScheme.surface
    val borderColor = if (clase.hayEmpalme) FimRed else if (esActual) MaterialTheme.colorScheme.primary else Color.Transparent
    
    // Modern Card
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().border(BorderStroke(if (clase.hayEmpalme) 2.dp else 1.dp, borderColor), shape = RoundedCornerShape(20.dp)), 
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor), 
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                val horas = clase.hora.split("-")
                Text(horas.getOrElse(0){""}.trim(), fontSize = 16.sp, fontWeight = FontWeight.Black, color = FimDark)
                Text(horas.getOrElse(1){""}.trim(), fontSize = 12.sp, color = Color.Gray) 
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.width(4.dp).height(40.dp).background(if(esActual) MaterialTheme.colorScheme.primary else Color.LightGray, CircleShape))
            Spacer(modifier = Modifier.width(16.dp))
            Column { 
                if (clase.hayEmpalme) Text("⚠ EMPALME DETECTADO", color = FimRed, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                if (esActual) Text("EN CURSO", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                Text(clase.materia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { 
                    Icon(Icons.Filled.Place, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${clase.aula} • ${clase.seccion}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) 
                } 
            }
        }
    }
}

@Composable
fun PantallaAvance(vm: FimViewModel) {
    BackHandler { vm.volverAlMenu() }
    
    Scaffold(
        topBar = { TopBarPersonalizada(titulo = "Avance Académico", onBack = { vm.volverAlMenu() }, onRefresh = { vm.cargarAvance() }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (vm.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
        } else {
            if (vm.avanceAcademico.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("No hay datos disponibles", color = Color.Gray) }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(vm.avanceAcademico) { index, item ->
                        AvanceCard(item, onVerTemas = {
                            vm.irATemasReportados(item.idHorario, item.idProfesor, item.materia)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun AvanceCard(item: AvanceItem, onVerTemas: () -> Unit) {
    val porcentajeNum = item.porcentaje.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 0f

    // Modern White Card
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.clave, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                Spacer(modifier = Modifier.weight(1f))
                Text(item.porcentaje, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.materia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(item.profesorNombre, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { porcentajeNum / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.background
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (item.idHorario.isNotBlank()) {
                Button(
                    onClick = onVerTemas,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha=0.2f), contentColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Ver Temas Reportados", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PantallaTemas(vm: FimViewModel) {
    BackHandler { vm.volverAAvance() }
    
    Scaffold(
        topBar = { TopBarPersonalizada(titulo = "Temas Reportados", onBack = { vm.volverAAvance() }, onRefresh = { vm.cargarTemas() }) },
        containerColor = FimBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Header for Materia Name
            Box(
                 modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha=0.15f)).padding(16.dp),
                 contentAlignment = Alignment.Center
            ) {
                Text(
                    text = vm.materiaSeleccionadaNombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            if (vm.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            } else {
                if (vm.temasReportados.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "No hay temas reportados", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                        }
                    }
                } else {
                    val grupos = remember(vm.temasReportados) {
                        vm.temasReportados.groupBy { it.tema }
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                        grupos.forEach { (temaPrincipal, listaSubtemas) ->
                            item { TemaHeader(temaPrincipal) }
                            items(listaSubtemas) { subtema -> SubtemaCard(subtema) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemaHeader(titulo: String) {
    Surface(color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(4.dp).height(18.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun SubtemaCard(item: TemaReportado) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 16.dp)) {
                Icon(Icons.Filled.DateRange, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Text(text = item.fecha, style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center)
            }
            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.LightGray))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = item.subtema, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
