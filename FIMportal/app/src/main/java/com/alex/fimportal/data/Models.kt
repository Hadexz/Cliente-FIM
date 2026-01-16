package com.alex.fimportal.data

import androidx.annotation.Keep

/** ENUMERACION DE PANTALLAS */
enum class Pantalla {
    LOGIN, MENU, CALIFICACIONES_PARCIALES, CALIFICACIONES_DEPTALES, HORARIO, CREDENCIAL, AVANCE, TEMAS_REPORTADOS, CALCULADORA_TERMO, ASISTENCIA_PROFESORES, NOTAS_COMUNIDAD, SUGERENCIAS, DETALLE_SUGERENCIA
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
