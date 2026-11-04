package com.oaklog.app.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * OakLog's own icons, drawn to match Material's outlined set: 24 dp, 2 dp stroke, round caps. They use
 * a black stroke that Icon() tints, so they follow the surrounding content color.
 */
object OakIcons {

    private fun icon(name: String, block: androidx.compose.ui.graphics.vector.ImageVector.Builder.() -> Unit) =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply(block).build()

    private fun ImageVector.Builder.stroke(block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit) =
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        )

    /** A Poké Ball: ring, belt, and button. */
    val PokeBall: ImageVector by lazy {
        icon("PokeBall") {
            stroke {
                moveTo(21f, 12f)
                arcToRelative(9f, 9f, 0f, true, true, -18f, 0f)
                arcToRelative(9f, 9f, 0f, true, true, 18f, 0f)
                close()
            }
            stroke { moveTo(3f, 12f); horizontalLineTo(8.5f) }
            stroke { moveTo(15.5f, 12f); horizontalLineTo(21f) }
            stroke {
                moveTo(15f, 12f)
                arcToRelative(3f, 3f, 0f, true, true, -6f, 0f)
                arcToRelative(3f, 3f, 0f, true, true, 6f, 0f)
                close()
            }
        }
    }

    /** The Pokédex device: a body, its lens, a screen and two buttons. */
    val Pokedex: ImageVector by lazy {
        icon("Pokedex") {
            stroke {
                moveTo(7f, 3f); horizontalLineTo(17f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
                verticalLineTo(19f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
                horizontalLineTo(7f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                verticalLineTo(5f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
                close()
            }
            stroke {
                moveTo(10.5f, 7f)
                arcToRelative(1.5f, 1.5f, 0f, true, true, -3f, 0f)
                arcToRelative(1.5f, 1.5f, 0f, true, true, 3f, 0f)
                close()
            }
            stroke { moveTo(8f, 12f); horizontalLineTo(16f); verticalLineTo(15.5f); horizontalLineTo(8f); close() }
            stroke { moveTo(8.5f, 18.5f); horizontalLineTo(8.6f) }
            stroke { moveTo(12f, 18.5f); horizontalLineTo(12.1f) }
        }
    }

    /** A berry: a round fruit with a leaf and stem. */
    val Berry: ImageVector by lazy {
        icon("Berry") {
            stroke {
                moveTo(18f, 14f)
                arcToRelative(6f, 6f, 0f, true, true, -12f, 0f)
                arcToRelative(6f, 6f, 0f, true, true, 12f, 0f)
                close()
            }
            stroke { moveTo(12f, 8f); curveTo(12f, 6f, 12.5f, 4.5f, 14f, 3.5f) }
            stroke {
                moveTo(12f, 6.5f)
                curveTo(13.5f, 5f, 16f, 4.5f, 18f, 5.5f)
                curveTo(17f, 7.5f, 14.5f, 8f, 12f, 6.5f)
                close()
            }
        }
    }
}
