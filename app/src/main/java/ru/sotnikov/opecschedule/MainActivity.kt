package ru.sotnikov.opecschedule

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.koin.androidx.compose.viewModel
import ru.sotnikov.opecschedule.model.Dates
import ru.sotnikov.opecschedule.model.MainShedules
import ru.sotnikov.opecschedule.model.Results
import ru.sotnikov.opecschedule.ui.theme.OPECscheduleTheme

@ExperimentalSerializationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel by viewModel()
            val state = viewModel.container.stateFlow.collectAsState()
            viewModel.updateHtml()
            OPECscheduleTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Greeting(
                        state = state.value,
                        function =
                     { dates ->
                        var json =
                            Json.decodeFromStream<MainShedules>(resources.openRawResource(R.raw.test))
                        json =
                            MainShedules(json.results.filter { it.week == dates.week && it.dow == dates.day })
                        state.value.paraList?.list?.filter { it.week == dates.week && it.day == dates.day }
                            ?.let { viewModel.updateSortedParaList(it) }
                        viewModel.updateMainShedules(json)
                        Log.e("qqqqq",state.value.sortedParaList.size.toString())
                        var groups = emptySet<String>()
                        json.results.forEach {
                            groups = groups.plus(it.group)
                        }
                        Log.e("qqq",groups.size.toString())
                        viewModel.updateGroups(groups.toList())
                    },
                    function_two = {
                        var shed = state.value.mainShedules!!.results.filter { q -> q.group == it }
                        val sortGroup = state.value.sortedParaList.filter { q -> q.group == it }
                        sortGroup.forEach {
                            shed.forEach { it1 ->
                                shed = if(it1.para == it.para && it.pg == it1.pg){
                                    shed.minus(it1)
                                }else{
                                    shed.plus(Results(week = it.week, audience = it.aud.plus(""), discipline = it.predmet, dow = it.day, group = it.group, para = it.para, pg = it.pg, teacher = it.prepod ))
                                }
                            }

                        }
                        //sortGroup.forEach {
                        //    if(!shed.contains(Results(week = it.week, audience = it.snAud.plus(""), discipline = it.snPredmet, dow = it.day, group = it.group, para = it.para, pg = it.pg, teacher = it.snPrepod ))){
                        //        shed.plus(Results(week = it.week, audience = it.aud.plus(""), discipline = it.predmet, dow = it.day, group = it.group, para = it.para, pg = it.pg, teacher = it.prepod ))
                        //    }else{
                        //
                        //    }
                        //}
                        viewModel.updateEdited(sortGroup,shed)
                        Log.e("1",state.value.editedGroup.size.toString())
                        Log.e("2",state.value.editedMain.size.toString())
                    })
                }
            }
        }
    }
}

@Composable
fun Greeting(
    state: DetailState,
    function: (Dates) -> Unit,
    function_two: (String) -> Unit
) {
    Column {
        Column {
            state.dates.forEach { date ->
                Text(date.date, modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        function(date)
                    })
            }
        }
        if (!state.groups.isNullOrEmpty()) {
            LazyColumn(
                Modifier
                    .padding(start = 16.dp)
                    .height(200.dp)) {
                items(state.groups.size) { it ->
                    Text(state.groups.toList()[it], modifier = Modifier
                        .padding(16.dp)
                        .clickable {
                            function_two(state.groups.toList()[it])
                        })
                }
            }
        }
        if(!state.editedMain.isNullOrEmpty()){
            LazyColumn{
                items(state.editedMain.size){
                    Text(state.editedMain[it].discipline)
                }
            }
        }
    }
}

        //LazyColumn {
    //    state.sortedParaList.size.let { it ->
    //        items(it) {
    //            Text(text = "Hello ${state.sortedParaList[it]}!")
            //        }
            //    }
            //}



fun dayConvert(day: String): String {
    return when (day) {
        "понедельник" -> "1"
        "вторник" -> "2"
        "среда" -> "3"
        "четверг" -> "4"
        "пятница" -> "5"
        "суббота" -> "6"
        else -> "0"
    }
}

data class ParaList(
    var list: List<Para> = emptyList()
)

data class Para(
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


