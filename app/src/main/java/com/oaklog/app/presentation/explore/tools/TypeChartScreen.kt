package com.oaklog.app.presentation.explore.tools

import com.oaklog.app.presentation.components.Card
import com.oaklog.app.presentation.components.DropdownOption
import com.oaklog.app.presentation.components.OakDropdown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oaklog.app.domain.logic.TypeChart
import com.oaklog.app.presentation.components.LoadingView
import com.oaklog.app.presentation.components.OakTopBar
import com.oaklog.app.presentation.components.TypeBadge
import com.oaklog.app.presentation.theme.OakCaughtGreen

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TypeChartScreen(
    typeChart: TypeChart?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (typeChart == null) {
        LoadingView(message = "Calculating type relationships...")
        return
    }

    var selectedMode by remember { mutableIntStateOf(0) } // 0: Defending, 1: Attacking
    val allTypes = remember(typeChart) { typeChart.types.sorted() }
    val typeOptions = remember(allTypes) {
        allTypes.map { DropdownOption(it, it.replaceFirstChar { c -> c.uppercase() }) }
    }
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // Brighter hues on dark surfaces keep the group headings readable.
    val extremeWeak = if (dark) Color(0xFFFF6B6B) else Color(0xFFB71C1C)
    val weak = if (dark) Color(0xFFFF8A80) else Color(0xFFE53935)
    val resist = if (dark) Color(0xFF81C784) else Color(0xFF2E7D32)
    val immuneColor = if (dark) Color(0xFFB39DDB) else Color(0xFF512DA8)
    val notVeryColor = if (dark) Color(0xFFFFB74D) else Color(0xFFE65100)

    // Defending Mode States
    var defenderType1 by remember { mutableStateOf(allTypes.firstOrNull() ?: "normal") }
    var defenderType2 by remember { mutableStateOf<String?>(null) }

    // Attacking Mode State
    var attackingType by remember { mutableStateOf(allTypes.firstOrNull() ?: "normal") }

    Scaffold(
        topBar = {
            OakTopBar(
                title = "Type Effectiveness Chart",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Mode Selector Tabs (Defending vs Attacking)
            PrimaryTabRow(
                selectedTabIndex = selectedMode,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Tab(
                    selected = selectedMode == 0,
                    onClick = { selectedMode = 0 },
                    text = { Text("Defending (Dual-Type)", fontWeight = FontWeight.Bold) },
                )
                Tab(
                    selected = selectedMode == 1,
                    onClick = { selectedMode = 1 },
                    text = { Text("Attacking", fontWeight = FontWeight.Bold) },
                )
            }

            if (selectedMode == 0) {
                // ---- DEFENDING MODE -----------------------------------------------------------
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Defending Types",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Spacer(Modifier.height(12.dp))

                        OakDropdown(
                            label = "Primary type",
                            options = typeOptions,
                            selected = defenderType1,
                            onSelect = {
                                defenderType1 = it
                                if (defenderType2 == it) defenderType2 = null
                            },
                        )

                        Spacer(Modifier.height(12.dp))

                        OakDropdown(
                            label = "Secondary type (optional)",
                            options = listOf(DropdownOption("", "None")) + typeOptions.filter { it.value != defenderType1 },
                            selected = defenderType2 ?: "",
                            onSelect = { defenderType2 = it.ifEmpty { null } },
                        )
                    }
                }

                // Defensive Breakdown
                val defenderTypes = listOfNotNull(defenderType1, defenderType2)
                val profile = remember(defenderTypes, typeChart) {
                    typeChart.defensiveProfile(defenderTypes)
                }

                val quadWeak = profile.filterValues { it >= 4.0 }.keys.toList()
                val doubleWeak = profile.filterValues { it in 2.0..3.99 }.keys.toList()
                val normalDamage = profile.filterValues { it == 1.0 }.keys.toList()
                val halfResist = profile.filterValues { it in 0.49..0.51 }.keys.toList()
                val quarterResist = profile.filterValues { it in 0.001..0.26 }.keys.toList()
                val immune = profile.filterValues { it == 0.0 }.keys.toList()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (quadWeak.isNotEmpty()) {
                        EffectivenessGroupCard(title = "Takes 4× Damage (Extreme Weakness)", types = quadWeak, tagColor = extremeWeak)
                    }
                    if (doubleWeak.isNotEmpty()) {
                        EffectivenessGroupCard(title = "Takes 2× Damage (Weakness)", types = doubleWeak, tagColor = weak)
                    }
                    if (halfResist.isNotEmpty()) {
                        EffectivenessGroupCard(title = "Takes ½× Damage (Resistance)", types = halfResist, tagColor = resist)
                    }
                    if (quarterResist.isNotEmpty()) {
                        EffectivenessGroupCard(title = "Takes ¼× Damage (Extreme Resistance)", types = quarterResist, tagColor = resist)
                    }
                    if (immune.isNotEmpty()) {
                        EffectivenessGroupCard(title = "Takes 0× Damage (Complete Immunity)", types = immune, tagColor = immuneColor)
                    }
                    if (normalDamage.isNotEmpty()) {
                        EffectivenessGroupCard(title = "Takes 1× Damage (Neutral)", types = normalDamage, tagColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                // ---- ATTACKING MODE -----------------------------------------------------------
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Attacking Move Type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Spacer(Modifier.height(12.dp))

                        OakDropdown(
                            label = "Move type",
                            options = typeOptions,
                            selected = attackingType,
                            onSelect = { attackingType = it },
                        )
                    }
                }

                val offProfile = remember(attackingType, typeChart) {
                    typeChart.offensiveProfile(attackingType)
                }

                val superEffective = offProfile.filterValues { it > 1.0 }.keys.toList()
                val notVery = offProfile.filterValues { it in 0.001..0.999 }.keys.toList()
                val noEffect = offProfile.filterValues { it == 0.0 }.keys.toList()
                val neutral = offProfile.filterValues { it == 1.0 }.keys.toList()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (superEffective.isNotEmpty()) {
                        EffectivenessGroupCard(title = "Super-Effective (2× Damage)", types = superEffective, tagColor = OakCaughtGreen)
                    }
                    if (notVery.isNotEmpty()) {
                        EffectivenessGroupCard(title = "Not Very Effective (½× Damage)", types = notVery, tagColor = notVeryColor)
                    }
                    if (noEffect.isNotEmpty()) {
                        EffectivenessGroupCard(title = "No Effect (0× Damage)", types = noEffect, tagColor = immuneColor)
                    }
                    if (neutral.isNotEmpty()) {
                        EffectivenessGroupCard(title = "Neutral Damage (1× Damage)", types = neutral, tagColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EffectivenessGroupCard(
    title: String,
    types: List<String>,
    tagColor: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = tagColor,
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                types.forEach { type ->
                    TypeBadge(type = type, small = true)
                }
            }
        }
    }
}
