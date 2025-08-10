package com.media.player.service.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.media.player.service.ui.theme.MediaPlayerTheme

@Composable
fun TagItem(
    text: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "삭제",
                    modifier = Modifier.size(14.dp)
                )
            }
        },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = backgroundColor,
            labelColor = contentColor,
            trailingIconContentColor = contentColor
        )
    )
}

@Composable
fun TagList(
    tags: List<String>,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(tags) { tag ->
            TagItem(
                text = tag,
                onRemove = { onRemoveTag(tag) },
                backgroundColor = backgroundColor,
                contentColor = contentColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagItemPreview() {
    MediaPlayerTheme {
        var tags by remember { 
            mutableStateOf(listOf("백제고", "무안고", "북구청", "광주역", "송정역"))
        }
        
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "수락 키워드",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            TagList(
                tags = tags,
                onRemoveTag = { tag ->
                    tags = tags.filter { it != tag }
                }
            )
            
            Text(
                text = "제외 키워드",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            TagList(
                tags = listOf("병원", "응급실", "야간"),
                onRemoveTag = { },
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
