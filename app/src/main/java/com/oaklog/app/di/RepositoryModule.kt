package com.oaklog.app.di

import com.oaklog.app.data.repository.CollectionRepositoryImpl
import com.oaklog.app.data.repository.PokemonRepositoryImpl
import com.oaklog.app.data.repository.TeamRepositoryImpl
import com.oaklog.app.domain.repository.CollectionRepository
import com.oaklog.app.domain.repository.PokemonRepository
import com.oaklog.app.domain.repository.TeamRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPokemonRepository(impl: PokemonRepositoryImpl): PokemonRepository

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(impl: CollectionRepositoryImpl): CollectionRepository

    @Binds
    @Singleton
    abstract fun bindTeamRepository(impl: TeamRepositoryImpl): TeamRepository
}
