package ru.sotnikov.opecschedule

data class ParaRaspes(
    val group: String,
    val para: String,
    val snPg: String,
    val snPredmet: String,
    val snPrepod: String,
    val snAud: String?,
    val pg: String,
    val predmet: String,
    val prepod: String,
    val aud: String?,
    val week: String,
    val day: String,
    val date: String
)