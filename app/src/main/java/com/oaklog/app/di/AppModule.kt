package com.oaklog.app.di

import android.content.Context
import androidx.room.Room
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.oaklog.app.data.local.OakLogDatabase
import com.oaklog.app.data.remote.PokeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Provides @Singleton
    fun okHttp(@ApplicationContext context: Context): OkHttpClient =
        OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "http"), 20L * 1024 * 1024))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides @Singleton
    fun pokeApi(client: OkHttpClient, json: Json): PokeApiService =
        Retrofit.Builder()
            .baseUrl(PokeApiService.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PokeApiService::class.java)

    @Provides @Singleton
    fun database(@ApplicationContext context: Context): OakLogDatabase =
        Room.databaseBuilder(context, OakLogDatabase::class.java, OakLogDatabase.NAME).build()

    @Provides fun cachedResourceDao(db: OakLogDatabase) = db.cachedResourceDao()
    @Provides fun pokemonIndexDao(db: OakLogDatabase) = db.pokemonIndexDao()
    @Provides fun collectionDao(db: OakLogDatabase) = db.collectionDao()
    @Provides fun teamDao(db: OakLogDatabase) = db.teamDao()
}
