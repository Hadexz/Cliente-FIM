package com.alex.fimportal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alex.fimportal.data.Pantalla
import com.alex.fimportal.ui.*
import com.alex.fimportal.ui.theme.*

class MainActivity : ComponentActivity() {
    // ViewModel survives configuration changes
    private val viewModel: FimViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FIMportalTheme(accentColor = viewModel.currentAccentColor) {
                AppContent(viewModel)
            }
        }
    }
}

@Composable
fun AppContent(vm: FimViewModel) {
    Surface(modifier = Modifier.fillMaxSize(), color = FimBackground) {
        Crossfade(targetState = vm.pantallaActual, animationSpec = tween(500), label = "Navegacion") { pantalla ->
            when (pantalla) {
                Pantalla.LOGIN -> PantallaLogin(vm)
                Pantalla.MENU -> PantallaMenu(vm)
                Pantalla.CALIFICACIONES_PARCIALES -> PantallaNotas(vm, "Calificaciones Parciales", vm.materiasParciales) { vm.cargarParciales() }
                Pantalla.CALIFICACIONES_DEPTALES -> PantallaNotas(vm, "Exámenes Departamentales", vm.materiasDeptales) { vm.cargarDepartamentales() }
                Pantalla.HORARIO -> PantallaHorario(vm)
                Pantalla.CREDENCIAL -> PantallaCredencial(vm)
                Pantalla.AVANCE -> PantallaAvance(vm)
                Pantalla.TEMAS_REPORTADOS -> PantallaTemas(vm)
                Pantalla.CALCULADORA_TERMO -> PantallaCalculadoraTermo(vm)
                Pantalla.ASISTENCIA_PROFESORES -> PantallaAsistencia(vm)
                Pantalla.NOTAS_COMUNIDAD -> PantallaNotasComunidad(vm)
                Pantalla.SUGERENCIAS -> PantallaSugerencias(vm)
                Pantalla.DETALLE_SUGERENCIA -> PantallaDetalleSugerencia(vm)
            }
        }
    }
}