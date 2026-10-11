package dz.taxidz

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dz.taxidz.data.FuelExpense
import dz.taxidz.data.Trip
import dz.taxidz.logic.EarningsCalculator
import dz.taxidz.ui.AppLocale
import dz.taxidz.ui.StatsUi
import dz.taxidz.ui.TaxiDzViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val vm: TaxiDzViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val lang = remember { mutableStateOf(AppLocale.current(this)) }
            val dark = isSystemInDarkTheme()
            val context = LocalContext.current
            val scheme = remember(dark) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                } else {
                    if (dark) darkColorScheme() else lightColorScheme()
                }
            }
            CompositionLocalProvider(
                LocalLayoutDirection provides if (AppLocale.isRtl(lang.value)) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                MaterialTheme(colorScheme = scheme) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        TaxiDzApp(vm) {
                            val next = if (lang.value == AppLocale.FRENCH) AppLocale.ARABIC else AppLocale.FRENCH
                            AppLocale.apply(next, this)
                            lang.value = next
                            recreate()
                        }
                    }
                }
            }
        }
    }
}

private enum class Tab { TRIPS, STATS, FUEL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaxiDzApp(vm: TaxiDzViewModel, onToggleLang: () -> Unit) {
    var tab by remember { mutableStateOf(Tab.TRIPS) }
    val trips by vm.tripList.collectAsState()
    val fuels by vm.fuelList.collectAsState()
    val stats by vm.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    TextButton(onClick = onToggleLang) {
                        Text(stringResource(R.string.switch_lang))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val tripsLabel = stringResource(R.string.tab_trips)
                val statsLabel = stringResource(R.string.tab_stats)
                val fuelLabel = stringResource(R.string.tab_fuel)
                NavigationBarItem(
                    selected = tab == Tab.TRIPS,
                    onClick = { tab = Tab.TRIPS },
                    label = { Text(tripsLabel) },
                    icon = { Icon(Icons.Filled.DirectionsCar, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = tab == Tab.STATS,
                    onClick = { tab = Tab.STATS },
                    label = { Text(statsLabel) },
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = tab == Tab.FUEL,
                    onClick = { tab = Tab.FUEL },
                    label = { Text(fuelLabel) },
                    icon = { Icon(Icons.Filled.LocalGasStation, contentDescription = null) }
                )
            }
        }
    ) { pad ->
        val content = Modifier.padding(pad).consumeWindowInsets(pad)
        when (tab) {
            Tab.TRIPS -> TripsScreen(content, trips, vm::addTrip, vm::deleteTrip)
            Tab.STATS -> StatsScreen(content, stats)
            Tab.FUEL -> FuelScreen(content, fuels, vm::addFuel, vm::deleteFuel)
        }
    }
}

@Composable
private fun TripsScreen(
    modifier: Modifier = Modifier,
    trips: List<Trip>,
    onAdd: (String, String, Int) -> Unit,
    onDelete: (Trip) -> Unit
) {
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var fare by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    val deleteLabel = stringResource(R.string.delete_item)
    val invalidRoute = stringResource(R.string.invalid_route)
    val invalidFare = stringResource(R.string.invalid_fare)
    val dzd = stringResource(R.string.dzd)

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(from, { from = it }, label = { Text(stringResource(R.string.departure)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(to, { to = it }, label = { Text(stringResource(R.string.destination)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            fare, { fare = it.filter(Char::isDigit).take(7) },
            label = { Text(stringResource(R.string.fare_dzd)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = {
            val err = EarningsCalculator.validateTrip(from.trim(), to.trim(), fare.toIntOrNull())
            if (err == null) {
                onAdd(from, to, fare.toInt())
                from = ""; to = ""; fare = ""; error = null
            } else {
                error = when (err) {
                    "blank_route" -> invalidRoute
                    "fare_too_big" -> "> 1 000 000"
                    else -> invalidFare
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.add_trip))
        }
        Spacer(Modifier.height(4.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(trips, key = { it.id }) { t ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("${t.departure} → ${t.destination}", style = MaterialTheme.typography.titleSmall)
                            Text("${t.fareDzd} $dzd • ${fmt.format(Date(t.timestamp))}",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onDelete(t) }) {
                            Icon(Icons.Filled.Delete, contentDescription = deleteLabel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsScreen(modifier: Modifier = Modifier, stats: StatsUi) {
    val dzd = stringResource(R.string.dzd)
    val dayFmt = remember { SimpleDateFormat("EEE dd/MM", Locale.getDefault()) }
    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.today), style = MaterialTheme.typography.titleMedium)
                    Text("${stats.todayTotal} $dzd • ${stats.todayCount} ${stringResource(R.string.trips)}",
                        style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.last_7_days), style = MaterialTheme.typography.titleMedium)
                    Text("${stringResource(R.string.gross)}: ${stats.weekTotal} $dzd")
                    Text("${stringResource(R.string.fuel)}: ${stats.weekFuel} $dzd")
                    Text("${stringResource(R.string.net)}: ${stats.weekNet} $dzd",
                        style = MaterialTheme.typography.titleLarge)
                    Text("${stringResource(R.string.grand_total)}: ${stats.grandTotal} $dzd",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        items(stats.last7, key = { it.dayStartMillis }) { d ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dayFmt.format(Date(d.dayStartMillis)))
                Text("${d.totalDzd} $dzd (${d.tripCount})")
            }
        }
    }
}

@Composable
private fun FuelScreen(
    modifier: Modifier = Modifier,
    fuels: List<FuelExpense>,
    onAdd: (Int, Double?, String) -> Unit,
    onDelete: (FuelExpense) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var liters by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    val deleteLabel = stringResource(R.string.delete_item)

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            amount, { amount = it.filter(Char::isDigit).take(7) },
            label = { Text(stringResource(R.string.fuel_amount)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            liters, { liters = it.filter { c -> c.isDigit() || c == '.' }.take(6) },
            label = { Text(stringResource(R.string.fuel_liters)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.fuel_note)) }, modifier = Modifier.fillMaxWidth())
        if (error) Text(stringResource(R.string.invalid_amount), color = MaterialTheme.colorScheme.error)
        Button(onClick = {
            val err = EarningsCalculator.validateFuel(amount.toIntOrNull())
            if (err == null) {
                onAdd(amount.toInt(), liters.toDoubleOrNull(), note)
                amount = ""; liters = ""; note = ""; error = false
            } else error = true
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.add_fuel))
        }
        Spacer(Modifier.height(4.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(fuels, key = { it.id }) { f ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("${f.amountDzd} ${stringResource(R.string.dzd)}" +
                                (f.liters?.let { " • $it L" } ?: "") +
                                (f.note.ifBlank { null }?.let { " • $it" } ?: ""),
                                style = MaterialTheme.typography.titleSmall)
                            Text(fmt.format(Date(f.timestamp)), style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onDelete(f) }) {
                            Icon(Icons.Filled.Delete, contentDescription = deleteLabel)
                        }
                    }
                }
            }
        }
    }
}
