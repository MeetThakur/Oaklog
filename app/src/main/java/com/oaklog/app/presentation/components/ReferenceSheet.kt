package com.oaklog.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import coil3.compose.AsyncImage
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.model.AbilityInfo
import com.oaklog.app.domain.model.ItemInfo
import com.oaklog.app.domain.model.MoveInfo
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** What a [ReferenceSheet] is showing. */
sealed interface ReferenceTarget {
    val name: String

    data class Move(override val name: String) : ReferenceTarget
    data class Ability(override val name: String) : ReferenceTarget
    data class Item(override val name: String) : ReferenceTarget
}

@HiltViewModel
class ReferenceViewModel @Inject constructor(
    private val repo: PokemonRepository,
) : ViewModel() {
    suspend fun move(name: String): Result<MoveInfo> = repo.getMove(name)
    suspend fun ability(name: String): Result<AbilityInfo> = repo.getAbility(name)
    suspend fun item(name: String): Result<ItemInfo> = repo.getItem(name)
}

/** Long lists collapse to this many species chips until the user asks for the rest. */
private const val CHIP_LIMIT = 36

private sealed interface Loaded<out T> {
    data object Loading : Loaded<Nothing>
    data object Failed : Loaded<Nothing>
    data class Ready<T>(val value: T) : Loaded<T>
}

/**
 * Full entry for a move, ability or item. Species chips call [onOpenPokemon], so the sheet is the
 * hop between "what does this do" and "who has it" from anywhere in the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceSheet(
    target: ReferenceTarget,
    onDismiss: () -> Unit,
    onOpenPokemon: (Int) -> Unit,
    viewModel: ReferenceViewModel = hiltViewModel(),
) {
    val loaded by produceState<Loaded<Any>>(Loaded.Loading, target) {
        value = Loaded.Loading
        val result: Result<Any> = when (target) {
            is ReferenceTarget.Move -> viewModel.move(target.name)
            is ReferenceTarget.Ability -> viewModel.ability(target.name)
            is ReferenceTarget.Item -> viewModel.item(target.name)
        }
        value = result.fold({ Loaded.Ready(it) }, { Loaded.Failed })
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = NameFormatter.pretty(target.name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            when (val state = loaded) {
                Loaded.Loading -> Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Loaded.Failed -> Text(
                    "Couldn't load this entry. Check your connection and try again.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                is Loaded.Ready -> when (val v = state.value) {
                    is MoveInfo -> MoveBody(v, onOpenPokemon, onDismiss)
                    is AbilityInfo -> AbilityBody(v, onOpenPokemon, onDismiss)
                    is ItemInfo -> ItemBody(v)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoveBody(info: MoveInfo, onOpenPokemon: (Int) -> Unit, onDismiss: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        info.type?.let { TypeBadge(type = it) }
        info.damageClass?.let { Label(NameFormatter.pretty(it)) }
        info.generation?.let { Label("Gen ${NameFormatter.generationRoman(it)}") }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        Fact("Power", info.power?.toString() ?: "—")
        Fact("Accuracy", info.accuracy?.let { "$it%" } ?: "—")
        Fact("PP", info.pp?.toString() ?: "—")
        Fact("Priority", if (info.priority > 0) "+${info.priority}" else info.priority.toString())
    }
    info.description?.let { Body(it) }
    if (info.learnedBy.isNotEmpty()) {
        Section("Learned by ${info.learnedBy.size} Pokémon")
        var showAll by remember { mutableStateOf(false) }
        val shown = if (showAll) info.learnedBy else info.learnedBy.take(CHIP_LIMIT)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            shown.forEach { p ->
                PokemonChip(NameFormatter.pretty(p.name)) {
                    onDismiss()
                    onOpenPokemon(p.id)
                }
            }
        }
        if (info.learnedBy.size > CHIP_LIMIT) {
            TextButton(onClick = { showAll = !showAll }) {
                Text(if (showAll) "Show fewer" else "Show all ${info.learnedBy.size}")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AbilityBody(info: AbilityInfo, onOpenPokemon: (Int) -> Unit, onDismiss: () -> Unit) {
    info.generation?.let { Label("Gen ${NameFormatter.generationRoman(it)}") }
    (info.description ?: info.shortDescription)?.let { Body(it) }
    val bearers = info.pokemon.filter { it.speciesId != null && it.speciesId < 10000 }
    if (bearers.isNotEmpty()) {
        Section("Pokémon with this ability (${bearers.size})")
        var showAll by remember { mutableStateOf(false) }
        val shown = if (showAll) bearers else bearers.take(CHIP_LIMIT)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            shown.forEach { p ->
                PokemonChip(NameFormatter.pretty(p.pokemonName) + if (p.isHidden) " (hidden)" else "") {
                    onDismiss()
                    onOpenPokemon(p.speciesId!!)
                }
            }
        }
        if (bearers.size > CHIP_LIMIT) {
            TextButton(onClick = { showAll = !showAll }) {
                Text(if (showAll) "Show fewer" else "Show all ${bearers.size}")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemBody(info: ItemInfo) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        info.spriteUrl?.let { AsyncImage(model = it, contentDescription = null, modifier = Modifier.height(48.dp)) }
        Column {
            info.category?.let { Label(NameFormatter.pretty(it)) }
            info.cost?.takeIf { it > 0 }?.let { Fact("Cost", "₽$it") }
        }
    }
    info.description?.let { Body(it) }
    if (info.attributes.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            info.attributes.forEach { Label(NameFormatter.pretty(it)) }
        }
    }
}

@Composable
private fun Body(text: String) = Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurface,
    lineHeight = 22.sp,
)

@Composable
private fun Section(text: String) {
    Spacer(Modifier.height(4.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun Label(text: String) = Text(
    text = text,
    style = MaterialTheme.typography.labelLarge,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
private fun Fact(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun PokemonChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}
