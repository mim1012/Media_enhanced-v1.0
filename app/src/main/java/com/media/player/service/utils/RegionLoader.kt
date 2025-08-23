package com.media.player.service.utils

import android.content.Context
import com.media.player.service.db.DatabaseHelper
import com.media.player.service.ui.screen.Region

/**
 * 지역 데이터 로더 - DB에서 실제 전국 데이터 가져오기
 */
object RegionLoader {
    
    /**
     * DB에서 전국 8도 체계로 데이터를 가져와서 Region 트리 구조로 변환
     */
    fun loadRegionsFromDB(context: Context): List<Region> {
        val regions = mutableListOf<Region>()
        
        try {
            val dbHelper = DatabaseHelper(context)
            
            // DB 초기화 확인
            if (!dbHelper.isDataInitialized()) {
                Logger.log("DB 초기화 중...")
                return get8DoRegions()
            }
            
            val db = dbHelper.readableDatabase
            
            // 8도 체계로 분류
            val provinces = mapOf(
                "경기도" to listOf("서울특별시", "인천광역시", "경기도"),
                "강원도" to listOf("강원특별자치도", "강원도"),
                "충청도" to listOf("대전광역시", "세종특별자치시", "충청북도", "충청남도"),
                "전라도" to listOf("광주광역시", "전라북도", "전북특별자치도", "전라남도"),
                "경상도" to listOf("부산광역시", "대구광역시", "울산광역시", "경상북도", "경상남도"),
                "제주도" to listOf("제주특별자치도")
            )
            
            for ((provinceName, sidoList) in provinces) {
                val provinceRegions = mutableListOf<Region>()
                
                for (sidoName in sidoList) {
                    val sidoCursor = db.rawQuery(
                        "SELECT DISTINCT sido_name FROM address_sigungus WHERE sido_name = ? ORDER BY sido_name",
                        arrayOf(sidoName)
                    )
                    
                    if (sidoCursor != null && sidoCursor.moveToFirst()) {
                        val actualSidoName = sidoCursor.getString(0)
                        val subRegions = loadSigungusBySido(context, actualSidoName)
                        
                        if (subRegions.isNotEmpty()) {
                            provinceRegions.add(
                                Region(
                                    name = actualSidoName,
                                    level = 1,
                                    subRegions = subRegions,
                                    isExpanded = false,
                                    isSelected = false
                                )
                            )
                        }
                    }
                    sidoCursor?.close()
                }
                
                if (provinceRegions.isNotEmpty()) {
                    regions.add(
                        Region(
                            name = provinceName,
                            level = 0,
                            subRegions = provinceRegions,
                            isExpanded = false,
                            isSelected = false
                        )
                    )
                }
            }
            
            db.close()
            
        } catch (e: Exception) {
            Logger.log("지역 데이터 로드 실패: ${e.message}")
            return get8DoRegions()
        }
        
        return regions.ifEmpty { get8DoRegions() }
    }
    
    /**
     * 특정 시도의 시군구 목록 가져오기
     */
    private fun loadSigungusBySido(context: Context, sidoName: String): List<Region> {
        val sigungus = mutableListOf<Region>()
        
        try {
            val dbHelper = DatabaseHelper(context)
            val db = dbHelper.readableDatabase
            
            val cursor = db.rawQuery(
                "SELECT sigungu_name FROM address_sigungus WHERE sido_name = ? ORDER BY sigungu_name",
                arrayOf(sidoName)
            )
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val sigunguName = cursor.getString(0)
                    
                    // 해당 시군구의 행정동들 가져오기
                    val hjdongs = loadHjdongsBySigungu(context, sidoName, sigunguName)
                    
                    sigungus.add(
                        Region(
                            name = sigunguName,
                            level = 2,  // 시군구는 2단계
                            subRegions = hjdongs,
                            isExpanded = false,
                            isSelected = false
                        )
                    )
                }
                cursor.close()
            }
            db.close()
            
        } catch (e: Exception) {
            Logger.log("시군구 데이터 로드 실패: ${e.message}")
        }
        
        return sigungus
    }
    
    /**
     * 특정 시군구의 행정동 목록 가져오기
     */
    private fun loadHjdongsBySigungu(context: Context, sidoName: String, sigunguName: String): List<Region> {
        val hjdongs = mutableListOf<Region>()
        
        try {
            val dbHelper = DatabaseHelper(context)
            val db = dbHelper.readableDatabase
            
            val cursor = db.rawQuery(
                "SELECT DISTINCT hjdong_name FROM address_hjdongs WHERE sido_name = ? AND sigungu_name = ? ORDER BY hjdong_name",
                arrayOf(sidoName, sigunguName)
            )
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val hjdongName = cursor.getString(0)
                    
                    hjdongs.add(
                        Region(
                            name = hjdongName,
                            level = 3,  // 읍면리는 3단계
                            subRegions = emptyList(),
                            isExpanded = false,
                            isSelected = false
                        )
                    )
                }
                cursor.close()
            }
            db.close()
            
        } catch (e: Exception) {
            Logger.log("행정동 데이터 로드 실패: ${e.message}")
        }
        
        return hjdongs
    }
    
    /**
     * 8도 체계 샘플 데이터 (DB 로드 실패 시 사용)
     */
    private fun get8DoRegions(): List<Region> {
        return listOf(
            Region(
                name = "경기도",
                level = 0,
                subRegions = listOf(
                    Region(
                        name = "서울특별시",
                        level = 1,
                        subRegions = listOf(
                            Region("강남구", level = 2, subRegions = listOf(Region("역삼동", level = 3))),
                            Region("강북구", level = 2, subRegions = listOf(Region("수유동", level = 3)))
                        )
                    ),
                    Region(
                        name = "경기도",
                        level = 1,
                        subRegions = listOf(
                            Region("수원시", level = 2, subRegions = listOf(Region("팔달구", level = 3))),
                            Region("성남시", level = 2, subRegions = listOf(Region("분당구", level = 3)))
                        )
                    )
                )
            ),
            Region(
                name = "충청도",
                level = 0,
                subRegions = listOf(
                    Region(
                        name = "충청북도",
                        level = 1,
                        subRegions = listOf(
                            Region("청주시", level = 2, subRegions = listOf(Region("상당구", level = 3)))
                        )
                    ),
                    Region(
                        name = "충청남도", 
                        level = 1,
                        subRegions = listOf(
                            Region("천안시", level = 2, subRegions = listOf(Region("동남구", level = 3)))
                        )
                    )
                )
            ),
            Region(
                name = "전라도",
                level = 0,
                subRegions = listOf(
                    Region(
                        name = "전라북도",
                        level = 1,
                        subRegions = listOf(
                            Region("전주시", level = 2, subRegions = listOf(Region("완산구", level = 3)))
                        )
                    ),
                    Region(
                        name = "전라남도",
                        level = 1, 
                        subRegions = listOf(
                            Region("목포시", level = 2, subRegions = listOf(Region("용해동", level = 3)))
                        )
                    )
                )
            ),
            Region(
                name = "경상도",
                level = 0,
                subRegions = listOf(
                    Region(
                        name = "경상북도",
                        level = 1,
                        subRegions = listOf(
                            Region("대구광역시", level = 2, subRegions = listOf(Region("중구", level = 3)))
                        )
                    ),
                    Region(
                        name = "경상남도",
                        level = 1,
                        subRegions = listOf(
                            Region("부산광역시", level = 2, subRegions = listOf(Region("해운대구", level = 3)))
                        )
                    )
                )
            ),
            Region(
                name = "강원도",
                level = 0,
                subRegions = listOf(
                    Region(
                        name = "강원특별자치도",
                        level = 1,
                        subRegions = listOf(
                            Region("춘천시", level = 2, subRegions = listOf(Region("소양로", level = 3)))
                        )
                    )
                )
            ),
            Region(
                name = "제주도",
                level = 0,
                subRegions = listOf(
                    Region(
                        name = "제주특별자치도",
                        level = 1,
                        subRegions = listOf(
                            Region("제주시", level = 2, subRegions = listOf(Region("일도동", level = 3)))
                        )
                    )
                )
            )
        )
    }
    
    /**
     * 폴백용 기본 샘플 데이터 (호환성)
     */
    private fun getSampleRegions(): List<Region> {
        return get8DoRegions()
    }
}