package com.media.player.service.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.media.player.service.ui.components.TabSelector
import com.media.player.service.ui.components.TagItem
import com.media.player.service.ui.theme.MediaPlayerTheme

data class KeywordData(
    val acceptKeywords: List<String> = emptyList(),
    val rejectKeywords: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeywordScreen(
    initialKeywords: KeywordData = KeywordData(),
    onSave: (KeywordData) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var acceptKeywords by remember { mutableStateOf(initialKeywords.acceptKeywords) }
    var rejectKeywords by remember { mutableStateOf(initialKeywords.rejectKeywords) }
    var newKeyword by remember { mutableStateOf("") }
    
    val tabs = listOf("수락 키워드", "제외 키워드")
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 상단 앱바
        TopAppBar(
            title = {
                Text(
                    text = "키워드 설정",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 탭 선택기
            TabSelector(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )
            
            // 현재 키워드 개수 표시
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTabIndex == 0) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTabIndex == 0) "수락 키워드" else "제외 키워드",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (selectedTabIndex == 0) acceptKeywords.size else rejectKeywords.size}개",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 키워드 입력 필드
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newKeyword,
                    onValueChange = { newKeyword = it },
                    label = { 
                        Text(
                            if (selectedTabIndex == 0) "수락할 키워드 입력" else "제외할 키워드 입력"
                        ) 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newKeyword.isNotBlank()) {
                                if (selectedTabIndex == 0) {
                                    acceptKeywords = acceptKeywords + newKeyword.trim()
                                } else {
                                    rejectKeywords = rejectKeywords + newKeyword.trim()
                                }
                                newKeyword = ""
                                keyboardController?.hide()
                            }
                        }
                    )
                )
                
                FloatingActionButton(
                    onClick = {
                        if (newKeyword.isNotBlank()) {
                            if (selectedTabIndex == 0) {
                                acceptKeywords = acceptKeywords + newKeyword.trim()
                            } else {
                                rejectKeywords = rejectKeywords + newKeyword.trim()
                            }
                            newKeyword = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    containerColor = if (selectedTabIndex == 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "추가"
                    )
                }
            }
            
            // 키워드 목록
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTabIndex == 0) {
                    // 수락 키워드
                    if (acceptKeywords.isNotEmpty()) {
                        item {
                            KeywordSection(
                                title = "수락 키워드 목록",
                                keywords = acceptKeywords,
                                onRemoveKeyword = { keyword ->
                                    acceptKeywords = acceptKeywords.filter { it != keyword }
                                },
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    // 예시 키워드
                    item {
                        KeywordExamples(
                            title = "수락 키워드 예시",
                            examples = listOf("백제고", "무안고", "북구청", "광주역", "송정역", "병원", "대학교"),
                            onAddExample = { example ->
                                if (!acceptKeywords.contains(example)) {
                                    acceptKeywords = acceptKeywords + example
                                }
                            },
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else {
                    // 제외 키워드
                    if (rejectKeywords.isNotEmpty()) {
                        item {
                            KeywordSection(
                                title = "제외 키워드 목록",
                                keywords = rejectKeywords,
                                onRemoveKeyword = { keyword ->
                                    rejectKeywords = rejectKeywords.filter { it != keyword }
                                },
                                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    
                    // 예시 키워드
                    item {
                        KeywordExamples(
                            title = "제외 키워드 예시",
                            examples = listOf("응급실", "야간", "새벽", "장거리", "고속도로", "톨게이트"),
                            onAddExample = { example ->
                                if (!rejectKeywords.contains(example)) {
                                    rejectKeywords = rejectKeywords + example
                                }
                            },
                            backgroundColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // 저장 버튼
            Button(
                onClick = {
                    onSave(KeywordData(acceptKeywords, rejectKeywords))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "저장",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun KeywordSection(
    title: String,
    keywords: List<String>,
    onRemoveKeyword: (String) -> Unit,
    backgroundColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(keywords) { keyword ->
                TagItem(
                    text = keyword,
                    onRemove = { onRemoveKeyword(keyword) },
                    backgroundColor = backgroundColor,
                    contentColor = contentColor
                )
            }
        }
    }
}

@Composable
fun KeywordExamples(
    title: String,
    examples: List<String>,
    onAddExample: (String) -> Unit,
    backgroundColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(examples) { example ->
                AssistChip(
                    onClick = { onAddExample(example) },
                    label = {
                        Text(
                            text = example,
                            fontSize = 12.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = backgroundColor.copy(alpha = 0.3f),
                        labelColor = contentColor
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KeywordScreenPreview() {
    MediaPlayerTheme {
        KeywordScreen(
            initialKeywords = KeywordData(
                acceptKeywords = listOf("백제고", "무안고", "북구청"),
                rejectKeywords = listOf("응급실", "야간")
            ),
            onSave = { },
            onBack = { }
        )
    }
}