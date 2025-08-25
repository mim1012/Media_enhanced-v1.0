const express = require('express');
const User = require('../models/User');
const router = express.Router();

/**
 * 앱에서 인증 확인 (최초 1회만)
 * POST /api/auth/verify
 */
router.post('/verify', async (req, res) => {
    try {
        const { phone_number, device_id, app_version } = req.body;
        
        // 입력 검증
        if (!phone_number || !device_id) {
            return res.status(400).json({
                authorized: false,
                message: "필수 정보가 누락되었습니다."
            });
        }
        
        // 사용자 조회
        const user = await User.findOne({ phone_number });
        
        if (!user) {
            return res.json({
                authorized: false,
                message: "등록되지 않은 사용자입니다.\n\n📞 관리자에게 문의해주세요:\n010-0000-0000"
            });
        }
        
        // 차단된 사용자 확인
        if (user.status === 'blocked') {
            return res.json({
                authorized: false,
                message: "차단된 사용자입니다.\n\n📞 관리자에게 문의해주세요:\n010-0000-0000"
            });
        }
        
        // 만료 확인
        if (new Date() > user.expires_at) {
            return res.json({
                authorized: false,
                message: "사용 기간이 만료되었습니다.\n\n📞 관리자에게 연장을 문의해주세요:\n010-0000-0000"
            });
        }
        
        // 인증 성공 - 디바이스 정보 업데이트
        user.device_id = device_id;
        user.last_auth = new Date();
        user.total_auths += 1;
        await user.save();
        
        console.log(`✅ 인증 성공: ${phone_number} (${user.type})`);
        
        res.json({
            authorized: true,
            type: user.type,
            expires_at: user.expires_at,
            remaining_days: user.remaining_days,
            message: `인증 완료! ${user.type === 'premium' ? '프리미엄' : '체험'} 사용자 (${user.remaining_days}일 남음)`
        });
        
    } catch (error) {
        console.error('인증 오류:', error);
        res.status(500).json({
            authorized: false,
            message: "서버 오류가 발생했습니다.\n잠시 후 다시 시도해주세요."
        });
    }
});

/**
 * 사용 통계 수집
 * POST /api/auth/stats
 */
router.post('/stats', async (req, res) => {
    try {
        const { phone_number, action, details } = req.body;
        
        // 사용자 찾기
        const user = await User.findOne({ phone_number });
        if (!user) return res.status(404).json({ error: '사용자 없음' });
        
        // 통계 업데이트
        user.stats.last_active = new Date();
        
        if (action === 'call_accepted') {
            user.stats.total_calls += 1;
            user.stats.accepted_calls += 1;
        } else if (action === 'call_rejected') {
            user.stats.total_calls += 1;
        }
        
        await user.save();
        
        res.json({ success: true });
        
    } catch (error) {
        console.error('통계 오류:', error);
        res.status(500).json({ error: '서버 오류' });
    }
});

module.exports = router;