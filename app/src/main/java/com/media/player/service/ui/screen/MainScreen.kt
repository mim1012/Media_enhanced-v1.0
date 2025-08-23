package com.media.player.service.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.media.player.service.ui.components.RoundedCardButton
import com.media.player.service.ui.theme.MediaPlayerTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onDistanceClick: () -> Unit,
    onCallModeClick: () -> Unit,
    onDestinationClick: () -> Unit,
    onLoadDestinationClick: () -> Unit = {},
    onKeywordClick: () -> Unit,
    onSaveTemplateClick: () -> Unit,      // 추가: 템플릿 저장 콜백
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentDistance: String = "10km",
    currentCallMode: String = "전체콜 모드",
    destinationCount: Int = 0,
    isServiceRunning: Boolean = false
) {
    val currentDate = remember {
        SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        
        // 고객과의 거리 설정
        RoundedCardButton(
            text = "🎯 고객과의 거리: $currentDistance",
            onClick = onDistanceClick
        )
        
        // 전체콜 모드 설정
        RoundedCardButton(
            text = "📞 $currentCallMode",
            onClick = onCallModeClick,
            backgroundColor = when (currentCallMode) {
                "전체콜 모드" -> MaterialTheme.colorScheme.primary
                "부분콜 모드" -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.tertiary
            }
        )
        
        // 도착지 설정
        RoundedCardButton(
            text = if (destinationCount > 0) {
"🧭 도착지 (${destinationCount}개 지역)"
            } else {
                "🧭 도착지"
            },
            onClick = onDestinationClick
        )
        
        // 불러오기 버튼
        RoundedCardButton(
            text = "📂 불러오기",
            onClick = onLoadDestinationClick,
            backgroundColor = MaterialTheme.colorScheme.secondary
        )
        
        // 키워드 설정
        RoundedCardButton(
            text = "🔤 키워드 설정",
            onClick = onKeywordClick,
            backgroundColor = MaterialTheme.colorScheme.tertiary
        )
        
        // 템플릿 저장
        RoundedCardButton(
            text = "💾 템플릿 저장",
            onClick = onSaveTemplateClick,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 시작/중지 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 시작 버튼
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isServiceRunning) "⏸️" else "▶️",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isServiceRunning) "일시정지" else "재생",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // 중지 버튼
            Button(
                onClick = onStopClick,
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏹️",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "정지",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // 서비스 상태 표시
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isServiceRunning) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(
                text = if (isServiceRunning) "🟢 서비스 실행 중" else "🔴 서비스 중지됨",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = if (isServiceRunning) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MediaPlayerTheme {
        MainScreen(
            onDistanceClick = { },
            onCallModeClick = { },
            onDestinationClick = { },
            onKeywordClick = { },
            onSaveTemplateClick = { },     // 추가
            onStartClick = { },
            onStopClick = { },
            currentDistance = "무제한",
            currentCallMode = "부분콜 모드",
            destinationCount = 5,
            isServiceRunning = true
        )
    }
}