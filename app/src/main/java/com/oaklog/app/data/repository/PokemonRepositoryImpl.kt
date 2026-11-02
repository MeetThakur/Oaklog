package com.oaklog.app.data.repository

import com.oaklog.app.data.local.ResourceCache
import com.oaklog.app.data.local.dao.CachedResourceDao
import com.oaklog.app.data.local.dao.PokemonIndexDao
import com.oaklog.app.data.local.entity.PokemonIndexEntity
import com.oaklog.app.data.prefs.SettingsDataStore
import com.oaklog.app.data.remote.PokeApiService
import com.oaklog.app.data.remote.dto.*
import com.oaklog.app.domain.logic.NameFormatter
import com.oaklog.app.domain.logic.TypeChart
import com.oaklog.app.domain.model.*
import com.oaklog.app.domain.repository.PokemonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApiService,
    private val cache: ResourceCache,
    private val indexDao: PokemonIndexDao,
    private val cachedResourceDao: CachedResourceDao,
    private val settingsStore: SettingsDataStore,
) : PokemonRepository {

    override fun observeIndex(): Flow<List<PokemonSummary>> =
        indexDao.observeAll().map { entities ->
            entities.map { e ->
                PokemonSummary(
                    id = e.id,
                    name = e.displayName ?: e.name,
                    types = listOfNotNull(e.type1, e.type2),
                    generation = e.generation,
                    height = e.height,
                    weight = e.weight,
                    baseStatTotal = e.baseStatTotal,
                    isLegendary = e.isLegendary,
                    isMythical = e.isMythical,
                    isBaby = e.isBaby,
                )
            }
        }

    override suspend fun syncIndex(force: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val count = indexDao.count()
            if (count > 0 && !force) return@runCatching

            val speciesList = cache.get("species_list_all", ResourceListDto.serializer()) {
                api.speciesList(limit = 1025, offset = 0)
            }

            // Fetch types to assign type1 / type2 efficiently
            val typeList = cache.get("type_list", ResourceListDto.serializer()) {
                api.typeList(limit = 18)
            }
            val pokemonTypesMap = mutableMapOf<String, MutableList<String>>()
            typeList.results.forEach { typeRef ->
                val typeName = typeRef.name
                runCatching {
                    val typeDetail = cache.get("type_$typeName", TypeDto.serializer()) {
                        api.type(typeName)
                    }
                    typeDetail.pokemon.forEach { tp ->
                        val list = pokemonTypesMap.getOrPut(tp.pokemon.name) { mutableListOf() }
                        if (!list.contains(typeName)) {
                            list.add(typeName)
                        }
                    }
                }
            }

            val entities = speciesList.results.mapNotNull { ref ->
                val id = ref.id ?: return@mapNotNull null
                val gen = generationForId(id)
                val types = pokemonTypesMap[ref.name] ?: emptyList()
                PokemonIndexEntity(
                    id = id,
                    name = ref.name,
                    displayName = NameFormatter.pretty(ref.name),
                    type1 = types.getOrNull(0),
                    type2 = types.getOrNull(1),
                    generation = gen,
                )
            }

            indexDao.upsertAll(entities)
            settingsStore.setLastIndexSync(System.currentTimeMillis())
        }
    }

    private fun generationForId(id: Int): Int = when (id) {
        in 1..151 -> 1
        in 152..251 -> 2
        in 252..386 -> 3
        in 387..493 -> 4
        in 494..649 -> 5
        in 650..721 -> 6
        in 722..809 -> 7
        in 810..905 -> 8
        else -> 9
    }

    override suspend fun getPokemonDetail(
        speciesIdOrName: String,
        varietyName: String?,
    ): Result<PokemonDetail> = withContext(Dispatchers.IO) {
        runCatching {
            val speciesDto = cache.get("species_$speciesIdOrName", SpeciesDto.serializer()) {
                api.species(speciesIdOrName)
            }

            val targetVarietyName = varietyName
                ?: speciesDto.varieties.firstOrNull { it.isDefault }?.pokemon?.name
                ?: speciesDto.name

            val pokemonDto = cache.get("pokemon_$targetVarietyName", PokemonDto.serializer()) {
                api.pokemon(targetVarietyName)
            }

            val statMap = mapOf(
                "hp" to "HP",
                "attack" to "Atk",
                "defense" to "Def",
                "special-attack" to "Sp. Atk",
                "special-defense" to "Sp. Def",
                "speed" to "Speed"
            )

            val stats = pokemonDto.stats.map {
                Stat(
                    key = it.stat.name,
                    label = statMap[it.stat.name] ?: NameFormatter.pretty(it.stat.name),
                    value = it.baseStat,
                )
            }

            val bst = stats.sumOf { it.value }
            val types = pokemonDto.types.sortedBy { it.slot }.map { it.type.name }

            // Hydrate local index with detailed info
            indexDao.upsert(
                PokemonIndexEntity(
                    id = speciesDto.id,
                    name = speciesDto.name,
                    displayName = speciesDto.names.firstOrNull { it.language?.name == "en" }?.name ?: NameFormatter.pretty(speciesDto.name),
                    type1 = types.getOrNull(0),
                    type2 = types.getOrNull(1),
                    generation = NameFormatter.generationFromSlug(speciesDto.generation?.name) ?: generationForId(speciesDto.id),
                    height = pokemonDto.height,
                    weight = pokemonDto.weight,
                    baseStatTotal = bst,
                    isLegendary = speciesDto.isLegendary,
                    isMythical = speciesDto.isMythical,
                    isBaby = speciesDto.isBaby,
                )
            )

            val artworkUrl = pokemonDto.sprites.other?.officialArtwork?.frontDefault
                ?: pokemonDto.sprites.other?.home?.frontDefault
                ?: pokemonDto.sprites.frontDefault

            val shinyArtworkUrl = pokemonDto.sprites.other?.officialArtwork?.frontShiny
                ?: pokemonDto.sprites.other?.home?.frontShiny
                ?: pokemonDto.sprites.frontShiny

            val dexEntries = speciesDto.flavorTextEntries
                .filter { it.language?.name == "en" && it.version != null }
                .map { PokedexEntryText(it.version!!.name, NameFormatter.cleanFlavor(it.flavorText ?: "")) }
                .distinctBy { it.versionName }

            val femaleRatio = if (speciesDto.genderRate < 0) null else speciesDto.genderRate

            PokemonDetail(
                id = pokemonDto.id,
                speciesId = speciesDto.id,
                name = speciesDto.name,
                displayName = speciesDto.names.firstOrNull { it.language?.name == "en" }?.name ?: NameFormatter.pretty(speciesDto.name),
                types = types,
                genus = speciesDto.genera.firstOrNull { it.language?.name == "en" }?.genus,
                heightDm = pokemonDto.height,
                weightHg = pokemonDto.weight,
                abilities = pokemonDto.abilities.sortedBy { it.slot }.map {
                    AbilityRef(it.ability.name, it.isHidden)
                },
                stats = stats,
                baseExperience = pokemonDto.baseExperience,
                captureRate = speciesDto.captureRate,
                femaleEighths = femaleRatio,
                eggGroups = speciesDto.eggGroups.map { it.name },
                generation = NameFormatter.generationFromSlug(speciesDto.generation?.name) ?: generationForId(speciesDto.id),
                isLegendary = speciesDto.isLegendary,
                isMythical = speciesDto.isMythical,
                isBaby = speciesDto.isBaby,
                artworkUrl = artworkUrl,
                shinyArtworkUrl = shinyArtworkUrl,
                spriteUrl = pokemonDto.sprites.frontDefault,
                varieties = speciesDto.varieties.map {
                    PokemonVariety(it.pokemon.id ?: 0, it.pokemon.name, it.isDefault)
                },
                dexEntries = dexEntries,
                evolutionChainId = speciesDto.evolutionChain?.url?.trimEnd('/')?.substringAfterLast('/')?.toIntOrNull(),
            )
        }
    }

    override suspend fun getEvolutionChain(chainId: Int): Result<EvolutionNode> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = cache.get("evolution_chain_$chainId", EvolutionChainDto.serializer()) {
                api.evolutionChain(chainId)
            }
            mapChainLink(dto.chain)
        }
    }

    private fun mapChainLink(link: ChainLinkDto): EvolutionNode {
        val reqs = link.evolutionDetails.map { detail ->
            formatEvolutionRequirement(detail)
        }
        return EvolutionNode(
            speciesId = link.species.id ?: 0,
            name = link.species.name,
            isBaby = link.isBaby,
            requirements = reqs,
            children = link.evolvesTo.map { mapChainLink(it) },
        )
    }

    private fun formatEvolutionRequirement(d: EvolutionDetailDto): String {
        val parts = mutableListOf<String>()
        if (d.minLevel != null) parts.add("Level ${d.minLevel}")
        if (d.item != null) parts.add("Use ${NameFormatter.pretty(d.item.name)}")
        if (d.minHappiness != null) parts.add("Happiness ${d.minHappiness}+")
        if (d.heldItem != null) parts.add("Hold ${NameFormatter.pretty(d.heldItem.name)}")
        if (d.knownMove != null) parts.add("Knows ${NameFormatter.pretty(d.knownMove.name)}")
        if (d.timeOfDay != null && d.timeOfDay.isNotBlank()) parts.add(d.timeOfDay.replaceFirstChar { it.uppercase() })
        if (d.location != null) parts.add("at ${NameFormatter.pretty(d.location.name)}")
        if (d.needsOverworldRain) parts.add("During Rain")
        if (d.turnUpsideDown) parts.add("Turn Device Upside Down")
        if (d.tradeSpecies != null) parts.add("Trade with ${NameFormatter.pretty(d.tradeSpecies.name)}")
        if (d.trigger?.name == "trade" && d.heldItem == null && d.tradeSpecies == null) parts.add("Trade")
        if (parts.isEmpty()) {
            val trig = d.trigger?.name ?: "Level Up"
            parts.add(NameFormatter.pretty(trig))
        }
        return parts.joinToString(", ")
    }

    override suspend fun getMove(nameOrId: String): Result<MoveInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = cache.get("move_$nameOrId", MoveDto.serializer()) {
                api.move(nameOrId)
            }
            val desc = dto.flavorTextEntries.firstOrNull { it.language?.name == "en" }?.flavorText
                ?: dto.effectEntries.firstOrNull { it.language?.name == "en" }?.shortEffect
            MoveInfo(
                id = dto.id,
                name = dto.name,
                type = dto.type?.name,
                damageClass = dto.damageClass?.name,
                power = dto.power,
                accuracy = dto.accuracy,
                pp = dto.pp,
                priority = dto.priority,
                description = desc?.let { NameFormatter.cleanFlavor(it) },
                generation = NameFormatter.generationFromSlug(dto.generation?.name),
            )
        }
    }

    override suspend fun getAbility(nameOrId: String): Result<AbilityInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = cache.get("ability_$nameOrId", AbilityDto.serializer()) {
                api.ability(nameOrId)
            }
            val desc = dto.effectEntries.firstOrNull { it.language?.name == "en" }?.effect
                ?: dto.flavorTextEntries.firstOrNull { it.language?.name == "en" }?.flavorText
            AbilityInfo(
                id = dto.id,
                name = dto.name,
                description = desc?.let { NameFormatter.cleanFlavor(it) },
                generation = NameFormatter.generationFromSlug(dto.generation?.name),
                pokemon = dto.pokemon.map {
                    AbilityPokemon(it.pokemon.name, it.pokemon.id, it.isHidden)
                },
            )
        }
    }

    override suspend fun getItem(nameOrId: String): Result<ItemInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = cache.get("item_$nameOrId", ItemDto.serializer()) {
                api.item(nameOrId)
            }
            val desc = dto.flavorTextEntries.firstOrNull { it.language?.name == "en" }?.text
                ?: dto.effectEntries.firstOrNull { it.language?.name == "en" }?.shortEffect
            ItemInfo(
                id = dto.id,
                name = dto.name,
                category = dto.category?.name,
                cost = dto.cost,
                description = desc?.let { NameFormatter.cleanFlavor(it) },
                spriteUrl = dto.sprites.default,
                attributes = dto.attributes.map { it.name },
            )
        }
    }

    override suspend fun getEncounters(pokemonIdOrName: String): Result<List<EncounterGroup>> = withContext(Dispatchers.IO) {
        runCatching {
            val list = cache.get("encounters_$pokemonIdOrName", ListSerializer(LocationAreaEncounterDto.serializer())) {
                api.encounters(pokemonIdOrName)
            }
            val versionMap = mutableMapOf<String, MutableList<EncounterSpot>>()
            list.forEach { areaEnc ->
                val areaName = NameFormatter.pretty(areaEnc.locationArea.name)
                areaEnc.versionDetails.forEach { vd ->
                    val versionName = vd.version.name
                    val spots = versionMap.getOrPut(versionName) { mutableListOf() }
                    vd.encounterDetails.forEach { ed ->
                        spots.add(
                            EncounterSpot(
                                location = areaName,
                                method = NameFormatter.pretty(ed.method.name),
                                minLevel = ed.minLevel,
                                maxLevel = ed.maxLevel,
                                chance = ed.chance,
                                conditions = ed.conditionValues.map { NameFormatter.pretty(it.name) },
                            )
                        )
                    }
                }
            }
            versionMap.map { (v, spots) -> EncounterGroup(v, spots) }
        }
    }

    override suspend fun getPokedex(slug: String): Result<PokedexInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = cache.get("pokedex_$slug", PokedexDto.serializer()) {
                api.pokedex(slug)
            }
            val desc = dto.descriptions.firstOrNull { it.language?.name == "en" }?.description
            val entryMap = dto.pokemonEntries.associate {
                (it.pokemonSpecies.id ?: 0) to it.entryNumber
            }
            PokedexInfo(
                name = dto.name,
                region = dto.region?.name,
                description = desc,
                entries = entryMap,
            )
        }
    }

    override suspend fun getPokedexList(): Result<List<NamedEntry>> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = cache.get("pokedex_list", ResourceListDto.serializer()) {
                api.pokedexList(limit = 100)
            }
            dto.results.mapNotNull { ref ->
                val id = ref.id ?: return@mapNotNull null
                NamedEntry(id, ref.name)
            }
        }
    }

    override suspend fun getRegions(): Result<List<RegionInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val listDto = cache.get("region_list", ResourceListDto.serializer()) {
                api.regionList(limit = 20)
            }
            listDto.results.mapNotNull { ref ->
                val r = cache.get("region_${ref.name}", RegionDto.serializer()) {
                    api.region(ref.name)
                }
                RegionInfo(
                    id = r.id,
                    name = r.name,
                    pokedexes = r.pokedexes.map { it.name },
                    generation = NameFormatter.generationFromSlug(r.mainGeneration?.name),
                )
            }
        }
    }

    override suspend fun getGenerations(): Result<List<GenerationInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val listDto = cache.get("generation_list", ResourceListDto.serializer()) {
                api.generationList(limit = 20)
            }
            listDto.results.mapNotNull { ref ->
                val g = cache.get("generation_${ref.name}", GenerationDto.serializer()) {
                    api.generation(ref.name)
                }
                GenerationInfo(
                    id = g.id,
                    name = g.name,
                    region = g.mainRegion?.name,
                    versionGroups = g.versionGroups.map { it.name },
                )
            }
        }
    }

    override suspend fun getGameVersions(): Result<List<GameVersion>> = withContext(Dispatchers.IO) {
        runCatching {
            val listDto = cache.get("version_list", ResourceListDto.serializer()) {
                api.versionList(limit = 100)
            }
            listDto.results.mapNotNull { ref ->
                val v = cache.get("version_${ref.name}", VersionDto.serializer()) {
                    api.version(ref.name)
                }
                GameVersion(
                    name = v.name,
                    versionGroup = v.versionGroup.name,
                    generation = null,
                )
            }
        }
    }

    override suspend fun getTypeChart(): Result<TypeChart> = withContext(Dispatchers.IO) {
        runCatching {
            val typeList = cache.get("type_list", ResourceListDto.serializer()) {
                api.typeList(limit = 18)
            }
            val relations = mutableMapOf<String, TypeChart.Relations>()
            typeList.results.forEach { ref ->
                val typeDto = cache.get("type_${ref.name}", TypeDto.serializer()) {
                    api.type(ref.name)
                }
                val dr = typeDto.damageRelations
                relations[typeDto.name] = TypeChart.Relations(
                    doubleTo = dr.doubleDamageTo.map { it.name },
                    halfTo = dr.halfDamageTo.map { it.name },
                    noneTo = dr.noDamageTo.map { it.name },
                )
            }
            TypeChart.fromRelations(relations)
        }
    }

    override fun observeCacheSize(): Flow<Pair<Int, Long>> =
        combine(cachedResourceDao.count(), cachedResourceDao.totalBytes()) { count, bytes ->
            count to bytes
        }

    override suspend fun clearCache() {
        cachedResourceDao.clear()
    }

    override suspend fun preloadGeneration(
        gen: Int,
        onProgress: (done: Int, total: Int) -> Unit,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val idRange = when (gen) {
                1 -> 1..151
                2 -> 152..251
                3 -> 252..386
                4 -> 387..493
                5 -> 494..649
                6 -> 650..721
                7 -> 722..809
                8 -> 810..905
                else -> 906..1025
            }
            val total = idRange.count()
            var done = 0
            onProgress(done, total)
            for (id in idRange) {
                runCatching { getPokemonDetail(id.toString()) }
                done++
                onProgress(done, total)
            }
        }
    }

    override suspend fun getMoveList(limit: Int, offset: Int): Result<List<NamedEntry>> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = cache.get("move_list_${limit}_$offset", ResourceListDto.serializer()) {
                api.moveList(limit = limit, offset = offset)
            }
            dto.results.mapNotNull { ref ->
                val id = ref.id ?: return@mapNotNull null
                NamedEntry(id, ref.name)
            }
        }
    }

    override suspend fun getAbilityList(limit: Int, offset: Int): Result<List<NamedEntry>> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = cache.get("ability_list_${limit}_$offset", ResourceListDto.serializer()) {
                api.abilityList(limit = limit, offset = offset)
            }
            dto.results.mapNotNull { ref ->
                val id = ref.id ?: return@mapNotNull null
                NamedEntry(id, ref.name)
            }
        }
    }

    override suspend fun getItemList(limit: Int, offset: Int): Result<List<NamedEntry>> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = cache.get("item_list_${limit}_$offset", ResourceListDto.serializer()) {
                api.itemList(limit = limit, offset = offset)
            }
            dto.results.mapNotNull { ref ->
                val id = ref.id ?: return@mapNotNull null
                NamedEntry(id, ref.name)
            }
        }
    }
}
