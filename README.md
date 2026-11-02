# OakLog: Pokémon Companion & Pokédex App

**OakLog** is a native Android Pokémon companion application inspired by Professor Oak's digital research journal. Built with modern Android development standards, it combines a Pokédex with player tools including offline collection tracking, real-time team building and type analysis, damage relationship calculations, and deep explore archives.

---

## 📱 Features

### 1. Pokédex
- **Complete Species Roster**: Displays Pokémon from Generation I through Generation IX powered by [PokéAPI](https://pokeapi.co/).
- **Dual Layout Modes**: Switch seamlessly between Grid and List views.
- **Fast, Debounced Search**: Search by Pokémon name, National Pokédex number (`#0025`), or partial keywords.
- **Multidimensional Filters**:
  - Generation (Gen 1 – 9)
  - Elemental Types (all 18 types)
  - Collection Status (Caught / Uncaught)
  - Legendary & Mythical status
  - User Favorites
- **Multi-criteria Sorting**: Sort by National Number, Alphabetical name, Height, Weight, and Base Stat Total (BST) in ascending or descending order.
- **Quick Status Actions**: 1-tap toggling for favorites and cycling caught statuses directly from list cards.

### 2. Pokémon Research Details
- **Dynamic Type-Themed Header**: Large official artwork with dynamic background gradients matching the Pokémon's elemental types.
- **Shiny Artwork Preview**: Toggle shiny form preview with a single tap.
- **Forms & Varieties**: Switch between standard, regional (Alolan, Galarian, Hisuian, Paldean), Mega Evolutions, and Gigantamax forms.
- **Game-Specific Entries**: Read authentic Pokédex descriptions with an interactive game version selector.
- **Animated Base Stats**: Smooth animated stat bars (HP, Atk, Def, Sp. Atk, Sp. Def, Speed) with BST total and tier evaluations.
- **Interactive Evolution Tree**: Visual branching and linear evolution trees with actual evolution requirements (levels, stones, friendship, trades, weather conditions). Every Pokémon node is clickable!
- **Location & Encounters**: Detailed wild encounter spots, capture methods (walking, surfing, fishing), min/max levels, and encounter rates grouped by game version.

### 3. Team Builder & Real-Time Analysis
- **Multiple Teams**: Create, rename, edit, and manage custom teams offline.
- **6-Member Roster**: Add Pokémon with custom nicknames, chosen abilities, and up to 4 moves.
- **Real-Time Defensive Analysis**:
  - Complete matrix calculating individual member resistances, immunities, and weaknesses (2× and 4× quad damage).
  - **Shared Weaknesses Alert**: Highlights when multiple team members share an unresisted elemental vulnerability.
- **STAB Offensive Coverage**: Highlights defender types hit super-effectively by the team's natural STAB typing.

### 4. Collection Tracker
- **Tracking Metrics**: Keep track of Caught, Seen, Uncaught, Shiny, and Favorite Pokémon.
- **Overall Completion Rate**: Visual completion progress bar and percentage breakdown across all 1025 species.
- **Generation Progress Breakdown**: Individual completion gauges for Generations I through IX.
- **Offline Persistence**: All collection data is saved locally in SQLite via Room.

### 5. Research Hub (Explore Section)
- **Interactive Type Chart**:
  - **Defending Mode**: Calculates complete damage multipliers (4×, 2×, 1×, ½×, ¼×, 0×) for any single or dual-type combination.
  - **Attacking Mode**: Inspects offensive advantages, resistances, and immunities for any chosen move type.
- **Moves Database**: Searchable database of moves with base power, accuracy, PP, damage categories (Physical, Special, Status), and battle descriptions.
- **Abilities Database**: Searchable abilities library with detailed descriptions and known species bearers.
- **Items Database**: Comprehensive inventory of Poké Balls, held items, evolution stones, and medicine with official sprites.
- **Regions & Generations**: Geographical regions (Kanto to Paldea) and regional Pokédex rosters.

### 6. Settings & Offline Data Management
- **Themes**: Support for Light, Dark, and System modes with Material 3 dynamic color theming.
- **Cache Management**: Inspect cached payload counts and disk usage with a 1-tap cache clear option.
- **Offline Pre-downloading**: Pre-download entire generation datasets (stats, moves, artwork) for offline field research.

---

## 🛠 Technology Stack & Architecture

- **Language**: Kotlin 2.1
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture + MVVM + Repository Pattern
- **Dependency Injection**: Dagger Hilt
- **Local Persistence**: Room Database (SQLite)
- **Preferences**: Jetpack DataStore
- **Networking**: Retrofit 3.0 + OkHttp 4.12
- **Serialization**: Kotlinx Serialization JSON
- **Image Loading**: Coil 3 with disk caching
- **Async & Concurrency**: Kotlin Coroutines + StateFlow / SharedFlow

### Project Structure
```
app/src/main/java/com/oaklog/app/
├── data/
│   ├── local/
│   │   ├── dao/             # Room DAOs (PokemonIndexDao, CollectionDao, TeamDao, CachedResourceDao)
│   │   ├── entity/          # Room Entities
│   │   ├── OakLogDatabase   # Abstract Room database definition
│   │   └── ResourceCache    # Offline-first caching engine with fair-use API coalescing
│   ├── prefs/               # DataStore implementation for settings
│   ├── remote/
│   │   ├── dto/             # PokéAPI v2 wire models
│   │   └── PokeApiService   # Retrofit HTTP endpoints
│   └── repository/          # Concrete repository implementations
├── domain/
│   ├── logic/               # Pure business logic (TypeChart, TeamAnalyzer, PokedexQuery, CollectionStats, NameFormatter)
│   ├── model/               # Domain models (Pokemon, Team, Settings)
│   └── repository/          # Repository contracts
├── di/                      # Hilt Dependency Injection modules
└── presentation/
    ├── components/          # Reusable Compose UI widgets (TypeBadge, StatBar, PokemonCard, OakTopBar, SearchBar)
    ├── theme/               # Design system (Crimson & Journal Cream palette, Typography, Shapes, Type Colors)
    ├── navigation/          # Navigation Compose NavHost, Screens, and Bottom Navigation Bar
    ├── pokedex/             # Pokédex listing, search, and filter bottom sheet
    ├── detail/              # Pokémon detail screen with Overview, Stats, Evolution, Forms, and Location tabs
    ├── teams/               # Team builder, member customization, and real-time matchup analysis
    ├── collection/          # Collection tracker, completion percentages, and generation stats
    ├── explore/             # Research hub with Type Chart, Moves, Abilities, Items, and Regions
    └── settings/            # Theme, offline pre-downloads, and cache management
```

---

## 🚀 Building & Testing

### Prerequisites
- JDK 17 (set in `gradle.properties` / `JAVA_HOME`)
- Android SDK 35 (`compileSdk = 35`, `minSdk = 26`)

### Build Commands
```bash
# Compile Kotlin debug sources
./gradlew compileDebugKotlin

# Run all unit tests
./gradlew test

# Assemble debug APK
./gradlew assembleDebug
```
The resulting debug APK is located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📜 Attribution
Powered by [PokéAPI](https://pokeapi.co/). Pokémon and Pokémon character names are trademarks of Nintendo, Creatures Inc., and Game Freak.
