package com.alex.fimportal.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.InputStream
import java.util.regex.Pattern

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
                            var partes = rawHtml.split(Regex("<br\\s*/?>")).map { Jsoup.parse(it).text().trim() }.filter { it.isNotEmpty() }.toMutableList()
                            
                            // Si hay empalme y partes insuficientes o raramente formateadas, intentamos separar lineas pegadas
                             if (partes.size > 0) {
                                val fixedPartes = mutableListOf<String>()
                                for (p in partes) {
                                    if (p.startsWith("Edif") || p.contains("Aula", ignoreCase = true)) {
                                         val match = Regex("(?<=[0-9a-z])(?=[A-ZÁÉÍÓÚÑ])").find(p) 
                                         if (match != null && match.range.first > 10) { // 10 chars minimo de "Edif... "
                                             val splitIdx = match.range.first
                                              val firstPart = p.substring(0, splitIdx)
                                              val secondPart = p.substring(splitIdx)
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
                                    val aula = if (idx + 2 < partes.size) partes[idx + 2] else ""
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
