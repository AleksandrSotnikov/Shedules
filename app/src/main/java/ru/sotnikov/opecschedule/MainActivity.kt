package ru.sotnikov.opecschedule

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.koin.androidx.compose.viewModel
import ru.sotnikov.opecschedule.model.Dates
import ru.sotnikov.opecschedule.model.MainSchedules
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
                val selectedIndex = remember { mutableStateOf(0) }
                Scaffold(
                    bottomBar = { BottomBar(selectedIndex) },
                ) {
                    Greeting(
                        state = state.value,
                        function =
                        { dates ->
                            //Основное расписание из JSON
                            var json =
                                Json.decodeFromStream<MainSchedules>(resources.openRawResource(R.raw.test))
                            json =
                                MainSchedules(json.results.filter { it.week == dates.week && it.dow == dates.day })
                            state.value.paraList?.list?.filter {
                                it.week == dates.week && it.day == dates.day
                            }?.let { viewModel.updateSortedParaList(it) }
                            viewModel.updateMainSchedules(json)

                            //Обновлние списка групп
                            var groups = emptySet<String>()
                            json.results.forEach {
                                groups = groups.plus(it.group)
                            }
                            viewModel.updateGroups(groups.toList())
                        },
                        function_two = {
                            //основное расписание
                            val shed =
                                state.value.mainSchedules!!.results.filter { q -> q.group == it }
                            //пары изменения
                            val sortGroup =
                                state.value.sortedParaRaspesList.filter { q -> q.group == it }

                            var test = emptySet<Results>()

                            var test2 = emptySet<Results>()

                            shed.forEach { main ->
                                test = test.plus(
                                    Results(
                                        week = main.week,
                                        audience = main.audience.plus(""),
                                        discipline = main.discipline,
                                        dow = main.dow,
                                        group = main.group,
                                        para = main.para,
                                        pg = main.pg,
                                        teacher = main.teacher
                                    )
                                )
                            }
                            sortGroup.forEach { edited ->
                                if (edited.predmet.isNotEmpty()) {
                                    test = test.plus(
                                        Results(
                                            week = edited.week,
                                            audience = edited.aud.plus(""),
                                            discipline = edited.predmet,
                                            dow = edited.day,
                                            group = edited.group,
                                            para = edited.para,
                                            pg = edited.pg,
                                            teacher = edited.prepod
                                        )
                                    )
                                }

                                test2 = test2.plus(
                                    Results(
                                        week = edited.week,
                                        audience = edited.snAud.plus(""),
                                        discipline = edited.snPredmet,
                                        dow = edited.day,
                                        group = edited.group,
                                        para = edited.para,
                                        pg = edited.snPg,
                                        teacher = edited.snPrepod
                                    )
                                )

                            }
                            test2.forEach { del ->
                                test.forEach { save ->
                                    Log.e("Final", del.toString() + "\n" + save.toString())
                                    if (del == save) {
                                        test = test.minus(del)
                                    }
                                }
                            }

                            viewModel.updateEdited(sortGroup, test.toList())
                        },
                        function_three = {
                            viewModel.updateEdited(emptyList(), emptyList())
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
    function_two: (String) -> Unit,
    function_three: () -> Unit
) {
    val selectedDate = remember { mutableStateOf("") }
    val selectedGroup = remember { mutableStateOf("") }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column {
            Row(modifier = Modifier.padding(top = 16.dp)) {
                LazyRow() {
                    items(1) {
                        state.dates.forEach { date ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = if (selectedDate.value == date.date) Color.Black else Color.White,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Text(
                                    date.date, modifier = Modifier
                                        .clickable {
                                            function(date)
                                            selectedDate.value = date.date
                                            function_three()
                                            selectedGroup.value = ""
                                        },
                                    color = if (selectedDate.value == date.date) Color.White else Color.Black
                                )
                            }
                            Spacer(Modifier.padding(16.dp))
                        }
                    }
                }
            }
            //Группы
            if (!state.groups.isNullOrEmpty()) {
                Spacer(Modifier.padding(8.dp))
                LazyColumn(
                    Modifier
                        .padding(start = 16.dp)
                        .height(300.dp)
                ) {
                    items(1) {
                        FlowRow() {
                            repeat(state.groups.size) { index ->
                                Box(
                                    Modifier
                                        .padding(bottom = 8.dp, start = 4.dp)
                                        .background(
                                            color = if (state.groups.toList()[index] == selectedGroup.value) Color.Black else Color.White,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Text(state.groups.toList()[index],
                                        color = if (state.groups.toList()[index] == selectedGroup.value) Color.White else Color.Black,
                                        modifier = Modifier
                                            .padding(vertical = 2.dp, horizontal = 8.dp)
                                            .clickable {
                                                function_two(state.groups.toList()[index])
                                                selectedGroup.value = state.groups.toList()[index]
                                            })
                                }
                            }
                        }
                    }
                }
            }
            //расписание
            if (!state.editedMain.isNullOrEmpty()) {
                val scroll = rememberScrollState()
                val items = state.editedMain.sortedBy { it.para }
                Box(
                    Modifier
                        .padding(bottom = 64.dp)
                        .background(Color.Black)
                        .verticalScroll(scroll)) {
                for (item in items) {
                        Column(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .padding(top = 128.dp * (item.para.toInt() - 1))
                                .height(128.dp)
                                .fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()){
                                Text(item.para, color = Color.White, modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .padding(start = 16.dp), textAlign = TextAlign.Left)
                                Text(paraToTimeConvert(item.para), color = Color.White, modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp), textAlign = TextAlign.Right)
                            }
                            Row(Modifier.fillMaxWidth()) {
                                if (item.pg.toInt() == 0) {
                                    Column(Modifier.fillMaxWidth()) {
                                        Text(
                                            item.discipline,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            item.teacher,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            item.audience,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                if (item.pg.toInt() == 1) {
                                    Column(){
                                        Row(){
                                            Text(
                                                item.discipline,
                                                color = Color.White,
                                                textAlign = TextAlign.Left,
                                                modifier = Modifier
                                                    .fillMaxWidth(0.5f)
                                                    .padding(start = 16.dp)
                                            )
                                            Spacer(modifier = Modifier.fillMaxWidth())
                                        }
                                        Row(){
                                            Text(
                                                item.teacher,
                                                color = Color.White,
                                                textAlign = TextAlign.Left,
                                                modifier = Modifier
                                                    .fillMaxWidth(0.5f)
                                                    .padding(start = 16.dp)
                                            )
                                            Spacer(modifier = Modifier.fillMaxWidth())
                                        }
                                        Row(){
                                            Text(
                                                item.audience,
                                                color = Color.White,
                                                textAlign = TextAlign.Left,
                                                modifier = Modifier
                                                    .fillMaxWidth(0.5f)
                                                    .padding(start = 16.dp)
                                            )
                                            Spacer(modifier = Modifier.fillMaxWidth())
                                        }
                                    }

                                }
                                if (item.pg.toInt() == 2) {
                                    Column(){
                                        Row(){
                                            Spacer(modifier = Modifier.fillMaxWidth(0.5f))
                                            Text(
                                                item.discipline,
                                                color = Color.White,
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 16.dp)
                                            )
                                        }
                                        Row(){
                                            Spacer(modifier = Modifier.fillMaxWidth(0.5f))
                                            Text(
                                                item.teacher,
                                                color = Color.White,
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 16.dp)
                                            )
                                        }
                                        Row(){
                                            Spacer(modifier = Modifier.fillMaxWidth(0.5f))
                                            Text(
                                                item.audience,
                                                color = Color.White,
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun BottomBar(selectedIndex: MutableState<Int>) {
    BottomNavigation(modifier = Modifier.height(60.dp)) {
        BottomNavigationItem(icon = {
            Image(
                painter = painterResource(id = R.drawable.group),
                "",
                contentScale = ContentScale.Fit
            )
        },
            label = { Text(text = "Группа", color = Color.White) },
            selected = (selectedIndex.value == 0),
            onClick = {
                selectedIndex.value = 0
            })
        BottomNavigationItem(icon = {
            Image(
                painter = painterResource(id = R.drawable.teacher),
                "",
                contentScale = ContentScale.Fit
            )
        },
            label = { Text(text = "Преподаватель", color = Color.White) },
            selected = (selectedIndex.value == 1),
            onClick = {
                selectedIndex.value = 1
            })
        BottomNavigationItem(icon = {
            Image(
                painter = painterResource(id = R.drawable.audience),
                "",
                contentScale = ContentScale.Fit
            )
        },
            label = { Text(text = "Аудитория", color = Color.White) },
            selected = (selectedIndex.value == 2),
            onClick = {
                selectedIndex.value = 2
            })
    }
}

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

fun paraToTimeConvert(para: String): String {
    return when (para) {
        "1" -> "8:00 - 9:35"
        "2" -> "9:45 - 11:20"
        "3" -> "11:55 - 13:30"
        "4" -> "13:45 - 15:20"
        "5" -> "15:40 - 17:15"
        "6" -> "17:25 - 19:00"
        else -> "Unknown"
    }
}

@Composable
fun FilterItem(text: Pair<String, String>, onChange: (String) -> Unit, episode: String) {
    Box(
        Modifier
            .padding(horizontal = 8.dp)
            .background(Color.LightGray, RoundedCornerShape(16.dp))
            .clickable(onClick = { onChange(text.second) })
    ) {
        Text(
            text.first,
            color = if (episode == text.second) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

object Filter {
    val days = emptyList<Pair<String, String>>()
}




