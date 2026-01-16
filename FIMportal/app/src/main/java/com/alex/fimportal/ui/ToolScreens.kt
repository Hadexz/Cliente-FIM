package com.alex.fimportal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.fimportal.data.*
import com.alex.fimportal.ui.theme.*
import com.alex.fimportal.utils.ThermoEngine
import com.alex.fimportal.utils.normalizeText

@Composable
fun PantallaCalculadoraTermo(vm: FimViewModel) {
    BackHandler { vm.volverAlMenu() }

    var modoCalculo by remember { mutableStateOf("PT") }
    var inputP by remember { mutableStateOf("") }
    var inputT by remember { mutableStateOf("") }
    var inputX by remember { mutableStateOf("") }

    var resultado by remember { mutableStateOf<ThermoEngine.ThermoState?>(null) }
    var errorCalc by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopBarPersonalizada(titulo = "Calc. IAPWS-97", onBack = { vm.volverAlMenu() }, onRefresh = {
            inputP = ""; inputT = ""; inputX = ""; resultado = null; errorCalc = null
        }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Entradas:", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(selected = modoCalculo == "PT", onClick = { modoCalculo = "PT" }, label = { Text("Presión & Temp") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary.copy(0.2f), selectedLabelColor = MaterialTheme.colorScheme.onBackground, labelColor = Color.Gray))
                FilterChip(selected = modoCalculo == "PX", onClick = { modoCalculo = "PX" }, label = { Text("Presión & Calidad") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary.copy(0.2f), selectedLabelColor = MaterialTheme.colorScheme.onBackground, labelColor = Color.Gray))
            }
            Spacer(modifier = Modifier.height(16.dp))

            ThermoInput(value = inputP, onValueChange = { inputP = it }, label = "Presión (P)", unit = "kPa")
            Spacer(modifier = Modifier.height(12.dp))

            if (modoCalculo == "PT") {
                ThermoInput(value = inputT, onValueChange = { inputT = it }, label = "Temperatura (T)", unit = "°C")
            } else if (modoCalculo == "PX") {
                ThermoInput(value = inputX, onValueChange = { inputX = it }, label = "Calidad (x)", unit = "0 - 1")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    errorCalc = null
                    try {
                        val p = inputP.toDoubleOrNull()
                        if (p == null || p <= 0) throw Exception("Presión inválida")

                        if (modoCalculo == "PT") {
                            val t = inputT.toDoubleOrNull() ?: throw Exception("Temperatura inválida")
                            resultado = ThermoEngine.calcularPorPresionYTemperatura(p, t)
                        } else if (modoCalculo == "PX") {
                            val x = inputX.toDoubleOrNull() ?: throw Exception("Calidad inválida")
                            if (x < 0 || x > 1) throw Exception("La calidad debe estar entre 0 y 1")
                            resultado = ThermoEngine.calcularPorPresionYCalidad(p, x)
                        }
                    } catch (e: Exception) {
                        errorCalc = e.message
                        resultado = null
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Filled.Calculate, null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALCULAR", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorCalc != null) {
                Card(colors = CardDefaults.cardColors(containerColor = FimRed.copy(0.1f)), border = BorderStroke(1.dp, FimRed), shape = RoundedCornerShape(16.dp)) {
                    Text(errorCalc!!, color = FimRed, modifier = Modifier.padding(16.dp))
                }
            }

            resultado?.let { res ->
                Text("Resultados:", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))

                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        FaseBadge(res.fase)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(0.4f))

                        ResultRow("Presión (P)", String.format("%.3f", res.presion), "kPa")
                        ResultRow("Temperatura (T)", String.format("%.3f", res.temperatura), "°C")
                        ResultRow("Calidad (x)", res.calidad?.let { String.format("%.4f", it) } ?: "---", "")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(0.4f))
                        ResultRow("Entalpía (h)", String.format("%.2f", res.entalpia), "kJ/kg")
                        ResultRow("Entropía (s)", String.format("%.4f", res.entropia), "kJ/kg·K")
                        ResultRow("E. Interna (u)", String.format("%.2f", res.energiaInterna), "kJ/kg")
                        ResultRow("Vol. Esp. (v)", String.format("%.6f", res.volumen), "m³/kg")
                    }
                }
            }
        }
    }
}

@Composable
fun ThermoInput(value: String, onValueChange: (String) -> Unit, label: String, unit: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        trailingIcon = { Text(unit, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 12.dp)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ResultRow(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(unit, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
    }
}

@Composable
fun FaseBadge(fase: String) {
    val colorFase = when {
        fase.contains("Sobrecalentado") -> FimRed
        fase.contains("Líquido") -> FimBlue
        else -> FimGold
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colorFase))
        Spacer(modifier = Modifier.width(8.dp))
        Text(fase, color = colorFase, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun PantallaAsistencia(vm: FimViewModel) {
    BackHandler { vm.volverAlMenu() }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopBarPersonalizada(titulo = "Asistencia de Profesores", onBack = { vm.volverAlMenu() }, onRefresh = { vm.cargarAsistencia() }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar profesor") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            } else {
                if (vm.asistenciaProfesores.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay datos de asistencia", color = Color.Gray) }
                } else {
                    val filteredList = vm.asistenciaProfesores.filter {
                        normalizeText(it.profesor).contains(normalizeText(searchQuery)) ||
                                normalizeText(it.materia).contains(normalizeText(searchQuery))
                    }

                    if (filteredList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No se encontraron coincidencias", color = Color.Gray) }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(filteredList) { index, item ->
                                AsistenciaCard(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AsistenciaCard(item: AsistenciaProfesor) {
    val porcentajeNum = item.porcentaje.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 0f
    val colorBarra = if (porcentajeNum > 80) FimGreen else if (porcentajeNum > 50) FimYellow else FimRed

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(4.dp).height(40.dp).background(colorBarra, CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.profesor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(item.materia, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(item.porcentaje, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colorBarra)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.2f))

            LinearProgressIndicator(
                progress = { porcentajeNum / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = colorBarra,
                trackColor = FimBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if(item.seccion.isNotBlank()) Text("Grupo: ${item.seccion}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if(item.aula.isNotBlank()) Text("Aula: ${item.aula}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                if (item.estatus == "Presente") {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Presente", tint = FimGreen)
                } else if (item.estatus == "Ausente") {
                    Icon(Icons.Filled.Cancel, contentDescription = "Ausente", tint = FimRed)
                }
            }
        }
    }
}
