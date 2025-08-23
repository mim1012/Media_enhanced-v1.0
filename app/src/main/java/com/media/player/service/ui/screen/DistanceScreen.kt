package com.media.player.service.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.media.player.service.ui.theme.MediaPlayerTheme
import com.media.player.service.utils.DataStore
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistanceScreen(
    initialDistance: Float = 3f,
    onSave: (Float) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 거리 프리셋을 슬라이더 값으로 매핑
    val presets = DataStore.QUALITY_PRESETS
    val currentIndex = remember(initialDistance) {
        if (initialDistance >= 51f) {
            presets.size - 1  // "무제한"
        } else {
            val targetPreset = "${initialDistance.toInt()}km"
            presets.indexOf(targetPreset).let { if (it == -1) 2 else it }  // 기본값 3km
        }
    }
    
    var sliderValue by remember { mutableFloatStateOf(currentIndex.toFloat()) }
    val selectedPreset = presets[sliderValue.roundToInt().coerceIn(0, presets.size - 1)]
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 상단 앱바
        TopAppBar(
            title = {
                Text(
                    text = "고객과의 거리",
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
        
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = 80.dp),  // 하단 버튼 공간 확보
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // 현재 설정 표시
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "설정된 거리",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedPreset,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (selectedPreset == "무제한") {
                            "모든 거리의 콜 수락"
                        } else {
                            "이 거리 이내의 콜만 수락"
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // 드래그 안내
            Text(
                text = "🖱️ 아래 바를 좌우로 드래그해서 거리를 설정하세요",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 거리 드래그 슬라이더
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "거리 선택",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..(presets.size - 1).toFloat(),
                    steps = presets.size - 2,  // 중간 스텝들
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 양끝 라벨
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = presets.first(),  // "0.8km"
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = presets.last(),   // "무제한"
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            }
            
            // 하단 고정 저장 버튼
            Button(
                onClick = {
                    val distance = when (selectedPreset) {
                        "무제한" -> 51f
                        else -> selectedPreset.replace("km", "").toFloatOrNull() ?: 3f
                    }
                    onSave(distance)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp)
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

@Preview(showBackground = true)
@Composable
fun DistanceScreenPreview() {
    MediaPlayerTheme {
        DistanceScreen(
            initialDistance = 3f,
            onSave = { },
            onBack = { }
        )
    }
}