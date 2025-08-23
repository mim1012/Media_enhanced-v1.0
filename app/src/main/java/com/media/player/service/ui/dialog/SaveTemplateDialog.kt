package com.media.player.service.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.media.player.service.utils.DataStore
import com.media.player.service.utils.Preset
import com.media.player.service.utils.Config

@Composable
fun SaveTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    currentCallMode: String,
    currentDistance: String
) {
    var templateName by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }
    
    // 현재 설정 정보 가져오기
    val hasKeywords = DataStore.aFilterList?.isNotEmpty() == true
    val hasDistance = DataStore.nQuality > 0
    val keywordText = if (hasKeywords) {
        DataStore.aFilterList.take(3).joinToString(", ") +
        if (DataStore.aFilterList.size > 3) " 외 ${DataStore.aFilterList.size - 3}개" else ""
    } else "설정 안 됨"
    
    // 유효성 검사
    isValid = templateName.trim().isNotEmpty() && templateName.length <= 20
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 제목
                Text(
                    text = "💾 템플릿 저장",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 템플릿 이름 입력
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("템플릿 이름") },
                    placeholder = { Text("예: 강남 근무, 야간 운행") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = templateName.isNotEmpty() && !isValid
                )
                
                if (templateName.isNotEmpty() && !isValid) {
                    Text(
                        text = "1~20자 이내로 입력해주세요",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 현재 설정 표시
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "저장될 설정",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 모드
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📞 모드: ", fontSize = 12.sp)
                            Text(
                                text = currentCallMode,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 거리
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎯 거리: ", fontSize = 12.sp)
                            Text(
                                text = currentDistance,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (hasDistance) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 키워드
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔤 키워드: ", fontSize = 12.sp)
                            Text(
                                text = keywordText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (hasKeywords) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // 유효성 경고
                        if (currentCallMode == "부분콜 모드" && !hasKeywords && !hasDistance) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ 부분콜 모드는 거리나 키워드 중 하나는 설정해야 합니다",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 취소 버튼
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }
                    
                    // 저장 버튼
                    Button(
                        onClick = { 
                            if (isValid) {
                                onSave(templateName.trim())
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isValid && !(currentCallMode == "부분콜 모드" && !hasKeywords && !hasDistance)
                    ) {
                        Text("저장")
                    }
                }
            }
        }
    }
}