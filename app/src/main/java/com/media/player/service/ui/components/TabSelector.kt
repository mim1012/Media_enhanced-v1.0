package com.media.player.service.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TabSelector(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<String>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        tabs.forEachIndexed { index, title ->
            FilterChip(
                onClick = { onTabSelected(index) },
                label = { Text(title) },
                selected = selectedTabIndex == index,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}