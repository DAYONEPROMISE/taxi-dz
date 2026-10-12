package dz.taxidz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dz.taxidz.data.FuelExpense
import dz.taxidz.data.Trip
import dz.taxidz.logic.EarningsCalculator
import dz.taxidz.ui.AppLocale
import dz.taxidz.ui.StatsUi
import dz.taxidz.ui.TaxiDzTheme
import dz.taxidz.ui.TaxiDzViewModel
import dz.taxidz.ui.TaxiEmptyState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** French grouping for DA readouts (34 200 DA). */
private fun fmtAmount(n: Int): String = String.format(Locale.FRANCE, "%,d", n)

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
            CompositionLocalProvider(
                LocalLayoutDirection provides if (AppLocale.isRtl(lang.value)) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                TaxiDzTheme(dark = dark) {
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

@Composable
private fun TaxiDzApp(vm: TaxiDzViewModel, onToggleLang: () -> Unit) {
    var tab by remember { mutableStateOf(Tab.TRIPS) }
    val trips by vm.tripList.collectAsState()
    val fuels by vm.fuelList.collectAsState()
    val stats by vm.stats.collectAsState()

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().height(64.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onToggleLang) {
                        Text(stringResource(R.string.switch_lang))
                    }
                }
            }
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
                    icon = { Icon(Icons.Filled.DirectionsCar, contentDescription = tripsLabel) }
                )
                NavigationBarItem(
                    selected = tab == Tab.STATS,
                    onClick = { tab = Tab.STATS },
                    label = { Text(statsLabel) },
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = statsLabel) }
                )
                NavigationBarItem(
                    selected = tab == Tab.FUEL,
                    onClick = { tab = Tab.FUEL },
                    label = { Text(fuelLabel) },
                    icon = { Icon(Icons.Filled.LocalGasStation, contentDescription = fuelLabel) }
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
    val fareTooBig = stringResource(R.string.fare_too_big)
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
                    "fare_too_big" -> fareTooBig
                    else -> invalidFare
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.add_trip))
        }
        Spacer(Modifier.height(4.dp))
        if (trips.isEmpty()) {
            TaxiEmptyState(
                icon = Icons.Filled.DirectionsCar,
                title = stringResource(R.string.empty_trips_title),
                hint = stringResource(R.string.empty_trips_hint)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(trips, key = { it.id }) { t ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text("${t.departure} \u2192 ${t.destination}", style = MaterialTheme.typography.titleSmall)
                                Text("${t.fareDzd} $dzd \u2022 ${fmt.format(Date(t.timestamp))}",
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
}

@Composable
private fun StatsScreen(modifier: Modifier = Modifier, stats: StatsUi) {
    val dzd = stringResource(R.string.dzd)
    val dayFmt = remember { SimpleDateFormat("EEE dd/MM", Locale.getDefault()) }
    if (stats.grandTotal == 0 && stats.weekTotal == 0 && stats.last7.isEmpty()) {
        TaxiEmptyState(
            icon = Icons.Filled.BarChart,
            title = stringResource(R.string.stats_empty_title),
            hint = stringResource(R.string.stats_empty_hint)
        )
        return
    }
    val maxDay = stats.last7.maxOfOrNull { it.totalDzd } ?: 0
    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        stringResource(R.string.net).uppercase() + " — " + stringResource(R.string.last_7_days),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        fmtAmount(stats.weekNet) + " $dzd",
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(
                        stringResource(R.string.gross) + " " + fmtAmount(stats.weekTotal) + " • " +
                            stringResource(R.string.fuel) + " " + fmtAmount(stats.weekFuel),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.week_chart), style = MaterialTheme.typography.titleSmall)
                    Row(
                        Modifier.fillMaxWidth().height(88.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        stats.last7.forEach { d ->
                            val frac = if (maxDay > 0) d.totalDzd.toFloat() / maxDay.toFloat() else 0f
                            Box(
                                Modifier.weight(1f).fillMaxHeight(if (frac > 0f) frac.coerceAtLeast(0.06f) else 0.02f)
                                    .background(
                                        if (d.totalDzd > 0) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                    )
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        stats.last7.forEach { d ->
                            Text(
                                dayFmt.format(Date(d.dayStartMillis)).substring(0, 3),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.today) + " — ${stats.todayTotal} $dzd • ${stats.todayCount} ${stringResource(R.string.trips)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.gross), style = MaterialTheme.typography.bodyMedium)
                        Text("${fmtAmount(stats.weekTotal)} $dzd", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.fuel), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                        Text("− ${fmtAmount(stats.weekFuel)} $dzd", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.grand_total), style = MaterialTheme.typography.bodyMedium)
                        Text("${fmtAmount(stats.grandTotal)} $dzd", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        items(stats.last7, key = { it.dayStartMillis }) { d ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dayFmt.format(Date(d.dayStartMillis)))
                Text("${fmtAmount(d.totalDzd)} $dzd (${d.tripCount})")
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
        if (fuels.isEmpty()) {
            TaxiEmptyState(
                icon = Icons.Filled.LocalGasStation,
                title = stringResource(R.string.empty_fuel_title),
                hint = stringResource(R.string.empty_fuel_hint)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fuels, key = { it.id }) { f ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("${f.amountDzd} ${stringResource(R.string.dzd)}" +
                                (f.liters?.let { " \u2022 $it L" } ?: "") +
                                (f.note.ifBlank { null }?.let { " \u2022 $it" } ?: ""),
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
}
