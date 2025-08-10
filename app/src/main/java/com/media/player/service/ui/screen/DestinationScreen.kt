package com.media.player.service.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.media.player.service.ui.theme.MediaPlayerTheme

data class Region(
    val name: String,
    val subRegions: List<Region> = emptyList(),
    val isExpanded: Boolean = false,
    val isSelected: Boolean = false,
    val level: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationScreen(
    onSave: (Map<String, Map<String, List<String>>>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialSelectedRegions: Map<String, Map<String, List<String>>> = emptyMap()
) {
    var searchQuery by remember { mutableStateOf("") }
    var regions by remember { mutableStateOf(getSampleRegions()) }
    var selectedCount by remember { mutableIntStateOf(0) }
    
    // 선택된 지역 수 계산
    LaunchedEffect(regions) {
        selectedCount = countSelectedRegions(regions)
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 상단 앱바
        TopAppBar(
            title = {
                Text(
                    text = "도착지 설정",
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
            // 검색창
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("지역 검색") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "검색"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 전체 선택/해제 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        regions = selectAllRegions(regions, true)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("전체 선택")
                }
                OutlinedButton(
                    onClick = {
                        regions = selectAllRegions(regions, false)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("전체 해제")
                }
            }
            
            // 지역 목록
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = if (searchQuery.isNotEmpty()) {
                        filterRegions(regions, searchQuery)
                    } else {
                        regions
                    }
                ) { region ->
                    RegionItem(
                        region = region,
                        onToggleExpand = { regionName ->
                            regions = toggleRegionExpansion(regions, regionName)
                        },
                        onToggleSelect = { regionName, level ->
                            regions = toggleRegionSelection(regions, regionName, level)
                        }
                    )
                }
            }
            
            // 저장 버튼
            Button(
                onClick = {
                    val selectedRegions = extractSelectedRegions(regions)
                    onSave(selectedRegions)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = true
            ) {
                Text(
                    text = "저장",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RegionItem(
    region: Region,
    onToggleExpand: (String) -> Unit,
    onToggleSelect: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (region.isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = (region.level * 16 + 8).dp,
                        top = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 체크박스
                Checkbox(
                    checked = region.isSelected,
                    onCheckedChange = { 
                        onToggleSelect(region.name, region.level)
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 지역명
                Text(
                    text = region.name,
                    fontSize = when (region.level) {
                        0 -> 16.sp
                        1 -> 14.sp
                        else -> 12.sp
                    },
                    fontWeight = when (region.level) {
                        0 -> FontWeight.Bold
                        1 -> FontWeight.Medium
                        else -> FontWeight.Normal
                    },
                    modifier = Modifier.weight(1f),
                    color = if (region.isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                // 확장/축소 버튼
                if (region.subRegions.isNotEmpty()) {
                    IconButton(
                        onClick = { onToggleExpand(region.name) }
                    ) {
                        Icon(
                            imageVector = if (region.isExpanded) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = if (region.isExpanded) "축소" else "확장"
                        )
                    }
                }
            }
        }
        
        // 하위 지역들
        if (region.isExpanded && region.subRegions.isNotEmpty()) {
            region.subRegions.forEach { subRegion ->
                RegionItem(
                    region = subRegion,
                    onToggleExpand = onToggleExpand,
                    onToggleSelect = onToggleSelect
                )
            }
        }
    }
}

// 샘플 지역 데이터 (실제 대한민국 전국 데이터)
fun getSampleRegions(): List<Region> {
    return listOf(
        Region(
            name = "서울특별시",
            level = 0,
            subRegions = listOf(
                Region(
                    name = "강남구",
                    level = 1,
                    subRegions = listOf(
                        Region("논현동", level = 2),
                        Region("역삼동", level = 2),
                        Region("삼성동", level = 2),
                        Region("대치동", level = 2)
                    )
                ),
                Region(
                    name = "강북구",
                    level = 1,
                    subRegions = listOf(
                        Region("수유동", level = 2),
                        Region("미아동", level = 2)
                    )
                ),
                Region(
                    name = "광진구",
                    level = 1,
                    subRegions = listOf(
                        Region("자양동", level = 2),
                        Region("구의동", level = 2)
                    )
                )
            )
        ),
        Region(
            name = "부산광역시",
            level = 0,
            subRegions = listOf(
                Region(
                    name = "해운대구",
                    level = 1,
                    subRegions = listOf(
                        Region("우동", level = 2),
                        Region("중동", level = 2)
                    )
                ),
                Region(
                    name = "부산진구",
                    level = 1,
                    subRegions = listOf(
                        Region("부전동", level = 2),
                        Region("서면동", level = 2)
                    )
                )
            )
        ),
        Region(
            name = "전라남도",
            level = 0,
            subRegions = listOf(
                Region(
                    name = "무안군",
                    level = 1,
                    subRegions = listOf(
                        Region("무안읍", level = 2),
                        Region("삼향읍", level = 2)
                    )
                ),
                Region(
                    name = "광주광역시",
                    level = 1,
                    subRegions = listOf(
                        Region("북구", level = 2),
                        Region("서구", level = 2),
                        Region("남구", level = 2)
                    )
                )
            )
        )
    )
}

// 유틸리티 함수들
fun countSelectedRegions(regions: List<Region>): Int {
    return regions.sumOf { region ->
        val current = if (region.isSelected) 1 else 0
        val children = countSelectedRegions(region.subRegions)
        current + children
    }
}

fun selectAllRegions(regions: List<Region>, selected: Boolean): List<Region> {
    return regions.map { region ->
        region.copy(
            isSelected = selected,
            subRegions = selectAllRegions(region.subRegions, selected)
        )
    }
}

fun toggleRegionExpansion(regions: List<Region>, regionName: String): List<Region> {
    return regions.map { region ->
        if (region.name == regionName) {
            region.copy(isExpanded = !region.isExpanded)
        } else {
            region.copy(
                subRegions = toggleRegionExpansion(region.subRegions, regionName)
            )
        }
    }
}

fun toggleRegionSelection(regions: List<Region>, regionName: String, level: Int): List<Region> {
    return regions.map { region ->
        if (region.name == regionName && region.level == level) {
            val newSelected = !region.isSelected
            region.copy(
                isSelected = newSelected,
                subRegions = selectAllRegions(region.subRegions, newSelected)
            )
        } else {
            region.copy(
                subRegions = toggleRegionSelection(region.subRegions, regionName, level)
            )
        }
    }
}

fun filterRegions(regions: List<Region>, query: String): List<Region> {
    return regions.mapNotNull { region ->
        val matchesQuery = region.name.contains(query, ignoreCase = true)
        val filteredSubRegions = filterRegions(region.subRegions, query)
        
        if (matchesQuery || filteredSubRegions.isNotEmpty()) {
            region.copy(
                subRegions = filteredSubRegions,
                isExpanded = if (filteredSubRegions.isNotEmpty()) true else region.isExpanded
            )
        } else {
            null
        }
    }
}

fun extractSelectedRegions(regions: List<Region>): Map<String, Map<String, List<String>>> {
    val result = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
    
    fun processRegion(region: Region, parentProvince: String? = null, parentCity: String? = null) {
        if (region.isSelected) {
            when (region.level) {
                0 -> { // 시/도
                    result[region.name] = mutableMapOf()
                }
                1 -> { // 구/군
                    parentProvince?.let { province ->
                        result.getOrPut(province) { mutableMapOf() }[region.name] = mutableListOf()
                    }
                }
                2 -> { // 동/읍/면
                    parentProvince?.let { province ->
                        parentCity?.let { city ->
                            result.getOrPut(province) { mutableMapOf() }
                                .getOrPut(city) { mutableListOf() }
                                .add(region.name)
                        }
                    }
                }
            }
        }
        
        region.subRegions.forEach { subRegion ->
            when (region.level) {
                0 -> processRegion(subRegion, region.name, null)
                1 -> processRegion(subRegion, parentProvince, region.name)
                else -> processRegion(subRegion, parentProvince, parentCity)
            }
        }
    }
    
    regions.forEach { processRegion(it) }
    return result.mapValues { it.value.toMap() }
}

@Preview(showBackground = true)
@Composable
fun DestinationScreenPreview() {
    MediaPlayerTheme {
        DestinationScreen(
            onSave = { },
            onBack = { }
        )
    }
}