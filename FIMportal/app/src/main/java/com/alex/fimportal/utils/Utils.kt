package com.alex.fimportal.utils

/** HELPER NORMALIZACION (BUSQUEDA ACCESIBLE) */
fun normalizeText(input: String): String {
    val nfd = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
    val pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(nfd).replaceAll("").lowercase().replace(Regex("[^a-z0-9 ]"), "") // Mantiene solo alfanum y espacios
}
