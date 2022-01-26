package ru.sotnikov.opecschedule.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MainSchedules(
    @SerialName("results")
    val results: List<Results>
)