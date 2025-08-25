const express = require('express');
const User = require('../models/User');
const moment = require('moment');
const router = express.Router();

/**
 * 관리자 인증 미들웨어
 */
const authenticateAdmin = (req, res, next) => {
    const password = req.headers.authorization;
    if (password !== process.env.ADMIN_PASSWORD) {
        return res.status(401).json({ error: '관리자 인증 실패' });
    }
    next();
};

/**
 * 신규 사용자 등록
 * POST /admin/register
 */
router.post('/register', authenticateAdmin, async (req, res) => {
    try {
        const { phone_number, type, custom_days, memo } = req.body;
        
        // 기간 계산
        let expiryDate;
        if (custom_days) {
            expiryDate = moment().add(parseInt(custom_days), 'days').toDate();
        } else {
            const days = type === 'premium' ? 90 : 7;  // 프리미엄 90일, 체험 7일
            expiryDate = moment().add(days, 'days').toDate();
        }
        
        // 중복 확인
        const existingUser = await User.findOne({ phone_number });
        if (existingUser) {
            return res.status(400).json({
                success: false,
                message: '이미 등록된 휴대폰 번호입니다.'
            });
        }
        
        // 새 사용자 생성
        const newUser = new User({
            phone_number,
            type,
            expires_at: expiryDate,
            memo: memo || '',
            status: 'active'
        });
        
        await newUser.save();
        
        console.log(`📱 신규 사용자 등록: ${phone_number} (${type}, ${moment(expiryDate).format('YYYY-MM-DD')})`);
        
        res.json({
            success: true,
            message: '사용자가 성공적으로 등록되었습니다.',
            user: {
                phone_number: newUser.phone_number,
                type: newUser.type,
                expires_at: newUser.expires_at,
                remaining_days: newUser.remaining_days
            }
        });
        
    } catch (error) {
        console.error('등록 오류:', error);
        res.status(500).json({
            success: false,
            message: '등록 중 오류가 발생했습니다.'
        });
    }
});

/**
 * 등록된 사용자 목록 조회
 * GET /admin/users
 */
router.get('/users', authenticateAdmin, async (req, res) => {
    try {
        const { status, type, search } = req.query;
        
        // 필터 조건 구성
        let filter = {};
        if (status) filter.status = status;
        if (type) filter.type = type;
        if (search) {
            filter.$or = [
                { phone_number: { $regex: search, $options: 'i' } },
                { memo: { $regex: search, $options: 'i' } }
            ];
        }
        
        const users = await User.find(filter)
            .sort({ registered_at: -1 })
            .limit(100);
        
        // 통계 계산
        const totalUsers = await User.countDocuments();
        const activeUsers = await User.countDocuments({ 
            status: 'active',
            expires_at: { $gt: new Date() }
        });
        const expiredUsers = await User.countDocuments({
            expires_at: { $lt: new Date() }
        });
        
        res.json({
            users: users.map(user => ({
                _id: user._id,
                phone_number: user.phone_number,
                type: user.type,
                registered_at: moment(user.registered_at).format('MM-DD'),
                expires_at: moment(user.expires_at).format('MM-DD'),
                remaining_days: user.remaining_days,
                status: user.status,
                memo: user.memo,
                last_auth: user.last_auth ? moment(user.last_auth).fromNow() : '없음',
                total_auths: user.total_auths
            })),
            summary: {
                total: totalUsers,
                active: activeUsers,
                expired: expiredUsers,
                blocked: await User.countDocuments({ status: 'blocked' })
            }
        });
        
    } catch (error) {
        console.error('사용자 목록 조회 오류:', error);
        res.status(500).json({ error: '서버 오류' });
    }
});

/**
 * 사용자 기간 연장
 * POST /admin/extend
 */
router.post('/extend', authenticateAdmin, async (req, res) => {
    try {
        const { user_id, extend_days } = req.body;
        
        const user = await User.findById(user_id);
        if (!user) {
            return res.status(404).json({ error: '사용자를 찾을 수 없습니다.' });
        }
        
        // 기간 연장
        const currentExpiry = new Date(user.expires_at);
        const newExpiry = moment(currentExpiry).add(extend_days, 'days').toDate();
        
        user.expires_at = newExpiry;
        await user.save();
        
        console.log(`⏰ 기간 연장: ${user.phone_number} → ${moment(newExpiry).format('YYYY-MM-DD')}`);
        
        res.json({
            success: true,
            message: `${extend_days}일 연장되었습니다.`,
            new_expiry: newExpiry,
            remaining_days: Math.ceil((newExpiry - Date.now()) / (1000 * 60 * 60 * 24))
        });
        
    } catch (error) {
        console.error('기간 연장 오류:', error);
        res.status(500).json({ error: '서버 오류' });
    }
});

/**
 * 사용자 차단/해제
 * POST /admin/block
 */
router.post('/block', authenticateAdmin, async (req, res) => {
    try {
        const { user_id, block } = req.body;  // block: true/false
        
        const user = await User.findById(user_id);
        if (!user) {
            return res.status(404).json({ error: '사용자를 찾을 수 없습니다.' });
        }
        
        user.status = block ? 'blocked' : 'active';
        await user.save();
        
        console.log(`🚫 사용자 ${block ? '차단' : '차단해제'}: ${user.phone_number}`);
        
        res.json({
            success: true,
            message: `사용자가 ${block ? '차단' : '차단해제'}되었습니다.`,
            new_status: user.status
        });
        
    } catch (error) {
        console.error('사용자 차단 오류:', error);
        res.status(500).json({ error: '서버 오류' });
    }
});

/**
 * 사용자 삭제
 * DELETE /admin/user/:id
 */
router.delete('/user/:id', authenticateAdmin, async (req, res) => {
    try {
        const { id } = req.params;
        
        const user = await User.findByIdAndDelete(id);
        if (!user) {
            return res.status(404).json({ error: '사용자를 찾을 수 없습니다.' });
        }
        
        console.log(`🗑️ 사용자 삭제: ${user.phone_number}`);
        
        res.json({
            success: true,
            message: '사용자가 삭제되었습니다.'
        });
        
    } catch (error) {
        console.error('사용자 삭제 오류:', error);
        res.status(500).json({ error: '서버 오류' });
    }
});

module.exports = router;