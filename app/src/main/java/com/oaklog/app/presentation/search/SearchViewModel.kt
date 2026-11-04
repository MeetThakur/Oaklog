package com.oaklog.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oaklog.app.domain.model.NamedEntry
import com.oaklog.app.domain.model.PokemonSummary
import com.oaklog.app.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchResults(
    val pokemon: List<PokemonSummary> = emptyList(),
    val moves: List<NamedEntry> = emptyList(),
    val abilities: List<NamedEntry> = emptyList(),
    val items: List<NamedEntry> = emptyList(),
) {
    val isEmpty: Boolean get() = pokemon.isEmpty() && moves.isEmpty() && abilities.isEmpty() && items.isEmpty()
}

data class SearchUiState(
    val query: String = "",
    val results: SearchResults = SearchResults(),
    /** True while the move/ability/item name lists are still being fetched. */
    val loadingNames: Boolean = true,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: PokemonRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val moves = MutableStateFlow<List<NamedEntry>>(emptyList())
    private val abilities = MutableStateFlow<List<NamedEntry>>(emptyList())
    private val items = MutableStateFlow<List<NamedEntry>>(emptyList())
    private val loadingNames = MutableStateFlow(true)

    val uiState: StateFlow<SearchUiState> = combine(
        query, repo.observeIndex(), moves, abilities, items,
    ) { q, index, m, a, i ->
        SearchUiState(
            query = q,
            results = if (q.isBlank()) SearchResults() else search(q, index, m, a, i),
        )
    }.combine(loadingNames) { state, loading -> state.copy(loadingNames = loading) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    init {
        // Name lists are small (names + ids only) and cached, so one fetch per list is enough.
        viewModelScope.launch(Dispatchers.IO) {
            val m = async { repo.getMoveList(limit = 2000).getOrDefault(emptyList()) }
            val a = async { repo.getAbilityList(limit = 1000).getOrDefault(emptyList()) }
            val i = async { repo.getItemList(limit = 3000).getOrDefault(emptyList()) }
            moves.value = m.await()
            abilities.value = a.await()
            items.value = i.await()
            loadingNames.value = false
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    private fun search(
        q: String,
        index: List<PokemonSummary>,
        m: List<NamedEntry>,
        a: List<NamedEntry>,
        i: List<NamedEntry>,
    ): SearchResults {
        val needle = q.trim().lowercase().replace('-', ' ')
        val number = needle.removePrefix("#").toIntOrNull()

        fun rank(name: String): Int {
            val n = name.lowercase().replace('-', ' ')
            return when {
                n == needle -> 0
                n.startsWith(needle) -> 1
                n.split(' ').any { it.startsWith(needle) } -> 2
                n.contains(needle) -> 3
                else -> -1
            }
        }

        fun List<NamedEntry>.top(limit: Int) =
            mapNotNull { e -> rank(e.name).takeIf { it >= 0 }?.let { it to e } }
                .sortedWith(compareBy({ it.first }, { it.second.name }))
                .take(limit).map { it.second }

        val pokemon = index.mapNotNull { p ->
            when {
                number != null && p.id == number -> 0 to p
                else -> rank(p.name).takeIf { it >= 0 }?.let { (it + 1) to p }
            }
        }.sortedWith(compareBy({ it.first }, { it.second.id })).take(20).map { it.second }

        return SearchResults(pokemon, m.top(8), a.top(8), i.top(8))
    }
}
