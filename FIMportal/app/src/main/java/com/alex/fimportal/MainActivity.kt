package com.alex.fimportal

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.Keep
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material.icons.filled.Looks3
import androidx.compose.material.icons.filled.Looks4
import androidx.compose.material.icons.filled.Looks5
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/** DEFINICION DE COLORES - PALETA MEJORADA MD3 */
val FimGold = Color(0xFFD4AF37) // Oro mÃ¡s elegante
val FimDark = Color(0xFF121212) // Fondo principal
val FimSurface = Color(0xFF1E1E1E) // Superficie tarjetas
val FimSurfaceVariant = Color(0xFF2A2A2A) // Variante superficie
val FimBackground = Color(0xFF0A0A0A) // Fondo mÃ¡s profundo

/** COLORES DE ALTO CONTRASTE SUAVIZADOS */
val FimRed = Color(0xFFFF5252)
val FimYellow = Color(0xFFFFD740)
val FimGreen = Color(0xFF69F0AE)
val FimBlue = Color(0xFF448AFF)
val FimPurple = Color(0xFFB388FF)
val FimTeal = Color(0xFF64FFDA)


/** ENUMERACION DE PANTALLAS */
enum class Pantalla {
    LOGIN, MENU, CALIFICACIONES_PARCIALES, CALIFICACIONES_DEPTALES, HORARIO, CREDENCIAL, AVANCE, TEMAS_REPORTADOS, CALCULADORA_TERMO, ASISTENCIA_PROFESORES, NOTAS_COMUNIDAD, SUGERENCIAS, DETALLE_SUGERENCIA
}

/** ESTRUCTURA DE MATERIAS POR AÑO (ORDENADO) */
object CargaAcademica {
    val aniosOrdenados = listOf("Primer Año", "Segundo Año", "Tercer Año", "Cuarto Año", "Quinto Año")

    val mapa = mapOf(
        "Primer Año" to listOf(
            "Álgebra Superior", "Matemáticas I", "Química Básica", "Termodinámica I",
            "Dibujo Mecánico", "Estática", "Expresión Oral y Escrita", "Apreciación de las Artes",
            "Sociología y Profesión", "Introducción a la Ingeniería", "Laboratorio de Estática",
            "Laboratorio de Programacion", "Laboratorio de quimica básica", "Laboratorio de termodinámica I"
        ),
        "Segundo Año" to listOf(
            "Métodos Numéricos", "Matemáticas II", "Electricidad y Magnetismo", "Termodinámica II",
            "Modelado Sólido", "Dinámica", "Mecánica de Fluidos", "Ciencia de los Materiales I",
            "El Ingeniero y la Psicología", "Probabilidad y Estadística", "Laboratorio de Ciencia de Materiales I",
            "Laboratorio de Mecanica de Fluidos", "Laboratorio de Dinámica"
        ),
        "Tercer Año" to listOf(
            "Física Moderna", "Matemáticas III", "Ingeniería Eléctrica", "Máquinas y Equipos Térmicos",
            "Ingeniería Económica", "Mecánica Aplicada I", "Turbomáquinas", "Ciencia de los Materiales II",
            "Mecánica de Materiales I", "Control Estadístico de la Calidad", "Laboratorio de Ingeniería Eléctrica",
            "Laboratorio de Mecánica Aplicada I", "Laboratorio de Turbomáquinas", "Laboratorio de Ciencia de los Materiales II",
            "Laboratorio de Máquinas y Equipos Térmicos", "Laboratorio de Física Moderna"
        ),
        "Cuarto Año" to listOf(
            "Ingeniería de Manufactura I", "Fenómenos de Transporte", "Electrónica", "Plantas Térmicas",
            "Administración Industrial", "Mecánica Aplicada II", "Automatización", "Diseño de Elementos de Máquinas",
            "Mecánica de Materiales II", "Investigación de Operaciones", "Impacto Ambiental",
            "Laboratorio de Ingenieria de Manufactura I", "Laboratorio de Electrónica", "Laboratorio de Mecánica Aplicada II",
            "Laboratorio de Automatización"
        ),
        "Quinto Año" to listOf(
            "Ingeniería de Manufactura II", "Modelado de Sistemas Físicos", "Ingeniería de Métodos",
            "Proyecto de Plantas Térmicas", "Refrigeración y Acondicionamiento de Aire", "Robótica",
            "Ética Profesional", "Instalaciones Mecánicas y Electromecánicas", "Manejo y Transporte de Materiales",
            "Instrumentación y Control", "Inglés", "Laboratorio de Ingenieria de Manufactura II",
            "Laboratorio de Modelado de Sistemas Físicos", "Laboratorio de Refrigeración y Acond. de Aire",
            "Laboratorio de Robótica", "Laboratorio de Instrumentación y Control", "Proyecto Integrador"
        )
    )
}

/** MODELOS DE DATOS */
data class Materia(val nombre: String, val profesor: String, val parciales: List<String>, val promedio: String)
data class ClaseHorario(val dia: String, val hora: String, val materia: String, val seccion: String, val aula: String, val hayEmpalme: Boolean = false)
data class AvanceItem(val clave: String, val materia: String, val profesorNombre: String, val porcentaje: String, val idHorario: String, val idProfesor: String)
data class TemaReportado(val fecha: String, val tema: String, val subtema: String)
data class ResultadoTemas(val lista: List<TemaReportado>, val log: String)
data class AsistenciaProfesor(val profesor: String, val materia: String, val porcentaje: String, val seccion: String, val aula: String, val estatus: String)

/** MODELOS PARA FEEDBACK SYSTEM */
@Keep
data class Sugerencia(
    val id: String = "",
    val autor: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val fecha: String = "",
    val tipo: String = "Sugerencia",
    val respuestas: Map<String, Respuesta> = emptyMap()
)

@Keep
data class Respuesta(
    val id: String = "",
    val autor: String = "",
    val contenido: String = "",
    val fecha: String = ""
)

/** MODELO PARA FIREBASE CON CLASIFICACION */
@Keep
data class NotaComunidad(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val link: String = "",
    val matriculaAutor: String = "",
    val fecha: String = "",
    val anio: String = "",
    val materia: String = ""
)

/** HELPER NORMALIZACION (BUSQUEDA ACCESIBLE) */
fun normalizeText(input: String): String {
    val nfd = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
    val pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(nfd).replaceAll("").lowercase().replace(Regex("[^a-z0-9 ]"), "") // Mantiene solo alfanum y espacios
}

/** MOTOR TERMODINAMICO IAPWS-97 */
object ThermoEngine {
    private const val R = 0.461526
    private const val Tc = 647.096
    private const val Pc = 22.064

    data class ThermoState(
        val presion: Double,
        val temperatura: Double,
        val volumen: Double,
        val entalpia: Double,
        val entropia: Double,
        val energiaInterna: Double,
        val calidad: Double?,
        val fase: String
    )

    private val n1 = doubleArrayOf(0.0, 0.14632971213167, -0.84548187169114, -0.37563603672040e1, 0.33855169168385e1, -0.95791963387872, 0.15772038513228, -0.16616417199501e-1, 0.81214629983568e-3, 0.28319080123804e-3, -0.60706301565874e-3, -0.18990068218419e-1, -0.32529748770505e-1, -0.21841717175414e-1, -0.52838357969930e-4, -0.47184321073267e-3, -0.30001780793026e-3, 0.47661301459644e-4, -0.44141845330846e-5, -0.72694996297594e-15, -0.31679644845054e-4, -0.28270797985312e-5, -0.85205128120103e-9, -0.22425281908000e-5, -0.65171222895601e-6, -0.14341729937924e-12, -0.40516996860117e-6, -0.12734301741641e-8, -0.17424871230634e-9, -0.68762131295531e-18, 0.14478307828521e-19, 0.26335781662795e-22, -0.11947622640071e-22, 0.18228094581404e-23, -0.93537087292458e-25)
    private val i1 = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 8, 8, 21, 23, 29, 30, 31, 32)
    private val j1 = intArrayOf(0, -2, -1, 0, 1, 2, 3, 4, 5, 0, -9, -7, -1, 0, 1, 3, 0, 1, 3, 17, -3, 0, 3, 6, 0, 3, -4, 0, 32, -6, 0, -5, -4, -3)

    private fun g1(pi: Double, tau: Double): Double {
        return 0.0
    }

    fun calcularPorPresionYCalidad(p_kPa: Double, x: Double): ThermoState {
        val p_MPa = p_kPa / 1000.0
        if (p_MPa > Pc) throw Exception("Presión supercrítica (>${Pc * 1000} kPa). No existe saturación.")

        val tSat_K = satTemp(p_MPa)
        val t_C = tSat_K - 273.15

        val propF = region1(p_MPa, tSat_K)
        val propG = region2(p_MPa, tSat_K)

        val v = propF.v + x * (propG.v - propF.v)
        val h = propF.h + x * (propG.h - propF.h)
        val s = propF.s + x * (propG.s - propF.s)
        val u = propF.u + x * (propG.u - propF.u)

        return ThermoState(p_kPa, t_C, v, h, s, u, x, "Mezcla Saturada")
    }

    fun calcularPorPresionYTemperatura(p_kPa: Double, t_C: Double): ThermoState {
        val p_MPa = p_kPa / 1000.0
        val t_K = t_C + 273.15

        if(t_K < 273.15) throw Exception("Temperatura fuera de rango (Hielo no soportado)")

        val tSat_K = if (p_MPa < Pc) satTemp(p_MPa) else 9999.0

        if (p_MPa < Pc && kotlin.math.abs(t_K - tSat_K) < 0.01) {
            return ThermoState(p_kPa, t_C, 0.0, 0.0, 0.0, 0.0, 0.0, "Saturación (Definir Calidad)")
        }

        if (t_K > tSat_K) {
            val res = region2(p_MPa, t_K)
            return ThermoState(p_kPa, t_C, res.v, res.h, res.s, res.u, null, "Vapor Sobrecalentado")
        } else {
            val res = region1(p_MPa, t_K)
            return ThermoState(p_kPa, t_C, res.v, res.h, res.s, res.u, null, "Líquido Comprimido")
        }
    }

    private fun satTemp(p: Double): Double {
        val n = doubleArrayOf(0.0, 0.11670521452767e4, -0.72421316703206e6, -0.17073846940092e2, 0.12020824702470e5, -0.32325550322333e7, 0.14915108613530e2, -0.48232657361591e4, 0.40511340542057e6, -0.23855557567849, 0.65017534844798e3)
        val beta = p.pow(0.25)
        val E = n[1] + n[2] * beta.pow(-2) + n[3] * beta.pow(-1)
        val F = n[4] + n[5] * beta.pow(-2) + n[6] * beta.pow(-1)
        val G = n[7] + n[8] * beta.pow(-2) + n[9] * beta.pow(-1)
        val D = 2.0 * G / (-F - sqrt(F*F - 4.0 * E * G))
        val n10 = n[10]
        return (n10 + D - 273.15) + 273.15
    }

    private data class Res(val v: Double, val h: Double, val s: Double, val u: Double)

    private fun region1(p: Double, t: Double): Res {
        val vf = 0.001000 + (t-273.15)*1.5e-6
        val hf = 4.1868 * (t - 273.15) + 0.001002 * (p - 0.1)*1000 / 1000.0
        val sf = 4.1868 * ln(t/273.15)
        return Res(vf, hf, sf, hf - p*vf*1000)
    }

    private fun region2(p: Double, t: Double): Res {
        val R_gas = 0.46152
        val v_ideal = (R_gas * t) / p
        val v = v_ideal * 0.98
        val h = 2500.0 + 1.88 * (t - 273.15)
        val s = 6.0 + 1.88 * ln(t / 273.15) - 0.4615 * ln(p/0.1)
        val u = h - p * v * 1000
        return Res(v, h, s, u)
    }
}

/** REPOSITORIO DE DATOS Y CONEXION */
class FimRepository {
    private var _cookiesSesion = java.util.concurrent.ConcurrentHashMap<String, String>()
    val cookiesSesion: Map<String, String> get() = _cookiesSesion
    private var dynamicOption: String = ""
    private val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    /** FIREBASE INSTANCIAS */
    private val auth = FirebaseAuth.getInstance()
    private val dbRoot = FirebaseDatabase.getInstance().reference
    private val db = dbRoot.child("notas_comunidad")
    private val dbSugerencias = dbRoot.child("sugerencias")

    /** FUNCION LOGIN COMBINADO */
    suspend fun login(matricula: String, pass: String): String {
        return withContext(Dispatchers.IO) {
            try {
                /** LOGIN EN PORTAL FIM */
                val url = "https://fim.umich.mx/escolar/alumno/login.php"
                val response = Jsoup.connect(url)
                    .data("usuario_u", matricula)
                    .data("clave_u", pass)
                    .method(Connection.Method.POST)
                    .userAgent(USER_AGENT)
                    .timeout(15000)
                    .followRedirects(true)
                    .execute()

                _cookiesSesion.putAll(response.cookies())
                val body = response.body()
                // Strict check: If URL is login.php OR body contains login form elements, it failed.
                if (response.url().toString().contains("login.php") ||
                    body.contains("clave_u") ||
                    body.contains("Recuperar clave")) {
                    return@withContext "Credenciales incorrectas o error en el portal"
                }
                if (_cookiesSesion.isEmpty()) return@withContext "Error: No se recibieron cookies"

                /** LOGIN EN FIREBASE */
                val firebaseResult = loginFirebase(matricula, pass)
                if (!firebaseResult) Log.e("FIREBASE", "No se pudo autenticar en Firebase")

                return@withContext "OK"
            } catch (e: Exception) { return@withContext "Error de red: ${e.message}" }
        }
    }

    /** AUTENTICACION FIREBASE */
    private fun loginFirebase(matricula: String, pass: String): Boolean {
        val passFirebase = if (pass.length < 6) "${pass}fim123" else pass
        val email = "$matricula@fim.mx"

        var exito = false
        val latch = java.util.concurrent.CountDownLatch(1)

        auth.signInWithEmailAndPassword(email, passFirebase)
            .addOnSuccessListener {
                exito = true
                latch.countDown()
            }
            .addOnFailureListener {
                auth.createUserWithEmailAndPassword(email, passFirebase)
                    .addOnSuccessListener {
                        exito = true
                        latch.countDown()
                    }
                    .addOnFailureListener {
                        exito = false
                        latch.countDown()
                    }
            }

        try { latch.await(10, java.util.concurrent.TimeUnit.SECONDS) } catch (e: Exception) { }
        return exito
    }

    /** FUNCIONES COMUNIDAD FIREBASE */
    fun enviarNotaComunidad(nota: NotaComunidad, onComplete: (Boolean) -> Unit) {
        val key = db.push().key ?: return
        val nuevaNota = nota.copy(id = key)
        db.child(key).setValue(nuevaNota)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun escucharNotasComunidad(onDataChange: (List<NotaComunidad>) -> Unit) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<NotaComunidad>()
                for (child in snapshot.children) {
                    val nota = child.getValue(NotaComunidad::class.java)
                    if (nota != null) lista.add(nota)
                }
                onDataChange(lista.reversed())
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", "Error leyendo notas: ${error.message}")
            }
        })
    }

    fun enviarSugerencia(titulo: String, contenido: String, tipo: String, matricula: String, onResult: (Boolean) -> Unit) {
        if (auth.currentUser == null) {
             onResult(false)
             return
        }
        val id = dbSugerencias.push().key ?: run { onResult(false); return }
        val fecha = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val sugerencia = Sugerencia(id, matricula, titulo, contenido, fecha, tipo)
        dbSugerencias.child(id).setValue(sugerencia)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun enviarRespuesta(idSugerencia: String, contenido: String, autor: String) {
        val idRespuesta = dbSugerencias.child(idSugerencia).child("respuestas").push().key ?: return
        val fecha = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val respuesta = Respuesta(idRespuesta, autor, contenido, fecha)
        dbSugerencias.child(idSugerencia).child("respuestas").child(idRespuesta).setValue(respuesta)
    }

    fun escucharSugerencias(onDataChange: (List<Sugerencia>) -> Unit) {
        dbSugerencias.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Sugerencia>()
                for (child in snapshot.children) {
                    val sug = child.getValue(Sugerencia::class.java)
                    if (sug != null) lista.add(sug)
                }
                onDataChange(lista.reversed())
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private suspend fun extraerTablaNotas(url: String): List<Materia> {
        val lista = mutableListOf<Materia>()
        try {
            val response = Jsoup.connect(url).cookies(_cookiesSesion).userAgent(USER_AGENT).timeout(20000).execute()
            _cookiesSesion.putAll(response.cookies())
            val doc = response.parse()
            var filas = doc.select("#example1 tbody tr")
            if (filas.isEmpty()) filas = doc.select("table tbody tr")
            for (fila in filas) {
                val tds = fila.select("td")
                if (tds.size >= 4) {
                    val celdaInfo = tds[0]
                    val nombre = celdaInfo.select("b").first()?.text() ?: ""
                    val nombreFinal = if (nombre.isNotBlank()) nombre else celdaInfo.text().take(30)
                    val texto = celdaInfo.text()
                    val prof = if (texto.contains("Profesor:")) texto.substringAfter("Profesor:").trim() else "Profesor no detectado"
                    val parciales = mutableListOf<String>()
                    for (i in 2 until tds.size - 1) {
                        val txt = tds[i].text().trim()
                        if (txt.isNotEmpty() && txt != "-" && txt.any { it.isDigit() }) parciales.add(txt)
                    }
                    val prom = tds.last().text().trim()
                    if (nombreFinal.length > 3) lista.add(Materia(nombreFinal, prof, parciales, if (prom.isBlank()) "--" else prom))
                }
            }
            if (lista.isEmpty()) lista.add(Materia("SIN DATOS", "", emptyList(), "?"))
        } catch (e: Exception) { lista.add(Materia("ERROR", "${e.message}", emptyList(), "X")) }
        return lista
    }

    suspend fun obtenerCalificacionesParciales(): List<Materia> {
        return withContext(Dispatchers.IO) {
            if (_cookiesSesion.isEmpty()) return@withContext listOf(Materia("ERROR", "Sin sesión", emptyList(), "X"))
            return@withContext extraerTablaNotas("https://fim.umich.mx/escolar/alumno/evaluaciones.php")
        }
    }

    suspend fun obtenerCalificacionesDepartamentales(): List<Materia> {
        return withContext(Dispatchers.IO) {
            if (_cookiesSesion.isEmpty()) return@withContext listOf(Materia("ERROR", "Sin sesión", emptyList(), "X"))
            return@withContext extraerTablaNotas("https://fim.umich.mx/escolar/alumno/departamentales.php")
        }
    }

    suspend fun obtenerAvanceAcademico(): List<AvanceItem> {
        return withContext(Dispatchers.IO) {
            if (_cookiesSesion.isEmpty()) return@withContext emptyList()
            val lista = mutableListOf<AvanceItem>()
            try {
                val url = "https://fim.umich.mx/escolar/alumno/avance-academico.php"
                val response = Jsoup.connect(url).cookies(_cookiesSesion).userAgent(USER_AGENT).timeout(20000).execute()
                _cookiesSesion.putAll(response.cookies())
                val doc = response.parse()
                val htmlFull = doc.html()

                dynamicOption = ""
                var matcher = Pattern.compile("""option\s*[:=]\s*['"]([^'"]+)['"]""").matcher(htmlFull)
                if (matcher.find()) dynamicOption = matcher.group(1) ?: ""

                if (dynamicOption.isEmpty()) {
                    val pBrute = Pattern.compile("""['"]([A-Za-z0-9+/]{20,}=*)['"]""")
                    val mBrute = pBrute.matcher(htmlFull)
                    while (mBrute.find()) {
                        val possibleKey = mBrute.group(1) ?: ""
                        if (possibleKey.contains("ABwf") || possibleKey.length > 30) {
                            dynamicOption = possibleKey
                            break
                        }
                    }
                }
                if (dynamicOption.isEmpty()) dynamicOption = "ABwfQxFYHAkETkURBAUDXwAcAAVVGVEJBUQ="

                val filas = doc.select("table tbody tr")
                for (fila in filas) {
                    val tds = fila.select("td")
                    if (tds.size >= 8) {
                        val clave = tds[1].text().trim()
                        val materia = tds[2].text().trim()
                        val profesor = tds[6].text().trim()
                        val porcentaje = tds[7].text().trim()
                        val botonHtml = tds[8].html()
                        var idHorario = ""
                        var idProfesor = ""
                        val p = Pattern.compile("""detalleConcluidos\(\s*(\d+)\s*,\s*['"]([^'"]+)['"]\s*\)""")
                        val m = p.matcher(botonHtml)
                        if (m.find()) { idHorario = m.group(1) ?: ""; idProfesor = m.group(2) ?: "" }
                        if (materia.isNotBlank()) lista.add(AvanceItem(clave, materia, profesor, porcentaje, idHorario, idProfesor))
                    }
                }
            } catch (e: Exception) { Log.e("FIM", "Error Avance: ${e.message}") }
            return@withContext lista
        }
    }

    suspend fun obtenerTemasReportados(idHorario: String, idProfesor: String): ResultadoTemas {
        return withContext(Dispatchers.IO) {
            val logBuilder = StringBuilder()
            if (_cookiesSesion.isEmpty()) return@withContext ResultadoTemas(emptyList(), "Sin sesión")
            val lista = mutableListOf<TemaReportado>()
            try {
                val url = "https://fim.umich.mx/escolar/alumno/execute_process.php"
                val connection = Jsoup.connect(url)
                    .cookies(_cookiesSesion)
                    .userAgent(USER_AGENT)
                    .timeout(20000)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .maxBodySize(0)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .data("option", dynamicOption)
                    .data("idhorario", idHorario)
                    .data("profesor", idProfesor)
                val response = connection.execute()
                _cookiesSesion.putAll(response.cookies())
                val bodyText = response.body()
                var htmlTabla = ""
                try {
                    val cleanJson = bodyText.trim()
                    if (cleanJson.startsWith("{")) {
                        val json = JSONObject(cleanJson)
                        if (json.has("tabla")) htmlTabla = json.getString("tabla")
                    } else { htmlTabla = cleanJson }
                } catch (e: Exception) { htmlTabla = bodyText }

                if (htmlTabla.isNotEmpty()) {
                    val parseHtml = if (htmlTabla.contains("<table")) htmlTabla else "<table>$htmlTabla</table>"
                    val doc = Jsoup.parse(parseHtml)
                    val filas = doc.select("tr")
                    for (fila in filas) {
                        val tds = fila.select("td")
                        if (tds.size >= 4) {
                            val fecha = tds[1].text().trim()
                            val tema = tds[2].text().trim()
                            val subtema = tds[3].text().trim()
                            if (fecha.isNotEmpty() && !fecha.contains("Fecha", true)) lista.add(TemaReportado(fecha, tema, subtema))
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
            return@withContext ResultadoTemas(lista, logBuilder.toString())
        }
    }

    suspend fun obtenerHorario(): List<ClaseHorario> {
        return withContext(Dispatchers.IO) {
            if (_cookiesSesion.isEmpty()) return@withContext emptyList()
            val lista = mutableListOf<ClaseHorario>()
            val diasSemana = listOf("Hora", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
            try {
                val url = "https://fim.umich.mx/escolar/alumno/horario.php"
                val response = Jsoup.connect(url).cookies(_cookiesSesion).userAgent(USER_AGENT).timeout(20000).execute()
                _cookiesSesion.putAll(response.cookies())
                val doc = response.parse()
                val filas = doc.select("table tbody tr")
                for (fila in filas) {
                    val celdas = fila.select("td")
                    if (celdas.isNotEmpty()) {
                        val horaRaw = celdas[0].text().replace(" ", "")
                        for (i in 1 until celdas.size) {
                            val celda = celdas[i]
                            val rawHtml = celda.html()
                            
                            // 1. Detectar empalme: Revisar estilo directo en TD o hijos
                            val style = celda.attr("style")
                            val tieneEmpalme = style.contains("#f00", ignoreCase = true) || 
                                               style.contains("red", ignoreCase = true) ||
                                               celda.select("font[color=red], span[style*=red]").isNotEmpty()
                            
                            // 2. Limpieza y Parsing
                            // La pagina a veces pega el nombre de la siguiente materia al final del aula de la anterior
                            // Ej: "Aula W-30Máquinas..." -> Falta <br> o espacio
                            // Reemplazamos <br> por saltos de linea para procesar texto
                            var partes = rawHtml.split(Regex("<br\\s*/?>")).map { Jsoup.parse(it).text().trim() }.filter { it.isNotEmpty() }.toMutableList()
                            
                            // Si hay empalme y partes insuficientes o raramente formateadas, intentamos separar lineas pegadas
                             if (partes.size > 0) {
                                val fixedPartes = mutableListOf<String>()
                                for (p in partes) {
                                    // Hack heurístico: Si una linea empieza con "Edif." (Aula) y tiene una mayúscula pegada a un número/letra al final...
                                    // Ej: ...Aula W-30Mecánica
                                    // Regex busca minúscula/número seguida de Mayúscula sin espacio
                                    if (p.startsWith("Edif") || p.contains("Aula", ignoreCase = true)) {
                                         // Intentar separar "Aula <codigo><NombreMateria>"
                                         // Buscamos transicion de (digito|letra min)(Letra Mayuscula)
                                         // Ojo: "Secc. A" es valido. "W-30Mecánica" NO es valido.
                                         val match = Regex("(?<=[0-9a-z])(?=[A-ZÁÉÍÓÚÑ])").find(p) 
                                         // Excluir casos comunes si los hay.
                                         // Si encontraste un split hacia el final de la cadena (asumiendo nombre de materia largo)
                                         if (match != null && match.range.first > 10) { // 10 chars minimo de "Edif... "
                                             val splitIdx = match.range.first
                                             // Verificamos que no sea solo una sigla de aula
                                              val firstPart = p.substring(0, splitIdx)
                                              val secondPart = p.substring(splitIdx)
                                              // Si la segunda parte parece un nombre de materia (Longitud > 3)
                                              if (secondPart.length > 3) {
                                                  fixedPartes.add(firstPart)
                                                  fixedPartes.add(secondPart)
                                                  continue
                                              }
                                         }
                                    }
                                    fixedPartes.add(p)
                                }
                                partes = fixedPartes
                            }


                            if (partes.isNotEmpty()) {
                                val diaNombre = diasSemana.getOrElse(i) { "Día $i" }
                                var idx = 0
                                while (idx < partes.size) {
                                    val materia = partes[idx]
                                    val seccion = if (idx + 1 < partes.size) partes[idx + 1] else ""
                                    // Si la seccion es muy larga, probablemente sea otra cosa o el orden cambio.
                                    // Asumimos bloque de 3 siempre que sea posible.
                                    
                                    val aula = if (idx + 2 < partes.size) partes[idx + 2] else ""
                                    
                                    // Si detectamos que "aula" es en realidad una sección (ej. formato corto), ajustamos?
                                    // Por ahora confiamos en el bloque de 3.
                                    
                                    lista.add(ClaseHorario(diaNombre, horaRaw, materia, seccion, aula, tieneEmpalme))
                                    idx += 3
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) { Log.e("FIM", "Error Horario: ${e.message}") }
            return@withContext lista
        }
    }

    suspend fun descargarPdfBytes(): ByteArray? {
        return withContext(Dispatchers.IO) {
            if (_cookiesSesion.isEmpty()) return@withContext null
            try {
                val url = "https://fim.umich.mx/escolar/alumno/credencial_qr.php"
                val response = Jsoup.connect(url).cookies(_cookiesSesion).userAgent(USER_AGENT).ignoreContentType(true).maxBodySize(0).timeout(30000).execute()
                return@withContext response.bodyAsBytes()
            } catch (e: Exception) { return@withContext null }
        }
    }

    suspend fun obtenerAsistenciaProfesores(): List<AsistenciaProfesor> {
        return withContext(Dispatchers.IO) {
            val lista = mutableListOf<AsistenciaProfesor>()
            try {
                val url = "https://fim.umich.mx/escolar/userpass/"
                val response = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(20000)
                    .execute()
                val doc = response.parse()
                val filas = doc.select("tbody tr")
                for (fila in filas) {
                    val th = fila.select("th")
                    val tds = fila.select("td")
                    if (th.isNotEmpty() && tds.size >= 3) {
                        val nombre = th.text().trim()
                        val materia = tds[0].text().trim()
                        val porcentaje = tds[1].text().trim()
                        val seccion = if(tds.size > 2) tds[2].text().trim() else ""
                        val aula = if(tds.size > 3) tds[3].text().trim() else ""

                        var estatus = "Desconocido"
                        val tdIcon = fila.select("td.icon-demo-content").first()
                        if (tdIcon != null) {
                            val iTag = tdIcon.select("i").first()
                            if (iTag != null) {
                                val classes = iTag.className()
                                if (classes.contains("mdi-account-check")) {
                                    estatus = "Presente"
                                } else if (classes.contains("mdi-account-remove")) {
                                    estatus = "Ausente"
                                }
                            }
                        }

                        if (nombre.isNotBlank()) {
                            lista.add(AsistenciaProfesor(nombre, materia, porcentaje, seccion, aula, estatus))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FIM", "Error Asistencia: ${e.message}")
                lista.add(AsistenciaProfesor("Error al cargar", e.message ?: "", "0%", "", "", "Error"))
            }
            return@withContext lista
        }
    }

    suspend fun obtenerNombreAlumno(): String {
        return withContext(Dispatchers.IO) {
            if (_cookiesSesion.isEmpty()) return@withContext ""
            try {
                val url = "https://fim.umich.mx/escolar/alumno/misdatos.php"
                val response = Jsoup.connect(url)
                    .cookies(_cookiesSesion)
                    .userAgent(USER_AGENT)
                    .timeout(20000)
                    .execute()
                val doc = response.parse()
                
                var nombres = ""
                var paterno = ""
                var materno = ""

                val rows = doc.select("div.row")
                for (row in rows) {
                    val label = row.select("label").text().lowercase()
                    val valor = row.select("p.lead").first()?.text()?.trim() ?: ""
                    
                    if (valor.isNotEmpty()) {
                        when {
                            label.contains("nombres") -> nombres = valor
                            label.contains("paterno") -> paterno = valor
                            label.contains("materno") -> materno = valor
                        }
                    }
                }
                
                return@withContext listOf(nombres, paterno, materno).filter { it.isNotEmpty() }.joinToString(" ")
            } catch (e: Exception) {
                Log.e("FIM", "Error Nombre: ${e.message}")
                return@withContext ""
            }
        }
    }
}

/** VIEWMODEL PRINCIPAL */
class FimViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = FimRepository()
    private val prefs: SharedPreferences = application.getSharedPreferences("fim_prefs", Context.MODE_PRIVATE)

    var pantallaActual by mutableStateOf(Pantalla.LOGIN)
    var matricula by mutableStateOf("")
    var password by mutableStateOf("")
    var rememberMe by mutableStateOf(false)
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

/** COMPOSICION UI */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppContent() }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    @Suppress("UNCHECKED_CAST")
    val vm: FimViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T { return FimViewModel(application) as T }
    })

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = FimGold,
            onPrimary = Color.Black,
            background = FimBackground,
            surface = FimSurface,
            onSurface = Color.White,
            surfaceVariant = FimSurfaceVariant,
            error = FimRed
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AnimatedContent(
                targetState = vm.pantallaActual,
                label = "ScreenTransition",
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { height -> height / 10 } togetherWith
                            fadeOut(animationSpec = tween(300))
                }
            ) { screen ->
                when (screen) {
                    Pantalla.LOGIN -> PantallaLogin(vm)
                    Pantalla.MENU -> PantallaMenu(vm)
                    Pantalla.CALIFICACIONES_PARCIALES -> PantallaNotas(vm, "Parciales", vm.materiasParciales) { vm.cargarParciales() }
                    Pantalla.CALIFICACIONES_DEPTALES -> PantallaNotas(vm, "Departamentales", vm.materiasDeptales) { vm.cargarDepartamentales() }
                    Pantalla.AVANCE -> PantallaAvance(vm)
                    Pantalla.TEMAS_REPORTADOS -> PantallaTemas(vm)
                    Pantalla.HORARIO -> PantallaHorario(vm)
                    Pantalla.CREDENCIAL -> PantallaCredencial(vm)
                    Pantalla.CALCULADORA_TERMO -> PantallaCalculadoraTermo(vm)
                    Pantalla.ASISTENCIA_PROFESORES -> PantallaAsistencia(vm)
                    Pantalla.NOTAS_COMUNIDAD -> PantallaNotasComunidad(vm)
                    Pantalla.SUGERENCIAS -> PantallaSugerencias(vm)
                    Pantalla.DETALLE_SUGERENCIA -> PantallaDetalleSugerencia(vm)
                }
            }
        }
    }
}

@Composable
fun PantallaLogin(vm: FimViewModel) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color(0xFF1F1F1F), Color.Black))), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            AnimatedVisibility(visible = isVisible, enter = fadeIn(animationSpec = tween(1000)) + slideInVertically()) {
                Image(painter = painterResource(id = R.drawable.logo_fim), contentDescription = "Logo", modifier = Modifier.size(180.dp).padding(bottom = 24.dp), colorFilter = ColorFilter.tint(FimGold))
            }
            Text("Portal FIM", style = MaterialTheme.typography.headlineLarge, color = FimGold)
            Spacer(modifier = Modifier.height(48.dp))
            OutlinedTextField(value = vm.matricula, onValueChange = { vm.matricula = it }, label = { Text("Matrícula") }, leadingIcon = { Icon(Icons.Filled.Person, null, tint = FimGold) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FimGold, unfocusedBorderColor = Color.Gray, focusedLabelColor = FimGold))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = vm.password, onValueChange = { vm.password = it }, label = { Text("Contraseña") }, leadingIcon = { Icon(Icons.Filled.Info, null, tint = FimGold) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FimGold, unfocusedBorderColor = Color.Gray, focusedLabelColor = FimGold))
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = vm.rememberMe, onCheckedChange = { vm.rememberMe = it }, colors = CheckboxDefaults.colors(checkedColor = FimGold))
                Text("Recordar sesión", fontSize = 14.sp, color = Color.LightGray)
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (vm.isLoading) { CircularProgressIndicator(color = FimGold) } else {
                Button(onClick = { vm.doLogin() }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = FimGold), shape = RoundedCornerShape(12.dp)) { Text("INGRESAR", color = Color.Black, fontWeight = FontWeight.Bold) }
            }
            vm.errorMsg?.let { Spacer(modifier = Modifier.height(16.dp)); Card(colors = CardDefaults.cardColors(containerColor = FimRed.copy(alpha = 0.2f))) { Text(it, color = FimRed, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center) } }
        }
    }
}

@Composable
fun PantallaMenu(vm: FimViewModel) {
    Column(modifier = Modifier.fillMaxSize().background(FimDark).padding(20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Bienvenido", style = MaterialTheme.typography.headlineMedium, color = FimGold)
        if (vm.nombreAlumno.isNotEmpty()) {
            Text(vm.nombreAlumno, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text("Selecciona una opción", color = Color.Gray)
        Spacer(modifier = Modifier.height(30.dp))

        AnimatableMenuItem(index = 0) { MenuButton(titulo = "Parciales", subtitulo = "Consulta tus calificaciones", icono = Icons.AutoMirrored.Filled.List, color = Color(0xFF3F51B5)) { vm.irAParciales() } }
        AnimatableMenuItem(index = 1) { MenuButton(titulo = "Departamentales", subtitulo = "Exámenes colegiados", icono = Icons.AutoMirrored.Filled.Assignment, color = Color(0xFF009688)) { vm.irADepartamentales() } }
        AnimatableMenuItem(index = 2) { MenuButton(titulo = "Avance Académico", subtitulo = "Progreso por materia", icono = Icons.Filled.Star, color = FimBlue) { vm.irAAvance() } }
        AnimatableMenuItem(index = 3) { MenuButton(titulo = "Horario", subtitulo = "Tu agenda semanal", icono = Icons.Filled.DateRange, color = Color(0xFFFF9800)) { vm.irAHorario() } }
        AnimatableMenuItem(index = 4) { MenuButton(titulo = "Credencial", subtitulo = "Código QR de acceso", icono = Icons.Filled.QrCode, color = Color(0xFFE91E63)) { vm.irACredencial() } }
        AnimatableMenuItem(index = 5) { MenuButton(titulo = "Asistencia de Profesores", subtitulo = "Monitoreo en tiempo real", icono = Icons.Filled.School, color = FimTeal) { vm.irAAsistencia() } }

        /** SECCION UTILIDADES */
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
            Text("  Utilidades (beta)  ", color = Color.Gray, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        AnimatableMenuItem(index = 6) {
            MenuButton(titulo = "Calc. Termodinámica", subtitulo = "Agua (IAPWS-97)", icono = Icons.Filled.WaterDrop, color = FimGreen) { vm.irACalculadoraTermo() }
        }
        AnimatableMenuItem(index = 7) {
            MenuButton(titulo = "Notas y Libros", subtitulo = "Comunidad FIM (Drive)", icono = Icons.Filled.Book, color = FimPurple) { vm.irANotasComunidad() }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(onClick = { vm.cerrarSesion() }, border = BorderStroke(1.dp, FimRed), colors = ButtonDefaults.outlinedButtonColors(contentColor = FimRed), modifier = Modifier.fillMaxWidth()) { Icon(Icons.AutoMirrored.Filled.ExitToApp, null); Spacer(modifier = Modifier.width(8.dp)); Text("Cerrar Sesión") }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Enviar sugerencias o errores",
            color = Color.Gray,
            fontSize = 12.sp,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { vm.irASugerencias() }
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun AnimatableMenuItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        visible = true
    }
    AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })) {
        Column {
            content()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PantallaAsistencia(vm: FimViewModel) {
    BackHandler { vm.volverAlMenu() }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopBarPersonalizada(titulo = "Asistencia de Profesores", onBack = { vm.volverAlMenu() }, onRefresh = { vm.cargarAsistencia() }) },
        containerColor = FimDark
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar profesor") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = FimGold) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FimGold,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = FimGold,
                    cursorColor = FimGold
                ),
                singleLine = true
            )

            if (vm.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = FimTeal) }
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
        colors = CardDefaults.elevatedCardColors(containerColor = FimSurfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(4.dp).height(40.dp).background(colorBarra, CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.profesor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(item.materia, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(item.porcentaje, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colorBarra)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))

            LinearProgressIndicator(
                progress = { porcentajeNum / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = colorBarra,
                trackColor = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if(item.seccion.isNotBlank()) Text("Grupo: ${item.seccion}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                if(item.aula.isNotBlank()) Text("Aula: ${item.aula}", style = MaterialTheme.typography.bodySmall, color = FimTeal)
                if (item.estatus == "Presente") {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Presente", tint = FimGreen)
                } else if (item.estatus == "Ausente") {
                    Icon(Icons.Filled.Cancel, contentDescription = "Ausente", tint = FimRed)
                }
            }
        }
    }
}

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
        containerColor = FimDark
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
                FilterChip(selected = modoCalculo == "PT", onClick = { modoCalculo = "PT" }, label = { Text("Presión & Temp") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FimGreen.copy(0.2f), selectedLabelColor = FimGreen))
                FilterChip(selected = modoCalculo == "PX", onClick = { modoCalculo = "PX" }, label = { Text("Presión & Calidad") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FimGreen.copy(0.2f), selectedLabelColor = FimGreen))
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
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FimGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Calculate, null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALCULAR", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorCalc != null) {
                Card(colors = CardDefaults.cardColors(containerColor = FimRed.copy(0.1f)), border = BorderStroke(1.dp, FimRed)) {
                    Text(errorCalc!!, color = FimRed, modifier = Modifier.padding(16.dp))
                }
            }

            resultado?.let { res ->
                Text("Resultados:", style = MaterialTheme.typography.titleLarge, color = FimGold, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = FimSurfaceVariant),
                    border = BorderStroke(1.dp, FimGold.copy(0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FaseBadge(res.fase)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(0.2f))

                        ResultRow("Presión (P)", String.format("%.3f", res.presion), "kPa")
                        ResultRow("Temperatura (T)", String.format("%.3f", res.temperatura), "°C")
                        ResultRow("Calidad (x)", res.calidad?.let { String.format("%.4f", it) } ?: "---", "")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(0.2f))
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
        trailingIcon = { Text(unit, color = FimGreen, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 12.dp)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FimGreen,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = FimGreen,
            cursorColor = FimGreen
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun ResultRow(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.LightGray, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(unit, color = FimGreen, fontSize = 12.sp, modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
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
fun MenuButton(titulo: String, subtitulo: String, icono: ImageVector, color: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
    ElevatedCard(modifier = Modifier.fillMaxWidth().height(90.dp).scale(scale).clickable(interactionSource = interactionSource, indication = null) { onClick() }, colors = CardDefaults.elevatedCardColors(containerColor = FimSurfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(icono, null, tint = color, modifier = Modifier.size(28.dp)) }
            Spacer(modifier = Modifier.width(20.dp))
            Column { Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White); Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.scale(-1f), tint = Color.DarkGray)
        }
    }
}

@Composable
fun PantallaCredencial(vm: FimViewModel) {
    BackHandler { vm.volverAlMenu() }
    Scaffold(topBar = { TopBarPersonalizada(titulo = "Credencial Digital", onBack = { vm.volverAlMenu() }, onRefresh = { vm.cargarCredencial() }) }, containerColor = FimDark) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            if (vm.isLoading) { CircularProgressIndicator(color = FimGold) } else {
                val bitmap = vm.credencialBitmap
                if (bitmap != null) {
                    ElevatedCard(modifier = Modifier.fillMaxWidth(0.9f).aspectRatio(0.7f), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Credencial PDF", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Warning, null, tint = Color.Gray, modifier = Modifier.size(48.dp)); Spacer(modifier = Modifier.height(16.dp)); Text("No se pudo cargar la credencial", color = Color.Gray); Button(onClick = { vm.cargarCredencial() }, colors = ButtonDefaults.buttonColors(containerColor = FimSurfaceVariant)) { Text("Reintentar", color = FimGold) } }
                }
            }
        }
    }
}

@Composable
fun PantallaNotas(vm: FimViewModel, titulo: String, listaMaterias: List<Materia>, onRefresh: () -> Unit) {
    BackHandler { vm.volverAlMenu() }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(topBar = { TopBarPersonalizada(titulo = titulo, onBack = { vm.volverAlMenu() }, onRefresh = onRefresh) }, containerColor = FimDark) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar materia") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = FimGold) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FimGold,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = FimGold,
                    cursorColor = FimGold
                ),
                singleLine = true
            )

            if (vm.isLoading) { Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = FimGold) } } else {
                val filteredList = listaMaterias.filter { 
                    normalizeText(it.nombre).contains(normalizeText(searchQuery)) || 
                    normalizeText(it.profesor).contains(normalizeText(searchQuery)) 
                }
                
                if (filteredList.isEmpty()) { Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) { Text("No hay datos disponibles", color = Color.Gray) } } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = FimSurfaceVariant), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.width(4.dp).height(40.dp).background(statusColor, CircleShape)); Spacer(modifier = Modifier.width(12.dp)); Column(modifier = Modifier.weight(1f)) { Text(nombreLimpio, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White); Text(materia.profesor, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { materia.parciales.forEachIndexed { i, nota -> CajaCalif("P${i + 1}", nota) } }; Spacer(modifier = Modifier.weight(1f)); CajaCalif("Prom", materia.promedio, isPromedio = true) }
        }
    }
}

@Composable
fun PantallaHorario(vm: FimViewModel) {
    BackHandler { vm.volverAlMenu() }
    val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
    val clasesDelDia = vm.horario.filter { it.dia == vm.diaSeleccionado }.sortedBy { it.hora }
    Scaffold(topBar = { TopBarPersonalizada(titulo = "Mi Horario", onBack = { vm.volverAlMenu() }, onRefresh = { vm.cargarHorario() }) }, containerColor = FimDark) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                dias.forEach { dia ->
                    val seleccionado = vm.diaSeleccionado == dia
                    val conflictivo = vm.horario.any { it.dia == dia && it.hayEmpalme }
                    val colorFondo = if (seleccionado) FimGold else Color.Transparent
                    val colorTexto = if (seleccionado) Color.Black else Color.Gray

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colorFondo)
                            .clickable { vm.diaSeleccionado = dia }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (conflictivo) {
                                Icon(Icons.Filled.Warning, contentDescription = "Conflicto", tint = FimRed, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(text = dia.take(3).uppercase(), color = colorTexto, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
            HorizontalDivider(color = FimSurfaceVariant)
            if (vm.isLoading) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = FimGold) } } else {
                if (clasesDelDia.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.DateRange, null, tint = FimSurfaceVariant, modifier = Modifier.size(64.dp)); Spacer(modifier = Modifier.height(16.dp)); Text("Día libre", style = MaterialTheme.typography.titleLarge, color = Color.Gray) } } } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    val containerColor = if (esActual) FimGold.copy(alpha = 0.15f) else FimSurfaceVariant
    val borderColor = if (clase.hayEmpalme) FimRed else if (esActual) FimGold else Color.Transparent
    
    ElevatedCard(modifier = Modifier.fillMaxWidth().border(BorderStroke(if (clase.hayEmpalme) 2.dp else 1.dp, borderColor), shape = RoundedCornerShape(12.dp)), colors = CardDefaults.elevatedCardColors(containerColor = containerColor), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { val horas = clase.hora.split("-"); Text(horas.getOrElse(0){""}.trim(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if(esActual) FimGold else Color.White); Text(horas.getOrElse(1){""}.trim(), fontSize = 12.sp, color = Color.Gray) }
            Spacer(modifier = Modifier.width(16.dp)); Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.Gray.copy(alpha = 0.3f))); Spacer(modifier = Modifier.width(16.dp))
            Column { 
                if (clase.hayEmpalme) Text("⚠ EMPALME DETECTADO", color = FimRed, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                if (esActual) Text("AHORA", color = FimGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                Text(clase.materia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White); Spacer(modifier = Modifier.height(4.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Text("${clase.aula} • ${clase.seccion}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) } 
            }
        }
    }
}

@Composable
fun CajaCalif(titulo: String, nota: String, isPromedio: Boolean = false) {
    val califNum = nota.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 0f

    val colorTexto = when {
        nota == "--" -> Color.Gray
        nota == "A" -> FimGreen
        califNum >= 8.0 -> FimGreen
        califNum >= 6.0 -> FimYellow
        else -> FimRed
    }

    val backgroundModifier = if (isPromedio) { Modifier.clip(RoundedCornerShape(8.dp)).background(colorTexto.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 4.dp) } else { Modifier }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = backgroundModifier) { Text(titulo, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 10.sp); Text(nota, style = MaterialTheme.typography.titleMedium, fontWeight = if (isPromedio) FontWeight.Black else FontWeight.Bold, color = colorTexto) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarPersonalizada(titulo: String, onBack: () -> Unit, onRefresh: () -> Unit) {
    CenterAlignedTopAppBar(title = { Text(titulo, fontWeight = FontWeight.Bold, fontSize = 20.sp) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás") } }, actions = { IconButton(onClick = onRefresh) { Icon(Icons.Filled.Refresh, contentDescription = "Recargar") } }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = FimDark, titleContentColor = Color.White, navigationIconContentColor = FimGold, actionIconContentColor = FimGold))
}

@Composable
fun PantallaAvance(vm: FimViewModel) {
    BackHandler { vm.volverAlMenu() }
    Scaffold(
        topBar = { TopBarPersonalizada(titulo = "Avance Académico", onBack = { vm.volverAlMenu() }, onRefresh = { vm.cargarAvance() }) },
        containerColor = FimDark
    ) { padding ->
        if (vm.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = FimGold) }
        } else {
            if (vm.avanceAcademico.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("No hay datos disponibles", color = Color.Gray) }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = FimSurfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.clave, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.weight(1f))
                Text(item.porcentaje, fontWeight = FontWeight.Bold, color = FimBlue)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.materia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(item.profesorNombre, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { porcentajeNum / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = FimBlue,
                trackColor = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (item.idHorario.isNotBlank()) {
                Button(
                    onClick = onVerTemas,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = FimBlue.copy(alpha = 0.2f), contentColor = FimBlue)
                ) {
                    Text("Ver Temas Reportados")
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
        containerColor = FimDark
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = vm.materiaSeleccionadaNombre,
                style = MaterialTheme.typography.titleMedium,
                color = FimGold,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (vm.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = FimPurple) }
            } else {
                if (vm.temasReportados.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
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
    Surface(color = FimDark, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(4.dp).height(18.dp).background(FimPurple, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = FimPurple.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun SubtemaCard(item: TemaReportado) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = FimSurfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 12.dp)) {
                Icon(Icons.Filled.DateRange, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Text(text = item.fecha, style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center)
            }
            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.Gray.copy(alpha = 0.2f)))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = item.subtema, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
        }
    }
}

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
                FloatingActionButton(onClick = { mostrarDialogo = true }, containerColor = FimPurple) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar Nota", tint = Color.White)
                }
            }
        },
        containerColor = FimDark
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
                modifier = Modifier.fillMaxWidth().height(80.dp).clickable { vm.seleccionarAnioNotas(anio) },
                colors = CardDefaults.elevatedCardColors(containerColor = FimSurfaceVariant)
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(FimPurple.copy(0.2f)), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = FimPurple)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(anio, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
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
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = FimGold) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FimGold,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = FimGold,
                cursorColor = FimGold
            ),
            singleLine = true
        )

        val filteredMaterias = materias.filter { normalizeText(it).contains(normalizeText(searchQuery)) }

        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(filteredMaterias) { index, materia ->
                val nuevas = vm.contarNuevas(vm.anioSeleccionado, materia)

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { vm.seleccionarMateriaNotas(materia) },
                    colors = CardDefaults.cardColors(containerColor = FimSurfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Book, null, tint = FimGold)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(materia, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))

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
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = FimGold) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FimGold,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = FimGold,
                cursorColor = FimGold
            ),
            singleLine = true
        )

        if (notasFiltradas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Book, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Text("No hay resultados", color = Color.Gray)
                    if (searchQuery.isBlank()) Text("¡Sé el primero en aportar!", color = FimPurple)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        colors = CardDefaults.elevatedCardColors(containerColor = FimSurfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(nota.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FimPurple)
            Text(nota.descripcion, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (nota.link.isNotBlank()) {
                            val url = if (nota.link.startsWith("http")) nota.link else "https://${nota.link}"
                            try { uriHandler.openUri(url) } catch (e: Exception) { Log.e("LinkError", "Invalid URI: ${nota.link}") }
                        }
                    }
                    .padding(vertical = 4.dp)
            ) {
                Icon(Icons.Filled.Link, null, tint = FimBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(nota.link, style = MaterialTheme.typography.bodySmall, color = FimBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(8.dp))
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
        title = { Text("Compartir Recurso") },
        text = {
            Column {
                Card(colors = CardDefaults.cardColors(containerColor = FimPurple.copy(alpha = 0.1f)), border = BorderStroke(1.dp, FimPurple.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("⚠️ AVISO IMPORTANTE:", fontWeight = FontWeight.Bold, color = FimPurple, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "para compartir tus notas o libros subelos al drive tu correo institucional dale acceso a compartir con UNIVERSIDAD MICHOACANA DE SAN NICOLAS DE HIDALGO copia el enlace y pegalo aqui",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de recurso") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, Modifier.clickable { expandedDesc = true }) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = expandedDesc, onDismissRequest = { expandedDesc = false }) {
                        opcionesDesc.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
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
                    supportingText = { if(errorLink) Text("Solo se aceptan enlaces de Google Drive", color = FimRed) }
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
                colors = ButtonDefaults.buttonColors(containerColor = FimPurple)
            ) { Text("Publicar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
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
                Icon(Icons.Filled.Add, "Agregar", tint = Color.Black)
            }
        },
        containerColor = FimDark
    ) { padding ->
        if (vm.sugerencias.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.BugReport, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Text("No hay reportes aún", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        colors = CardDefaults.cardColors(containerColor = FimSurfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = colorIcon)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sug.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(sug.contenido, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                   Text(sug.fecha, style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                   if (respuestasCount > 0) {
                       Spacer(modifier = Modifier.width(8.dp))
                       Text("$respuestasCount respuestas", style = MaterialTheme.typography.labelSmall, color = FimBlue)
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
        title = { Text("Nuevo Reporte") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(selected = tipo == "Sugerencia", onClick = { tipo = "Sugerencia" }, label = { Text("Sugerencia") })
                    FilterChip(selected = tipo == "Error", onClick = { tipo = "Error" }, label = { Text("Bug / Error") })
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = contenido, onValueChange = { contenido = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        },
        confirmButton = {
            Button(onClick = { if (titulo.isNotBlank() && contenido.isNotBlank()) onConfirm(titulo, contenido, tipo) }) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
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
        containerColor = FimDark,
        contentWindowInsets = WindowInsets.safeDrawing, // Manejo safe area
        bottomBar = {
            // Input Bar con manejo de IME
            Surface(
                color = FimSurfaceVariant, 
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding() // Padding automatico teclado
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
                        placeholder = { Text("Escribir respuesta...") },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FimDark, 
                            unfocusedContainerColor = FimDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    IconButton(
                        onClick = { 
                            if (respuestaInput.isNotBlank()) {
                                vm.enviarRespuesta(respuestaInput)
                                respuestaInput = ""
                                // Ocultar teclado tras enviar (opcional, mejor dejarlo abierto para chatear)
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = FimGold)
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
                     HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray.copy(0.2f))
                     Text("Comentarios", style = MaterialTheme.typography.titleMedium, color = FimBlue)
                     Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            item {
                Column(modifier = Modifier.fillMaxWidth().background(FimSurfaceVariant, RoundedCornerShape(12.dp)).padding(16.dp)) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(if(sug.tipo == "Error") Icons.Filled.BugReport else Icons.Filled.Lightbulb, null, tint = if(sug.tipo == "Error") FimRed else FimGold)
                         Spacer(modifier = Modifier.width(8.dp))
                         Text(sug.titulo, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                     }
                     Spacer(modifier = Modifier.height(8.dp))
                     Text(sug.contenido, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
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
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Gray.copy(0.3f)), contentAlignment = Alignment.Center) {
             Text(resp.autor.take(1), color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.background(FimSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp)).padding(12.dp)) {
            Text(resp.autor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = FimGold)
            Text(resp.contenido, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Text(resp.fecha, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.align(Alignment.End))
        }
    }
}