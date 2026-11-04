package com.oaklog.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.oaklog.app.presentation.theme.CardShape
import com.oaklog.app.presentation.theme.ChipShape
import com.oaklog.app.presentation.theme.TypeTint
import com.oaklog.app.presentation.theme.typeTint

/**
 * Page layout shared by the Explore databases: a header painted in the section's color (the same
 * color as its Explore tile), a pill search field, optional filter chips, then the content.
 *
 * [color] is a type name used only as a hue, so each database keeps its own identity.
 */
@Composable
fun DatabasePage(
    title: String,
    subtitle: String?,
    color: String,
    icon: ImageVector,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    query: String = "",
    /** Null hides the search field, for pages that filter with dropdowns instead. */
    onQueryChange: ((String) -> Unit)? = null,
    searchHint: String = "",
    chips: (@Composable RowScope.() -> Unit)? = null,
    /** A shorter header for form-heavy pages: no subtitle, less padding. */
    compact: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tint = typeTint(color)
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).navigationBarsPadding()) {
        // Header: quiet like the Pokédex. A back chip and a small tinted icon chip, then overline and title.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = if (compact) 6.dp else 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(tint.container),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = tint.onContainer, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                text = "EXPLORE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = if (compact) 10.dp else 16.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null && !compact) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (onQueryChange != null) OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(searchHint) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            },
            singleLine = true,
            shape = ChipShape,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 14.dp, bottom = 6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
        if (chips != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = chips,
            )
        }
        if (onQueryChange == null) androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
        content()
    }
}

/** A card painted in [tint]; with [onClick] it is tappable. Content gets 16 dp of padding. */
@Composable
fun TintedCard(
    tint: TypeTint,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(tint.container)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

/** A small label over a bold value, used in rows of stats on a tinted card. */
@Composable
fun StatColumn(label: String, value: String, tint: TypeTint) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint.onContainer.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.titleSmall, color = tint.onContainer)
    }
}

/** Translucent pill on a tinted card. */
@Composable
fun TintPill(text: String, tint: TypeTint, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = tint.onContainer,
        modifier = modifier.clip(ChipShape).background(tint.chip).padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Keeps long result lists from needing a spacer between every card. */
val DatabaseListPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
