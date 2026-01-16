package com.alex.fimportal.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alex.fimportal.data.*
import com.alex.fimportal.utils.ThermoEngine
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import com.alex.fimportal.ui.theme.*
import androidx.compose.ui.graphics.Color

/** VIEWMODEL PRINCIPAL */
class FimViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = FimRepository()
    private val prefs: SharedPreferences = application.getSharedPreferences("fim_prefs", Context.MODE_PRIVATE)

    var pantallaActual by mutableStateOf(Pantalla.LOGIN)
    var matricula by mutableStateOf("")
    var password by mutableStateOf("")
    var rememberMe by mutableStateOf(false)
    
    // Dynamic Accent Color (Default to FimBlue value)
    var currentAccentColor by mutableStateOf(FimBlue)
    
    init {
        // Load Saved Accent Color
        val savedColorWithAlpha = prefs.getLong("accent_color", FimBlue.value.toLong())
        // Ensure alpha is preserved or re-applied if needed, but saving/loading Long works for ULong color value
        currentAccentColor = Color(savedColorWithAlpha.toULong())
    }
    var isLoading by mutableStateOf(false)
    var errorMsg by mutableStateOf<String?>(null)

    var materiasParciales = mutableStateListOf<Materia>()
    var materiasDeptales = mutableStateListOf<Materia>()
    var avanceAcademico = mutableStateListOf<AvanceItem>()
    var temasReportados = mutableStateListOf<TemaReportado>()
    var asistenciaProfesores = mutableStateListOf<AsistenciaProfesor>()

    /** ESTADO PARA NOTAS DE COMUNIDAD */
    var notasComunidad = mutableStateListOf<NotaComunidad>()
    var notasNavegacionNivel by mutableIntStateOf(0)
    var anioSeleccionado by mutableStateOf("")
    var materiaSeleccionadaNotas by mutableStateOf("")

    /** ESTADO PARA FEEDBACK SYSTEM */
    var sugerencias = mutableStateListOf<Sugerencia>()
    var sugerenciaSeleccionada by mutableStateOf<Sugerencia?>(null)

    var materiaSeleccionadaNombre by mutableStateOf("")
    var debugLog by mutableStateOf("")

    var selIdHorario by mutableStateOf("")
    var selIdProfesor by mutableStateOf("")

    var horario = mutableStateListOf<ClaseHorario>()
    var diaSeleccionado by mutableStateOf(obtenerDiaHoy())
    var credencialBitmap by mutableStateOf<Bitmap?>(null)
    
    var nombreAlumno by mutableStateOf("")

    init {
        val savedMat = prefs.getString("matricula", null)
        val savedPass = prefs.getString("password", null)
        if (!savedMat.isNullOrEmpty() && !savedPass.isNullOrEmpty()) {
            matricula = savedMat
            password = savedPass
            rememberMe = true
            doLogin(auto = true)
        }
    }

    fun updateAccentColor(newColor: Color) {
        currentAccentColor = newColor
        prefs.edit().putLong("accent_color", newColor.value.toLong()).apply()
    }
    
    fun cargarSugerencias() {
         repo.escucharSugerencias { lista ->
             sugerencias.clear()
             sugerencias.addAll(lista)
         }
    }

    fun enviarSugerencia(titulo: String, contenido: String, tipo: String, onResult: (Boolean) -> Unit) {
        val matricula = prefs.getString("matricula", "Anónimo") ?: "Anónimo"
        repo.enviarSugerencia(titulo, contenido, tipo, matricula, onResult)
    }
    
    fun enviarRespuesta(contenido: String) {
        sugerenciaSeleccionada?.let {
            val autor = nombreAlumno.ifEmpty { prefs.getString("matricula", "Anónimo") ?: "Anónimo" }
            repo.enviarRespuesta(it.id, contenido, autor)
        }
    }
    
    fun seleccionarSugerencia(sugerencia: Sugerencia) {
        sugerenciaSeleccionada = sugerencia
        pantallaActual = Pantalla.DETALLE_SUGERENCIA
    }
    
    fun cerrarDetalleSugerencia() {
        sugerenciaSeleccionada = null
        pantallaActual = Pantalla.SUGERENCIAS
    }

    private fun obtenerDiaHoy(): String {
        return try {
            val hoy = LocalDate.now().dayOfWeek
            when(hoy) {
                DayOfWeek.MONDAY -> "Lunes"
                DayOfWeek.TUESDAY -> "Martes"
                DayOfWeek.WEDNESDAY -> "Miércoles"
                DayOfWeek.THURSDAY -> "Jueves"
                DayOfWeek.FRIDAY -> "Viernes"
                else -> "Lunes"
            }
        } catch (e: Exception) { "Lunes" }
    }

    fun doLogin(auto: Boolean = false) {
        if (!auto && (matricula.isEmpty() || password.isEmpty())) {
            errorMsg = "Faltan datos"
            return
        }
        isLoading = true
        errorMsg = null
        val editor = prefs.edit()
        if (rememberMe) {
            editor.putString("matricula", matricula)
            editor.putString("password", password)
        } else {
            editor.remove("matricula")
            editor.remove("password")
        }
        editor.apply()

        viewModelScope.launch {
            if(!auto) delay(500)
            val resultado = repo.login(matricula, password)
            if (resultado == "OK") {
                // Cargar nombre del alumno en segundo plano
                launch {
                    val nombre = repo.obtenerNombreAlumno()
                    if (nombre.isNotEmpty()) nombreAlumno = nombre
                }
                
                repo.escucharNotasComunidad { notas ->
                    notasComunidad.clear()
                    notasComunidad.addAll(notas)
                }
                cargarSugerencias()
                pantallaActual = Pantalla.MENU
            } else {
                if (!auto) errorMsg = resultado
            }
            isLoading = false
        }
    }

    fun irAParciales() { pantallaActual = Pantalla.CALIFICACIONES_PARCIALES; cargarParciales() }
    fun irADepartamentales() { pantallaActual = Pantalla.CALIFICACIONES_DEPTALES; cargarDepartamentales() }
    fun irAAvance() { pantallaActual = Pantalla.AVANCE; cargarAvance() }
    fun irAAsistencia() { pantallaActual = Pantalla.ASISTENCIA_PROFESORES; cargarAsistencia() }
    fun irACalculadoraTermo() { pantallaActual = Pantalla.CALCULADORA_TERMO }
    fun irASugerencias() { pantallaActual = Pantalla.SUGERENCIAS }

    fun irANotasComunidad() {
        pantallaActual = Pantalla.NOTAS_COMUNIDAD
        notasNavegacionNivel = 0
        anioSeleccionado = ""
        materiaSeleccionadaNotas = ""
    }

    fun seleccionarAnioNotas(anio: String) {
        anioSeleccionado = anio
        notasNavegacionNivel = 1
    }

    fun seleccionarMateriaNotas(materia: String) {
        materiaSeleccionadaNotas = materia
        notasNavegacionNivel = 2
        marcarVistas(anioSeleccionado, materia)
    }

    fun retrocederNivelNotas() {
        if (notasNavegacionNivel > 0) notasNavegacionNivel--
        else volverAlMenu()
    }

    fun publicarNota(titulo: String, desc: String, link: String) {
        isLoading = true
        val fecha = LocalDate.now().toString()
        val nuevaNota = NotaComunidad(
            titulo = titulo,
            descripcion = desc,
            link = link,
            matriculaAutor = matricula,
            fecha = fecha,
            anio = anioSeleccionado,
            materia = materiaSeleccionadaNotas
        )
        repo.enviarNotaComunidad(nuevaNota) { exito ->
            isLoading = false
            if(!exito) errorMsg = "Error al publicar nota"
        }
    }

    /** LOGICA NOTIFICACION +1 */
    fun contarNuevas(anio: String, materia: String? = null): Int {
        val notasRelevantes = if (materia == null) {
            notasComunidad.filter { it.anio == anio }
        } else {
            notasComunidad.filter { it.anio == anio && it.materia == materia }
        }
        val totalActual = notasRelevantes.size
        val key = if (materia == null) "count_${anio}" else "count_${anio}_${materia}"
        val vistas = prefs.getInt(key, 0)

        return if (totalActual > vistas) totalActual - vistas else 0
    }

    fun marcarVistas(anio: String, materia: String) {
        val notasMat = notasComunidad.filter { it.anio == anio && it.materia == materia }
        val count = notasMat.size
        prefs.edit().putInt("count_${anio}_${materia}", count).apply()
    }

    fun irATemasReportados(idHorario: String, idProfesor: String, nombreMateria: String) {
        materiaSeleccionadaNombre = nombreMateria
        selIdHorario = idHorario
        selIdProfesor = idProfesor
        debugLog = "Iniciando solicitud..."
        pantallaActual = Pantalla.TEMAS_REPORTADOS
        cargarTemas()
    }

    fun irAHorario() { diaSeleccionado = obtenerDiaHoy(); pantallaActual = Pantalla.HORARIO; cargarHorario() }
    fun irACredencial() { pantallaActual = Pantalla.CREDENCIAL; cargarCredencial() }
    fun volverAlMenu() { pantallaActual = Pantalla.MENU }
    fun volverAAvance() { pantallaActual = Pantalla.AVANCE }

    fun cargarCredencial() {
        isLoading = true
        credencialBitmap = null
        viewModelScope.launch {
            val bytes = repo.descargarPdfBytes()
            if (bytes != null && bytes.isNotEmpty()) {
                val bitmap = renderizarPdf(bytes)
                credencialBitmap = bitmap
            }
            isLoading = false
        }
    }

    private suspend fun renderizarPdf(bytes: ByteArray): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val tempFile = File(context.cacheDir, "temp_credencial.pdf")
                FileOutputStream(tempFile).use { it.write(bytes) }
                val fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fileDescriptor)
                val page = renderer.openPage(0)
                val width = page.width * 2
                val height = page.height * 2
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                fileDescriptor.close()
                return@withContext recortarEspaciosBlancos(bitmap)
            } catch (e: Exception) { e.printStackTrace(); return@withContext null }
        }
    }

    private fun recortarEspaciosBlancos(source: Bitmap): Bitmap {
        var minX = source.width
        var minY = source.height
        var maxX = -1
        var maxY = -1

        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)

        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                val pixel = pixels[y * source.width + x]
                if (pixel != android.graphics.Color.WHITE && pixel != android.graphics.Color.TRANSPARENT) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }
        if (maxX < minX || maxY < minY) return source
        return Bitmap.createBitmap(source, minX, minY, maxX - minX + 1, maxY - minY + 1)
    }

    fun cargarParciales() { isLoading = true; viewModelScope.launch { materiasParciales.clear(); materiasParciales.addAll(repo.obtenerCalificacionesParciales()); isLoading = false } }
    fun cargarDepartamentales() { isLoading = true; viewModelScope.launch { materiasDeptales.clear(); materiasDeptales.addAll(repo.obtenerCalificacionesDepartamentales()); isLoading = false } }
    fun cargarAvance() { isLoading = true; viewModelScope.launch { avanceAcademico.clear(); avanceAcademico.addAll(repo.obtenerAvanceAcademico()); isLoading = false } }

    fun cargarAsistencia() {
        isLoading = true
        viewModelScope.launch {
            asistenciaProfesores.clear()
            asistenciaProfesores.addAll(repo.obtenerAsistenciaProfesores())
            isLoading = false
        }
    }

    fun cargarTemas() {
        if (selIdHorario.isBlank()) return
        isLoading = true
        temasReportados.clear()
        viewModelScope.launch {
            val resultado = repo.obtenerTemasReportados(selIdHorario, selIdProfesor)
            temasReportados.addAll(resultado.lista)
            debugLog = resultado.log
            isLoading = false
        }
    }

    fun cargarHorario() { isLoading = true; viewModelScope.launch { horario.clear(); horario.addAll(repo.obtenerHorario()); isLoading = false } }

    fun cerrarSesion() {
        pantallaActual = Pantalla.LOGIN
        materiasParciales.clear()
        materiasDeptales.clear()
        avanceAcademico.clear()
        temasReportados.clear()
        horario.clear()
        asistenciaProfesores.clear()
        credencialBitmap = null
        FirebaseAuth.getInstance().signOut()
    }
}
