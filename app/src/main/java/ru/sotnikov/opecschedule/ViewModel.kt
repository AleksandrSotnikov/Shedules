package ru.sotnikov.opecschedule

import android.content.res.Resources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import ru.sotnikov.opecschedule.model.Dates
import ru.sotnikov.opecschedule.model.MainShedules
import ru.sotnikov.opecschedule.model.Results

class MainViewModel() : ViewModel(), ContainerHost<DetailState, SideEffect> {

    override val container = container<DetailState, SideEffect>(DetailState())

    fun updateHtml() = intent{
        val url = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRXNw52ze21dKFbM7qlO9FVYR75syrbFAl296tRqAAia-EI_bfLZvYVQdACQLza9Udk-Uw_prCpCBok/pubhtml"
        val doc = Jsoup.connect(url)
            .get()
        reduce{
            state.copy(html = doc)
        }
        if (state.html != null) {
            val table: Elements = state.html!!.select("table") //находим первую таблицу в документе
            val rows: Elements = table.select("tr") // разбиваем нашу таблицу на строки по тегу
            val paraList = ParaList()
            var date  = ""

            for (i in 0 until rows.size) {
                val row: Element = rows[i] //по номеру индекса получает строку
                val cols: Elements = row.select("td") // разбиваем полученную строку по тегу  на столбы
                if(cols.size == 1 && cols[0].text().contains("на")){
                    date = cols[0].text().substring(3)
                }
                if (cols.size == 10 && cols[0].toString() != "Группа" && cols[0].toString()
                        .isNotBlank()
                ) {
                    paraList.list = paraList.list.plus(
                        Para(
                            cols[0].text(),
                            cols[1].text(),
                            cols[2].text(),
                            cols[3].text(),
                            cols[4].text(),
                            cols[5].text(),
                            cols[6].text(),
                            cols[7].text(),
                            cols[8].text(),
                            cols[9].text(),
                            date.substring(date.indexOf("Неделя")+7)[0].toString(), dayConvert(date.substring(date.indexOf("(")+1,date.indexOf(")"))), date
                        )
                    )
                }
                if (cols.size == 8 && cols[0].toString() != "Группа" && cols[0].toString()
                        .isNotBlank()
                ) {
                    paraList.list = paraList.list.plus(
                        Para(
                            cols[0].text(),
                            cols[1].text(),
                            cols[2].text(),
                            cols[3].text(),
                            cols[4].text(),
                            "",
                            cols[5].text(),
                            cols[6].text(),
                            cols[7].text(),
                            "",
                            date.substring(date.indexOf("Неделя")+7)[0].toString(), dayConvert(date.substring(date.indexOf("(")+1,date.indexOf(")"))), date
                        )
                    )
                }
            }
            var dates : Set<Dates> = emptySet()
            reduce{
                state.copy(paraList = paraList)
            }
            paraList.list.forEach { item ->
                dates = dates.plus(Dates(day = item.day, week = item.week, date = item.date))
            }
            reduce{
                state.copy(dates = dates)
            }
        }
    }

    fun updateMainShedules(mainShedules: MainShedules?) = intent{
        reduce{
            state.copy(mainShedules = mainShedules)
        }
    }
    fun updateSortedParaList(sortedParaList: List<Para>) = intent{
        reduce{
            state.copy(sortedParaList = sortedParaList)
        }
    }
    fun updateGroups(groups: List<String>) = intent{
        reduce{
            state.copy(groups = groups)
        }
    }
    fun updateEdited(groups: List<Para>,main:List<Results>)= intent{
        reduce{
            state.copy(editedGroup = groups, editedMain = main)
        }
    }
}





data class DetailState(
    val html: Document? = null,
    val paraList :ParaList? = null,
    val sortedParaList :List<Para> = emptyList(),
    val mainShedules : MainShedules? = null,
    val dates : Set<Dates> = emptySet(),
    val groups : List<String> = emptyList(),
    val editedGroup : List<Para> = emptyList(),
    val editedMain : List<Results> = emptyList()
)

sealed class SideEffect {}