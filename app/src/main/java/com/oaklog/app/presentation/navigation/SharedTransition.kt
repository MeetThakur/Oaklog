package com.oaklog.app.presentation.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Scopes provided by [OakLogNavHost] so artwork can morph between a list card and the detail header
 * without either composable knowing about navigation. Both are null outside the nav host (previews,
 * tests), in which case [sharedArtwork] is a no-op.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

fun artworkKey(speciesId: Int) = "artwork-$speciesId"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedArtwork(speciesId: Int): Modifier {
    val shared = LocalSharedTransitionScope.current
    val visibility = LocalAnimatedVisibilityScope.current
    if (shared == null || visibility == null) return this
    return with(shared) {
        this@sharedArtwork.sharedElement(
            sharedContentState = rememberSharedContentState(artworkKey(speciesId)),
            animatedVisibilityScope = visibility,
        )
    }
}
