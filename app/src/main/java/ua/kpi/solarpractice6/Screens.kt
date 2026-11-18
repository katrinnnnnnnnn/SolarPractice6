package ua.kpi.solarpractice6

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.random.Random

private val purple = Color(0xFF6650A4)
private val pale = Color(0xFFF0EAF7)

@Composable
fun SolarApp() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var selected by remember { mutableIntStateOf(21) }
    var status by remember { mutableStateOf("") }
    val rows = remember { mutableStateListOf<EnergyRecord>().also { it.addAll(EnergyData.sample()) } }
    Column(Modifier.fillMaxSize().background(Color(0xFFFFFAFF))) {
        Text("SolarPractice · ПР №6", Modifier.fillMaxWidth().background(pale).padding(20.dp), style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (21..27).forEach { index ->
                Button(onClick={ selected=index; status="" }, modifier=Modifier.weight(1f), contentPadding=PaddingValues(0.dp),
                    colors=ButtonDefaults.buttonColors(containerColor=if (selected==index) purple else pale, contentColor=if (selected==index) Color.White else purple)) {
                    Text("$index", style=MaterialTheme.typography.labelMedium)
                }
            }
        }
        Column(Modifier.fillMaxSize().padding(horizontal=16.dp).imePadding()) {
            when(selected) {
                21 -> AnimationScreen()
                22 -> FilesScreen(rows, onStatus={ status=it })
                23 -> PhotosScreen()
                24 -> PdfScreen(rows, onStatus={ status=it })
                25 -> TestsScreen()
                26 -> ResourcesScreen()
                27 -> ReleaseScreen()
            }
            if(status.isNotEmpty()) Text(status, color=purple, modifier=Modifier.padding(vertical=8.dp))
        }
    }
}

@Composable
private fun H(title:String, description:String) {
    Text(title, style=MaterialTheme.typography.titleLarge, fontWeight=FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    Text(description)
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun AnimationScreen() {
    var power by remember { mutableFloatStateOf(680f) }
    var temperature by remember { mutableFloatStateOf(28f) }
    var extended by remember { mutableStateOf(false) }
    val animatedPower by animateFloatAsState(power/1500f, label="power")
    val animatedTemp by animateFloatAsState(temperature/75f, label="temp")
    H("ПЗ 21 · Анімація", "Анімовані енергетичні показники. Дані навчальні.")
    OutlinedCard(onClick={ extended=!extended }, modifier=Modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(16.dp)) {
            Text("Потужність: ${power.toInt()} Вт", fontWeight=FontWeight.Bold)
            LinearProgressIndicator(progress={ animatedPower }, modifier=Modifier.fillMaxWidth().padding(vertical=10.dp), color=purple)
            Text("Температура: ${temperature.toInt()} °C")
            LinearProgressIndicator(progress={ animatedTemp }, modifier=Modifier.fillMaxWidth().padding(vertical=10.dp), color=Color(0xFFCA7052))
            if(extended) Text("Сонячна електростанція: демонстрація переходів стану у Compose")
        }
    }
    Text("Потужність", Modifier.padding(top=18.dp))
    Slider(value=power, onValueChange={power=it}, valueRange=0f..1500f)
    Text("Температура")
    Slider(value=temperature, onValueChange={temperature=it}, valueRange=0f..75f)
    Button(onClick={power=Random.nextInt(300, 1450).toFloat(); temperature=Random.nextInt(15, 60).toFloat()}) { Text("Оновити показники") }
}

@Composable
private fun FilesScreen(rows: MutableList<EnergyRecord>, onStatus:(String)->Unit) {
    val ctx=LocalContext.current
    val scope=rememberCoroutineScope()
    var source by remember { mutableStateOf("CSV") }
    var untilHour by remember { mutableFloatStateOf(23f) }
    var visible by remember { mutableStateOf(rows.toList()) }
    H("ПЗ 22 · CSV / JSON", "Збереження, читання, перевірка та фільтрація навчальних показників.")
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        FilterChip(selected=source=="CSV", onClick={source="CSV"}, label={Text("CSV")})
        FilterChip(selected=source=="JSON", onClick={source="JSON"}, label={Text("JSON")})
    }
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Button(onClick={scope.launch {
            try { withContext(Dispatchers.IO) { EnergyData.write(ctx,"energy.${source.lowercase()}", if(source=="CSV") EnergyData.csv(rows) else EnergyData.json(rows)) }; onStatus("Файл збережено") }
            catch(e:Exception) { onStatus("Помилка запису: ${e.message}") }
        }}) { Text("Зберегти") }
        OutlinedButton(onClick={scope.launch {
            try {
                val loaded=withContext(Dispatchers.IO) {
                    val value=EnergyData.read(ctx,"energy.${source.lowercase()}")
                    if(source=="CSV") EnergyData.parseCsv(value) else EnergyData.parseJson(value)
                }
                rows.clear(); rows.addAll(loaded); visible=loaded; onStatus("Зчитано ${loaded.size} записів")
            } catch(e:Exception) { onStatus("Помилка читання: ${e.message}") }
        }}) { Text("Прочитати") }
    }
    Text("Період: 00:00–${untilHour.toInt()}:00")
    Slider(untilHour, { untilHour=it; visible=rows.filter { r -> r.hour<=it.toInt() } }, valueRange=0f..23f, steps=22)
    Text("Записів: ${visible.size}")
    LazyColumn(modifier=Modifier.fillMaxWidth().height(220.dp)) {
        items(visible, key={it.hour}) { r ->
            Text("${r.hour}:00  •  ${r.power} Вт  •  ${r.temperature} °C", Modifier.fillMaxWidth().padding(8.dp))
        }
    }
}

@Composable
private fun PhotosScreen() {
    val ctx=LocalContext.current
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("Файл не вибрано") }
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pickedUri=uri
        if(uri!=null) {
            try { ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch(_:SecurityException) {}
            message="Вибрано: ${uri.lastPathSegment ?: "документ"}"
        }
    }
    H("ПЗ 23 · Фото та документи", "Вибір файлів за допомогою Storage Access Framework.")
    Button(onClick={launcher.launch(arrayOf("image/*","application/pdf"))}) { Text("Обрати фото або PDF") }
    Text(message, Modifier.padding(vertical=16.dp))
    val uri=pickedUri
    if(uri!=null && ctx.contentResolver.getType(uri)?.startsWith("image/")==true) {
        val bitmap=remember(uri) { runCatching { ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }.getOrNull() }
        if(bitmap!=null) Image(bitmap.asImageBitmap(), contentDescription="Фото об'єкта", modifier=Modifier.fillMaxWidth().height(230.dp))
    }
    Text("Застосунок не завантажує обрані документи на сервер.")
}

@Composable
private fun PdfScreen(rows:List<EnergyRecord>, onStatus:(String)->Unit) {
    val ctx=LocalContext.current
    val scope=rememberCoroutineScope()
    var file by remember { mutableStateOf<File?>(null) }
    H("ПЗ 24 · PDF-звіт", "Формування аналітичного документа локально засобами Android PdfDocument.")
    Button(onClick={scope.launch {
        try { file=withContext(Dispatchers.IO){ReportPdf.create(ctx, rows)}; onStatus("PDF створено: ${file?.name}") }
        catch(e:Exception) { onStatus("Помилка PDF: ${e.message}") }
    }}) { Text("Згенерувати PDF") }
    file?.let { Text("Файл: ${it.absolutePath}", Modifier.padding(top=12.dp)) }
    Text("Звіт містить години, потужність, температуру й підсумкові показники.", Modifier.padding(top=12.dp))
}

@Composable
private fun TestsScreen() {
    H("ПЗ 25 · Тестування", "Unit-тести розміщені в src/test, UI-тести — в src/androidTest.")
    Text("Unit: перевірка CSV/JSON, формату та валідації значень.")
    Spacer(Modifier.height(12.dp))
    Text("UI: перевірка заголовка та навігації між вкладками.")
    Spacer(Modifier.height(12.dp))
    Text("Запуск тестів — через Android Studio або Gradle. Цей екран не підміняє фактичного виконання тестів.")
}

@Composable
private fun ResourcesScreen() {
    val ctx=LocalContext.current
    var metrics by remember { mutableStateOf<DeviceMetrics?>(null) }
    LaunchedEffect(Unit) { while(true) { metrics=DeviceMetricsReader.read(ctx); delay(1500) } }
    H("ПЗ 26 · Ресурси пристрою", "Оновлення раз на 1,5 секунди без блокування інтерфейсу.")
    metrics?.let {
        MetricCard("Заряд батареї", "${it.battery}%")
        MetricCard("Пам'ять застосунку (Java heap)", "${it.appMemoryMb} МБ")
        MetricCard("Доступна RAM пристрою", "${it.availableMemoryMb} МБ")
        MetricCard("Мережа", it.network)
    }
    Text("Показник CPU не виводиться як точний системний відсоток: Android обмежує доступ до таких даних.")
}

@Composable
private fun MetricCard(label:String,value:String) {
    Card(Modifier.fillMaxWidth().padding(bottom=8.dp), colors=CardDefaults.cardColors(containerColor=pale)) {
        Column(Modifier.padding(16.dp)) { Text(label); Text(value, style=MaterialTheme.typography.titleLarge) }
    }
}

@Composable
private fun ReleaseScreen() {
    H("ПЗ 27 · Підготовка до релізу", "Збірка APK/AAB та перевірка готовності застосунку.")
    Text("1. Перевірити назву, версію та Application ID.")
    Text("2. Виконати unit та UI-тести.")
    Text("3. Build → Generate Signed Bundle / APK.")
    Text("4. Підготувати підпис, опис застосунку та матеріали для Google Play.")
    Text("5. Публікація виконується лише через власний Play Console обліковий запис.", Modifier.padding(top=12.dp))
    Text("Стан: демонстраційний застосунок. Публікацію в Google Play не виконано.", color=purple, modifier=Modifier.padding(top=16.dp))
}
