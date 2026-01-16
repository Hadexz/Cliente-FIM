package com.alex.fimportal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alex.fimportal.R
import com.alex.fimportal.data.Pantalla
import com.alex.fimportal.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PantallaLogin(vm: FimViewModel) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    // Theme aware background
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val inputColor = MaterialTheme.colorScheme.surface
    
    Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            AnimatedVisibility(visible = isVisible, enter = fadeIn(animationSpec = tween(1000)) + slideInVertically()) {
                Image(
                    painter = painterResource(id = R.drawable.logo_fim),
                    contentDescription = "Logo",
                    modifier = Modifier.size(140.dp).padding(bottom = 24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }
            Text("Hola de nuevo", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = textColor)
            Text("Ingresa a tu portal FIM", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            OutlinedTextField(
                value = vm.matricula, 
                onValueChange = { vm.matricula = it }, 
                label = { Text("Matrícula") }, 
                leadingIcon = { Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.primary) }, 
                singleLine = true, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(24.dp), 
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary, 
                    unfocusedBorderColor = Color.LightGray, 
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedContainerColor = inputColor,
                    unfocusedContainerColor = inputColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = vm.password, 
                onValueChange = { vm.password = it }, 
                label = { Text("Contraseña") }, 
                leadingIcon = { Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.primary) }, 
                visualTransformation = PasswordVisualTransformation(), 
                singleLine = true, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(24.dp), 
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary, 
                    unfocusedBorderColor = Color.LightGray, 
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedContainerColor = inputColor,
                    unfocusedContainerColor = inputColor
                )
            )
            
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = vm.rememberMe, onCheckedChange = { vm.rememberMe = it }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                Text("Recordar sesión", fontSize = 14.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (vm.isLoading) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } else {
                Button(
                    onClick = { vm.doLogin() }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp), 
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), 
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) { 
                    Text("INGRESAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) 
                }
            }
            vm.errorMsg?.let { 
                Spacer(modifier = Modifier.height(16.dp)); 
                Card(colors = CardDefaults.cardColors(containerColor = FimRed.copy(alpha = 0.1f)), shape = RoundedCornerShape(16.dp)) { 
                    Text(it, color = FimRed, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center) 
                } 
            }
        }
    }
}

@Composable
fun PantallaMenu(vm: FimViewModel) {
    val scrollState = rememberScrollState()
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    
    var mostrarDialogoColor by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        //HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Hola,", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                Text(
                    if (vm.nombreAlumno.isNotEmpty()) vm.nombreAlumno.split(" ").firstOrNull() ?: "Alumno" else "Alumno", 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = textColor
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 // SETTINGS BUTTON
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(vm.currentAccentColor.copy(alpha = 0.2f))
                        .clickable { mostrarDialogoColor = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Settings, null, tint = vm.currentAccentColor)
                }

                // LOGOUT BUTTON
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(FimRed.copy(alpha = 0.2f))
                        .clickable { vm.cerrarSesion() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = FimRed)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // MAIN WIDGET: AVANCE
        Text("Tu progreso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        DashboardWidget(
            titulo = "Avance Académico",
            subtitulo = "Ver calificaciones y kárdex",
            icono = Icons.Filled.School,
            colorFondo = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), // Highlighted with Accent
            colorIcono = MaterialTheme.colorScheme.primary,
            onClick = { vm.irAAvance() },
            modifier = Modifier.fillMaxWidth().height(160.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // BENTO GRID - DASHBOARD
        Text("Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardWidget(
                titulo = "Parciales",
                subtitulo = "Ver notas",
                icono = Icons.AutoMirrored.Filled.List,
                colorFondo = MaterialTheme.colorScheme.surface,
                colorIcono = MaterialTheme.colorScheme.primary,
                onClick = { vm.irAParciales() },
                modifier = Modifier.weight(1f).aspectRatio(1f)
            )
            DashboardWidget(
                titulo = "Horario",
                subtitulo = "Semana",
                icono = Icons.Filled.DateRange,
                colorFondo = MaterialTheme.colorScheme.surface,
                colorIcono = MaterialTheme.colorScheme.primary,
                onClick = { vm.irAHorario() },
                modifier = Modifier.weight(1f).aspectRatio(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardWidget(
                titulo = "Deptales",
                subtitulo = "Exámenes",
                icono = Icons.AutoMirrored.Filled.Assignment,
                colorFondo = MaterialTheme.colorScheme.surface,
                colorIcono = MaterialTheme.colorScheme.primary,
                onClick = { vm.irADepartamentales() },
                modifier = Modifier.weight(1f).aspectRatio(1f)
            )
             DashboardWidget(
                titulo = "Asistencia",
                subtitulo = "Profesores",
                icono = Icons.Filled.PersonSearch,
                colorFondo = MaterialTheme.colorScheme.surface,
                colorIcono = MaterialTheme.colorScheme.primary,
                onClick = { vm.irAAsistencia() },
                modifier = Modifier.weight(1f).aspectRatio(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        DashboardWidget(
            titulo = "Credencial",
            subtitulo = "Código QR",
            icono = Icons.Filled.QrCode,
            colorFondo = MaterialTheme.colorScheme.surface, // Dark Card
            colorIcono = MaterialTheme.colorScheme.primary, // Dynamic Accent
            onClick = { vm.irACredencial() },
            modifier = Modifier.fillMaxWidth().height(130.dp) // Increased height to fix cut-off
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // UTILIDADES BETA
         Text("Utilidades Beta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.fillMaxWidth())
         Spacer(modifier = Modifier.height(12.dp))
         
         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
             DashboardWidget(
                titulo = "Termo",
                subtitulo = "Calc. Agua",
                icono = Icons.Filled.WaterDrop,
                colorFondo = MaterialTheme.colorScheme.surface,
                colorIcono = MaterialTheme.colorScheme.primary,
                onClick = { vm.irACalculadoraTermo() },
                modifier = Modifier.weight(1f).aspectRatio(1f)
            )
            DashboardWidget(
                titulo = "Notas",
                subtitulo = "Drive",
                icono = Icons.Filled.Book,
                colorFondo = MaterialTheme.colorScheme.surface, // Dark Card
                colorIcono = MaterialTheme.colorScheme.primary, // Dynamic Accent
                onClick = { vm.irANotasComunidad() },
                modifier = Modifier.weight(1f).aspectRatio(1f)
            )
         }

        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Reportar un problema",
            color = Color.Gray,
            fontSize = 12.sp,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { vm.irASugerencias() }
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
    
    // Dialogo Seleccion Color
    if (mostrarDialogoColor) {
        DialogoSeleccionColor(
            onDismiss = { mostrarDialogoColor = false },
            onColorSelected = { color ->
                vm.updateAccentColor(color)
                mostrarDialogoColor = false
            }
        )
    }
}

@Composable
fun DialogoSeleccionColor(onDismiss: () -> Unit, onColorSelected: (Color) -> Unit) {
    val colores = listOf(
        FimPurple, FimBlue, FimGreen, FimRed, FimOrange, FimTeal, FimGold, Color(0xFFE91E63) // Pink
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Elige tu estilo", color = FimDark, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Selecciona el color de acento de la app:", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colores.take(4).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onColorSelected(color) }
                                .border(2.dp, FimSurface, CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colores.drop(4).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onColorSelected(color) }
                                .border(2.dp, FimSurface, CircleShape)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = FimRed) }
        },
        containerColor = FimSurface
    )
}
