package com.alex.fimportal.utils

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
