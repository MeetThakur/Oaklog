package com.oaklog.app.domain.model

data class TeamMember(
    val id: Long = 0,
    val teamId: Long = 0,
    val position: Int = 0,
    val speciesId: Int,
    val pokemonId: Int,
    val pokemonName: String,
    val nickname: String? = null,
    val ability: String? = null,
    val moves: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val artworkUrl: String? = null,
)

data class Team(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val members: List<TeamMember> = emptyList(),
)

