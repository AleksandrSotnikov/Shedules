package ru.sotnikov.opecschedule

import org.jsoup.nodes.Document
import ru.sotnikov.opecschedule.model.Dates
import ru.sotnikov.opecschedule.model.MainSchedules
import ru.sotnikov.opecschedule.model.Results

data class DetailState(
    val html: Document? = null,
    val paraList: ParaList? = null,
    val sortedParaRaspesList: List<ParaRaspes> = emptyList(),
    val mainSchedules: MainSchedules? = null,
    val dates: Set<Dates> = emptySet(),
    val groups: List<String> = emptyList(),
    val editedGroup: List<ParaRaspes> = emptyList(),
    val editedMain: List<Results> = emptyList()
)