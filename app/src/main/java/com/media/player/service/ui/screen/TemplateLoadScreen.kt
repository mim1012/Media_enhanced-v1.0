package com.media.player.service.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.media.player.service.ui.theme.MediaPlayerTheme
import com.media.player.service.utils.DataStore
import com.media.player.service.utils.Preset
import com.media.player.service.utils.Config
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateLoadScreen(
    onBack: () -> Unit,
    onTemplateSelected: (Preset) -> Unit,
    onTemplateDeleted: (Preset) -> Unit,
    modifier: Modifier = Modifier
) {
    val templates = remember { DataStore.aPresetList ?: emptyList() }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 상단 앱바
        TopAppBar(
            title = {
                Text(
                    text = "📂 템플릿 불러오기",
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
            }
        )
        
        if (templates.isEmpty()) {
            // 빈 상태
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📝",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "저장된 템플릿이 없습니다",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "메인화면에서 '💾 템플릿 저장'을\n눌러서 현재 설정을 저장해보세요",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            // 템플릿 목록
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates) { template ->
                    TemplateCard(
                        template = template,
                        onSelect = { onTemplateSelected(template) },
                        onDelete = { onTemplateDeleted(template) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateCard(
    template: Preset,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 설정 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 모드
                val modeText = when {
                    template.fullMode -> "전체콜"
                    template.nMode == Config.MODE_DEST -> "부분콜"  
                    template.nMode == Config.MODE_NONE -> "키워드"
                    else -> "기타"
                }
                
                Text(
                    text = "📞 $modeText",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 거리
                if (template.qualityPreset.isNotEmpty()) {
                    Text(
                        text = "🎯 ${template.qualityPreset}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            // 키워드
            if (template.filters.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val keywords = DataStore.parseStr2Array(template.filters).take(3)
                val keywordText = keywords.joinToString(", ") + 
                    if (DataStore.parseStr2Array(template.filters).size > 3) " 외 +" else ""
                
                Text(
                    text = "🔤 $keywordText",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 생성일
            Spacer(modifier = Modifier.height(8.dp))
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            Text(
                text = "생성: ${dateFormat.format(Date(template.createdAt))}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("템플릿 삭제") },
            text = { Text("'${template.name}' 템플릿을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TemplateLoadScreenPreview() {
    MediaPlayerTheme {
        TemplateLoadScreen(
            onBack = { },
            onTemplateSelected = { },
            onTemplateDeleted = { }
        )
    }
}