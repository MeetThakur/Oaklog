package com.oaklog.app.presentation.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/** Streams a Pokémon cry from [play]'s URL; starting a new one stops the previous. */
@Composable
fun rememberCryPlayer(): (String) -> Unit {
    val holder = remember { CryHolder() }
    DisposableEffect(holder) { onDispose { holder.release() } }
    return remember(holder) { { url: String -> holder.play(url) } }
}

private class CryHolder {
    private var player: MediaPlayer? = null

    fun play(url: String) {
        release()
        val mp = MediaPlayer()
        player = mp
        try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            mp.setDataSource(url)
            mp.setOnPreparedListener { it.start() }
            mp.setOnCompletionListener { done ->
                done.release()
                if (player === done) player = null
            }
            mp.setOnErrorListener { failed, _, _ ->
                failed.release()
                if (player === failed) player = null
                true
            }
            mp.prepareAsync()
        } catch (e: Exception) {
            // A cry is decoration: a bad URL or no audio focus shouldn't surface as an error.
            mp.release()
            player = null
        }
    }

    fun release() {
        player?.release()
        player = null
    }
}
