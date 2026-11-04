package com.oaklog.app.data.remote

import com.oaklog.app.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Thin Retrofit mapping of the PokéAPI v2 endpoints OakLog uses. */
interface PokeApiService {
    @GET("pokemon-species")
    suspend fun speciesList(@Query("limit") limit: Int = 3000, @Query("offset") offset: Int = 0): ResourceListDto

    @GET("pokemon-species/{id}")
    suspend fun species(@Path("id") idOrName: String): SpeciesDto

    @GET("pokemon/{id}")
    suspend fun pokemon(@Path("id") idOrName: String): PokemonDto

    @GET("pokemon/{id}/encounters")
    suspend fun encounters(@Path("id") idOrName: String): List<LocationAreaEncounterDto>

    @GET("evolution-chain")
    suspend fun evolutionChainList(@Query("limit") limit: Int = 1000, @Query("offset") offset: Int = 0): ResourceListDto

    @GET("evolution-chain/{id}")
    suspend fun evolutionChain(@Path("id") id: Int): EvolutionChainDto

    @GET("type")
    suspend fun typeList(@Query("limit") limit: Int = 100): ResourceListDto

    @GET("type/{id}")
    suspend fun type(@Path("id") idOrName: String): TypeDto

    @GET("move")
    suspend fun moveList(@Query("limit") limit: Int = 3000, @Query("offset") offset: Int = 0): ResourceListDto

    @GET("move/{id}")
    suspend fun move(@Path("id") idOrName: String): MoveDto

    @GET("ability")
    suspend fun abilityList(@Query("limit") limit: Int = 1000, @Query("offset") offset: Int = 0): ResourceListDto

    @GET("ability/{id}")
    suspend fun ability(@Path("id") idOrName: String): AbilityDto

    @GET("item")
    suspend fun itemList(@Query("limit") limit: Int = 3000, @Query("offset") offset: Int = 0): ResourceListDto

    @GET("item/{id}")
    suspend fun item(@Path("id") idOrName: String): ItemDto

    @GET("generation")
    suspend fun generationList(@Query("limit") limit: Int = 100): ResourceListDto

    @GET("generation/{id}")
    suspend fun generation(@Path("id") idOrName: String): GenerationDto

    @GET("region")
    suspend fun regionList(@Query("limit") limit: Int = 100): ResourceListDto

    @GET("location/{id}")
    suspend fun location(@Path("id") idOrName: String): LocationDto

    @GET("location-area/{id}")
    suspend fun locationArea(@Path("id") idOrName: String): LocationAreaDto

    @GET("region/{id}")
    suspend fun region(@Path("id") idOrName: String): RegionDto

    @GET("pokedex")
    suspend fun pokedexList(@Query("limit") limit: Int = 100): ResourceListDto

    @GET("pokedex/{id}")
    suspend fun pokedex(@Path("id") idOrName: String): PokedexDto

    @GET("version")
    suspend fun versionList(@Query("limit") limit: Int = 100): ResourceListDto

    @GET("version/{id}")
    suspend fun version(@Path("id") idOrName: String): VersionDto

    @GET("version-group")
    suspend fun versionGroupList(@Query("limit") limit: Int = 100): ResourceListDto

    @GET("version-group/{id}")
    suspend fun versionGroup(@Path("id") idOrName: String): VersionGroupDto

    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}
