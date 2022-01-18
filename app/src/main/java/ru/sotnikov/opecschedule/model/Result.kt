package ru.sotnikov.opecschedule.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Results(
    val audience: String,
    val discipline: String,
    val dow: String,
    val group: String,
    val para: String,
    val pg: String,
    val teacher: String,
    val week: String
)