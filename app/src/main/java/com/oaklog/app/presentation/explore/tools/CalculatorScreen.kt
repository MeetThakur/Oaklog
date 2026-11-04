package com.oaklog.app.presentation.explore.tools

import androidx.compose.material.icons.filled.Calculate
import com.oaklog.app.presentation.components.DatabasePage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.DamageCalc
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.logic.Nature
import com.oaklog.app.domain.logic.Natures
import com.oaklog.app.domain.logic.STAT_KEYS
import com.oaklog.app.domain.logic.StatCalc
import com.oaklog.app.domain.model.PokemonDetail
import com.oaklog.app.presentation.components.Card
import com.oaklog.app.presentation.components.DropdownOption
import com.oaklog.app.presentation.components.EmptyView
import com.oaklog.app.presentation.components.FilterChip
import com.oaklog.app.presentation.components.OakDropdown
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.components.TypeBadge
import com.oaklog.app.presentation.teams.PokemonPickerSheet

private val STAT_LABELS = mapOf(
    "hp" to "HP", "attack" to "Atk", "defense" to "Def",
    "special-attack" to "Sp. Atk", "special-defense" to "Sp. Def", "speed" to "Speed",
)

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var picking by remember { mutableStateOf<CalcRole?>(null) }

    DatabasePage(
        title = "Calculator",
        subtitle = "Stats and damage",
        color = "electric",
        icon = Icons.Default.Calculate,
        onNavigateBack = onNavigateBack,
        compact = true,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PrimaryTabRow(
                selectedTabIndex = tab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Stats", fontWeight = FontWeight.Bold) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Damage", fontWeight = FontWeight.Bold) })
            }
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (tab == 0) {
                    StatsTab(state.stats) { picking = CalcRole.STATS }
                } else {
                    DamageTab(
                        state = state,
                        onPickAttacker = { picking = CalcRole.ATTACKER },
                        onPickDefender = { picking = CalcRole.DEFENDER },
                    )
                }
            }
        }
    }

    picking?.let { role ->
        PokemonPickerSheet(
            pokemonList = state.index,
            title = when (role) {
                CalcRole.STATS -> "Choose Pokémon"
                CalcRole.ATTACKER -> "Attacker"
                CalcRole.DEFENDER -> "Defender"
            },
            onSelectPokemon = { viewModel.pick(role, it.id) },
            onDismiss = { picking = null },
        )
    }
}

@Composable
private fun PokemonSlot(label: String, detail: PokemonDetail?, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (detail == null) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Choose $label", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                AsyncImage(detail.artworkUrl, detail.displayName, Modifier.size(56.dp), contentScale = ContentScale.Fit)
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(detail.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    detail.types.forEach { TypeBadge(type = it, small = true) }
                }
            }
        }
    }
}

@Composable
private fun StatsTab(detail: PokemonDetail?, onPick: () -> Unit) {
    var level by remember { mutableIntStateOf(50) }
    var natureName by remember { mutableStateOf(Natures.neutral.name) }
    val ivs = remember { mutableStateMapOf<String, Int>().apply { STAT_KEYS.forEach { put(it, StatCalc.MAX_IV) } } }
    val evs = remember { mutableStateMapOf<String, Int>().apply { STAT_KEYS.forEach { put(it, 0) } } }
    val nature = Natures.byName(natureName)

    PokemonSlot("Pokémon", detail, onPick)
    if (detail == null) {
        Text(
            "Pick a Pokémon to see its stats at any level, nature and training.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Level $level", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Slider(
                value = level.toFloat(),
                onValueChange = { level = it.toInt() },
                valueRange = 1f..100f,
            )
            OakDropdown(
                label = "Nature",
                options = Natures.all.map { DropdownOption(it.name, natureLabel(it)) },
                selected = natureName,
                onSelect = { natureName = it },
            )
        }
    }

    val totalEv = evs.values.sum()
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(onClick = { STAT_KEYS.forEach { ivs[it] = StatCalc.MAX_IV } }) { Text("Max IVs") }
                TextButton(onClick = { STAT_KEYS.forEach { evs[it] = 0 } }) { Text("Clear EVs") }
            }
            STAT_KEYS.forEach { key ->
                val base = detail.baseStat(key)
                val iv = ivs.getValue(key)
                val ev = evs.getValue(key)
                val final = StatCalc.stat(key, base, iv, ev, level, nature)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(STAT_LABELS.getValue(key), fontWeight = FontWeight.SemiBold, modifier = Modifier.width(64.dp))
                        Text("base $base", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        val mark = when (key) {
                            nature.up -> " ▲"
                            nature.down -> " ▼"
                            else -> ""
                        }
                        Text(
                            "$final$mark",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = when (key) {
                                nature.up -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                    SliderRow("IV", iv, 0, StatCalc.MAX_IV, 1) { ivs[key] = it }
                    SliderRow("EV", ev, 0, StatCalc.MAX_EV, 4) {
                        // The game caps the combined total, so spend only what is left.
                        val room = StatCalc.MAX_TOTAL_EV - (totalEv - ev)
                        evs[key] = it.coerceAtMost(room)
                    }
                }
            }
            Text(
                "EVs used: $totalEv / ${StatCalc.MAX_TOTAL_EV}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Int, min: Int, max: Int, step: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(((it / step).toInt() * step).coerceIn(min, max)) },
            valueRange = min.toFloat()..max.toFloat(),
            modifier = Modifier.weight(1f).height(28.dp),
        )
        Text(value.toString(), fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
    }
}

private fun natureLabel(n: Nature): String =
    if (n.up == null || n.down == null) "${n.name} (neutral)"
    else "${n.name} (+${STAT_LABELS.getValue(n.up)}, −${STAT_LABELS.getValue(n.down)})"

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DamageTab(state: CalculatorUiState, onPickAttacker: () -> Unit, onPickDefender: () -> Unit) {
    var level by remember { mutableIntStateOf(50) }
    var physical by remember { mutableStateOf(true) }
    var moveType by remember { mutableStateOf("normal") }
    var powerText by remember { mutableStateOf("80") }
    var attackerMax by remember { mutableStateOf(false) }
    var defenderMax by remember { mutableStateOf(false) }
    var crit by remember { mutableStateOf(false) }

    PokemonSlot("Attacker", state.attacker, onPickAttacker)
    PokemonSlot("Defender", state.defender, onPickDefender)

    val chart = state.typeChart
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Move", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OakDropdown(
                    label = "Type",
                    options = (chart?.types ?: listOf("normal")).sorted()
                        .map { DropdownOption(it, it.replaceFirstChar { c -> c.uppercase() }) },
                    selected = moveType,
                    onSelect = { moveType = it },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = powerText,
                    onValueChange = { powerText = it.filter(Char::isDigit).take(3) },
                    label = { Text("Power") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.7f),
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = physical, onClick = { physical = true }, label = { Text("Physical") })
                FilterChip(selected = !physical, onClick = { physical = false }, label = { Text("Special") })
                FilterChip(selected = crit, onClick = { crit = !crit }, label = { Text("Critical hit") })
            }
            Text("Level $level", style = MaterialTheme.typography.labelLarge)
            Slider(value = level.toFloat(), onValueChange = { level = it.toInt() }, valueRange = 1f..100f)
            Text("Attacker investment", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !attackerMax, onClick = { attackerMax = false }, label = { Text("None") })
                FilterChip(selected = attackerMax, onClick = { attackerMax = true }, label = { Text("252 EVs + boosting nature") })
            }
            Text("Defender investment", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !defenderMax, onClick = { defenderMax = false }, label = { Text("None") })
                FilterChip(selected = defenderMax, onClick = { defenderMax = true }, label = { Text("252 HP / 252 defense") })
            }
        }
    }

    val attacker = state.attacker
    val defender = state.defender
    val power = powerText.toIntOrNull() ?: 0
    if (attacker == null || defender == null || chart == null) {
        Text(
            "Choose an attacker and a defender to estimate damage.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val atkKey = if (physical) "attack" else "special-attack"
    val defKey = if (physical) "defense" else "special-defense"
    fun spread(boost: Boolean, key: String, base: Int): Int {
        val ev = if (boost) StatCalc.MAX_EV else 0
        val nature = if (boost && key != "hp") Nature("boost", key, null) else Natures.neutral
        return StatCalc.stat(key, base, StatCalc.MAX_IV, ev, level, nature)
    }
    val atk = spread(attackerMax, atkKey, attacker.baseStat(atkKey))
    val def = spread(defenderMax, defKey, defender.baseStat(defKey))
    val hp = spread(defenderMax, "hp", defender.baseStat("hp"))
    val result = DamageCalc.calculate(
        level, power, atk, def, hp, moveType, attacker.types, defender.types, chart, crit,
    )

    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Result", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (result.max == 0) {
                Text(
                    if (result.effectiveness == 0.0) "${defender.displayName} is immune to this move." else "Enter a move power above 0.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Text(
                    "${result.min} – ${result.max} HP",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "%.1f%% – %.1f%% of ${defender.displayName}'s $hp HP".format(result.minPercent, result.maxPercent),
                    style = MaterialTheme.typography.bodyMedium,
                )
                result.hitsToKo?.let { (best, worst) ->
                    Text(
                        if (best == worst) "Takes $best hit${if (best == 1) "" else "s"} to faint"
                        else "Takes $best–$worst hits to faint",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val notes = buildList {
                if (result.stab) add("Same-type bonus ×1.5")
                if (result.effectiveness != 1.0) add("Type effectiveness ×${formatMultiplier(result.effectiveness)}")
                if (crit) add("Critical hit ×1.5")
            }
            if (notes.isNotEmpty()) {
                Text(notes.joinToString(" · "), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "Attack $atk vs defense $def at level $level. Ignores items, abilities and weather; rolls are 85–100%.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatMultiplier(m: Double): String = when (m) {
    0.25 -> "¼"
    0.5 -> "½"
    else -> if (m == m.toInt().toDouble()) m.toInt().toString() else m.toString()
}
