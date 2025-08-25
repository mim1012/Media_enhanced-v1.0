const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const cors = require('cors');
const bodyParser = require('body-parser');
const moment = require('moment');
const path = require('path');
// AdminManager 클래스 직접 구현 (파일 분리 대신)
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// SQLite 데이터베이스 초기화
const dbPath = path.join(__dirname, 'media_enhanced.db');
const db = new sqlite3.Database(dbPath);

// 관리자 권한 헬퍼 함수들
function getPermissions(role) {
    const permissions = {
        super: ['user_register', 'user_extend', 'user_block', 'user_delete', 'view_statistics', 'admin_manage', 'system_control'],
        manager: ['user_register', 'user_extend', 'user_block', 'view_statistics']
    };
    return permissions[role] || [];
}

function hasPermission(userRole, action) {
    return getPermissions(userRole).includes(action);
}

// 관리자 인증 함수
function authenticateAdminUser(username, password) {
    return new Promise((resolve, reject) => {
        db.get(`
            SELECT * FROM admins 
            WHERE username = ? AND password = ? AND active = 1
        `, [username, password], (err, admin) => {
            if (err) reject(err);
            else if (admin) {
                db.run(`UPDATE admins SET last_login = CURRENT_TIMESTAMP WHERE id = ?`, [admin.id]);
                resolve({
                    id: admin.id, 
                    username: admin.username, 
                    role: admin.role, 
                    name: admin.name,
                    permissions: getPermissions(admin.role)
                });
            } else resolve(null);
        });
    });
}

// 미들웨어
app.use(cors());
app.use(bodyParser.json());
app.use(express.static('public'));

// 관리자 인증 미들웨어 (토큰 기반)
const authenticateAdmin = (req, res, next) => {
    const token = req.headers.authorization;
    if (!token) {
        return res.status(401).json({ error: '인증 토큰이 필요합니다' });
    }
    
    // 간단한 토큰 검증 (실제로는 JWT 사용 권장)
    const adminData = JSON.parse(Buffer.from(token, 'base64').toString());
    if (!adminData.username || !adminData.role) {
        return res.status(401).json({ error: '유효하지 않은 토큰입니다' });
    }
    
    req.admin = adminData;
    next();
};

// 권한 체크 미들웨어
const checkPermission = (requiredPermission) => {
    return (req, res, next) => {
        if (!hasPermission(req.admin.role, requiredPermission)) {
            return res.status(403).json({ 
                error: '권한이 없습니다',
                required: requiredPermission,
                your_role: req.admin.role
            });
        }
        next();
    };
};

// 데이터베이스 테이블 생성
db.serialize(() => {
    db.run(`
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            phone_number TEXT UNIQUE NOT NULL,
            device_id TEXT,
            type TEXT DEFAULT 'trial',
            registered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            expires_at DATETIME NOT NULL,
            status TEXT DEFAULT 'active',
            memo TEXT DEFAULT '',
            last_auth DATETIME,
            total_auths INTEGER DEFAULT 0,
            total_calls INTEGER DEFAULT 0,
            accepted_calls INTEGER DEFAULT 0,
            last_active DATETIME
        )
    `);
    
    console.log('📊 SQLite 데이터베이스 초기화 완료');
    
    // 관리자 테이블도 여기서 생성
    db.run(`
        CREATE TABLE IF NOT EXISTS admins (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL,
            role TEXT NOT NULL,
            name TEXT NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            last_login DATETIME,
            active INTEGER DEFAULT 1
        )
    `);
    
    // 기본 관리자 계정 생성
    const defaultAdmins = [
        { username: 'super_admin', password: 'dev_master_2024!', role: 'super', name: '총 책임자 (개발자)' },
        { username: 'manager1', password: 'mgr_2024_01!', role: 'manager', name: '관리직원 1' },
        { username: 'manager2', password: 'mgr_2024_02!', role: 'manager', name: '관리직원 2' }
    ];
    
    defaultAdmins.forEach(admin => {
        db.run(`
            INSERT OR IGNORE INTO admins (username, password, role, name)
            VALUES (?, ?, ?, ?)
        `, [admin.username, admin.password, admin.role, admin.name]);
    });
    
    console.log('👤 관리자 계정 초기화 완료');
});

// 관리자 로그인 API
app.post('/admin/login', async (req, res) => {
    try {
        const { username, password } = req.body;
        
        const admin = await authenticateAdminUser(username, password);
        
        if (admin) {
            // 로그인 성공 - 토큰 생성
            const token = Buffer.from(JSON.stringify({
                username: admin.username,
                role: admin.role,
                name: admin.name
            })).toString('base64');
            
            console.log(`👤 관리자 로그인: ${admin.name} (${admin.role})`);
            
            res.json({
                success: true,
                token: token,
                admin: {
                    username: admin.username,
                    role: admin.role,
                    name: admin.name,
                    permissions: admin.permissions
                }
            });
        } else {
            res.status(401).json({
                success: false,
                message: '아이디 또는 비밀번호가 잘못되었습니다.'
            });
        }
        
    } catch (error) {
        console.error('로그인 오류:', error);
        res.status(500).json({
            success: false,
            message: '서버 오류가 발생했습니다.'
        });
    }
});

// 기본 페이지 리다이렉트
app.get('/admin', (req, res) => {
    res.redirect('/login.html');
});

// 인증 API
app.post('/api/auth/verify', (req, res) => {
    const { phone_number, device_id, app_version } = req.body;
    
    if (!phone_number || !device_id) {
        return res.status(400).json({
            authorized: false,
            message: "필수 정보가 누락되었습니다."
        });
    }
    
    db.get(`
        SELECT * FROM users 
        WHERE phone_number = ? AND status = 'active'
    `, [phone_number], (err, user) => {
        if (err) {
            console.error('DB 오류:', err);
            return res.status(500).json({
                authorized: false,
                message: "서버 오류가 발생했습니다."
            });
        }
        
        if (!user) {
            return res.json({
                authorized: false,
                message: "등록되지 않은 사용자입니다.\n\n📞 관리자에게 문의해주세요:\n010-0000-0000"
            });
        }
        
        // 만료 확인
        const now = new Date();
        const expiryDate = new Date(user.expires_at);
        
        if (now > expiryDate) {
            return res.json({
                authorized: false,
                message: "사용 기간이 만료되었습니다.\n\n📞 관리자에게 연장을 문의해주세요:\n010-0000-0000"
            });
        }
        
        // 인증 성공 - 정보 업데이트
        db.run(`
            UPDATE users 
            SET device_id = ?, last_auth = CURRENT_TIMESTAMP, total_auths = total_auths + 1
            WHERE phone_number = ?
        `, [device_id, phone_number]);
        
        const remainingDays = Math.ceil((expiryDate - now) / (1000 * 60 * 60 * 24));
        
        console.log(`✅ 인증 성공: ${phone_number} (${user.type}, ${remainingDays}일 남음)`);
        
        res.json({
            authorized: true,
            type: user.type,
            expires_at: user.expires_at,
            remaining_days: remainingDays,
            message: `인증 완료! ${user.type === 'premium' ? '프리미엄' : '체험'} 사용자 (${remainingDays}일 남음)`
        });
    });
});

// 통계 수집 API
app.post('/api/auth/stats', (req, res) => {
    const { phone_number, action, details } = req.body;
    
    if (action === 'call_accepted') {
        db.run(`
            UPDATE users 
            SET total_calls = total_calls + 1, 
                accepted_calls = accepted_calls + 1,
                last_active = CURRENT_TIMESTAMP
            WHERE phone_number = ?
        `, [phone_number]);
    } else if (action === 'call_rejected') {
        db.run(`
            UPDATE users 
            SET total_calls = total_calls + 1,
                last_active = CURRENT_TIMESTAMP
            WHERE phone_number = ?
        `, [phone_number]);
    }
    
    res.json({ success: true });
});

// 관리자 - 신규 사용자 등록
app.post('/admin/register', authenticateAdmin, checkPermission('user_register'), (req, res) => {
    const { phone_number, type, days, memo } = req.body;
    
    // 일수 확인
    if (!days || days <= 0) {
        return res.status(400).json({
            success: false,
            message: '올바른 일수를 입력해주세요.'
        });
    }
    
    const expiryDate = moment().add(days, 'days').format('YYYY-MM-DD HH:mm:ss');
    
    db.run(`
        INSERT INTO users (phone_number, type, expires_at, memo)
        VALUES (?, ?, ?, ?)
    `, [phone_number, type || 'trial', expiryDate, memo || ''], function(err) {
        if (err) {
            if (err.code === 'SQLITE_CONSTRAINT') {
                return res.status(400).json({
                    success: false,
                    message: '이미 등록된 휴대폰 번호입니다.'
                });
            }
            return res.status(500).json({
                success: false,
                message: '등록 중 오류가 발생했습니다.'
            });
        }
        
        const typeNames = {
            'trial3': '무료체험',
            'trial7': '체험',
            'month1': '1달',
            'month3': '3달', 
            'month6': '6달',
            'year1': '1년',
            'custom': '사용자정의'
        };
        
        console.log(`📱 신규 사용자 등록: ${phone_number} (${typeNames[type] || type}, ${days}일)`);
        
        res.json({
            success: true,
            message: '사용자가 성공적으로 등록되었습니다.',
            user: {
                phone_number,
                type: type || 'trial',
                expires_at: expiryDate,
                remaining_days: days
            }
        });
    });
});

// 관리자 - 사용자 목록 조회
app.get('/admin/users', authenticateAdmin, (req, res) => {
    const { status, type, search } = req.query;
    
    let query = 'SELECT * FROM users';
    let params = [];
    let conditions = [];
    
    if (status) {
        conditions.push('status = ?');
        params.push(status);
    }
    if (type) {
        conditions.push('type = ?');
        params.push(type);
    }
    if (search) {
        conditions.push('(phone_number LIKE ? OR memo LIKE ?)');
        params.push(`%${search}%`, `%${search}%`);
    }
    
    if (conditions.length > 0) {
        query += ' WHERE ' + conditions.join(' AND ');
    }
    query += ' ORDER BY registered_at DESC LIMIT 100';
    
    db.all(query, params, (err, users) => {
        if (err) {
            return res.status(500).json({ error: '서버 오류' });
        }
        
        // 통계 계산
        db.get(`
            SELECT 
                COUNT(*) as total,
                COUNT(CASE WHEN status = 'active' AND datetime(expires_at) > datetime('now') THEN 1 END) as active,
                COUNT(CASE WHEN datetime(expires_at) < datetime('now') THEN 1 END) as expired,
                COUNT(CASE WHEN status = 'blocked' THEN 1 END) as blocked
            FROM users
        `, (err, summary) => {
            const processedUsers = users.map(user => {
                const now = new Date();
                const expiryDate = new Date(user.expires_at);
                const remainingDays = Math.max(0, Math.ceil((expiryDate - now) / (1000 * 60 * 60 * 24)));
                
                return {
                    ...user,
                    registered_at: moment(user.registered_at).format('MM-DD'),
                    expires_at: moment(user.expires_at).format('MM-DD'),
                    remaining_days: remainingDays,
                    last_auth: user.last_auth ? moment(user.last_auth).fromNow() : '없음'
                };
            });
            
            res.json({
                users: processedUsers,
                summary: summary || { total: 0, active: 0, expired: 0, blocked: 0 }
            });
        });
    });
});

// 관리자 - 사용자 기간 연장
app.post('/admin/extend', authenticateAdmin, (req, res) => {
    const { user_id, extend_days } = req.body;
    
    db.get('SELECT * FROM users WHERE id = ?', [user_id], (err, user) => {
        if (err || !user) {
            return res.status(404).json({ error: '사용자를 찾을 수 없습니다.' });
        }
        
        const currentExpiry = new Date(user.expires_at);
        const newExpiry = moment(currentExpiry).add(extend_days, 'days').format('YYYY-MM-DD HH:mm:ss');
        
        db.run(`
            UPDATE users SET expires_at = ? WHERE id = ?
        `, [newExpiry, user_id], (err) => {
            if (err) {
                return res.status(500).json({ error: '서버 오류' });
            }
            
            console.log(`⏰ 기간 연장: ${user.phone_number} → ${moment(newExpiry).format('YYYY-MM-DD')}`);
            
            res.json({
                success: true,
                message: `${extend_days}일 연장되었습니다.`,
                new_expiry: newExpiry
            });
        });
    });
});

// 관리자 - 사용자 차단/해제
app.post('/admin/block', authenticateAdmin, (req, res) => {
    const { user_id, block } = req.body;
    const newStatus = block ? 'blocked' : 'active';
    
    db.run(`
        UPDATE users SET status = ? WHERE id = ?
    `, [newStatus, user_id], function(err) {
        if (err) {
            return res.status(500).json({ error: '서버 오류' });
        }
        
        if (this.changes === 0) {
            return res.status(404).json({ error: '사용자를 찾을 수 없습니다.' });
        }
        
        console.log(`🚫 사용자 ${block ? '차단' : '차단해제'}: ID ${user_id}`);
        
        res.json({
            success: true,
            message: `사용자가 ${block ? '차단' : '차단해제'}되었습니다.`
        });
    });
});

// 관리자 - 사용자 삭제 (총 책임자만 가능)
app.delete('/admin/user/:id', authenticateAdmin, checkPermission('user_delete'), (req, res) => {
    const { id } = req.params;
    
    db.run('DELETE FROM users WHERE id = ?', [id], function(err) {
        if (err) {
            return res.status(500).json({ error: '서버 오류' });
        }
        
        if (this.changes === 0) {
            return res.status(404).json({ error: '사용자를 찾을 수 없습니다.' });
        }
        
        console.log(`🗑️ 사용자 삭제: ID ${id}`);
        
        res.json({
            success: true,
            message: '사용자가 삭제되었습니다.'
        });
    });
});

// 관리자 - 통계 API
app.get('/admin/statistics', authenticateAdmin, (req, res) => {
    // 기본 사용자 통계
    db.get(`
        SELECT 
            COUNT(*) as total,
            COUNT(CASE WHEN status = 'active' AND datetime(expires_at) > datetime('now') THEN 1 END) as active,
            COUNT(CASE WHEN datetime(expires_at) < datetime('now') THEN 1 END) as expired,
            COUNT(CASE WHEN status = 'blocked' THEN 1 END) as blocked
        FROM users
    `, (err, summary) => {
        if (err) {
            return res.status(500).json({ error: '통계 조회 오류' });
        }
        
        // 콜 통계 계산
        db.get(`
            SELECT 
                SUM(total_calls) as total_calls,
                SUM(accepted_calls) as accepted_calls
            FROM users
        `, (err, callStats) => {
            
            // 유형별 분포
            db.all(`
                SELECT type, COUNT(*) as count
                FROM users 
                GROUP BY type
                ORDER BY count DESC
            `, (err, typeDistribution) => {
                
                res.json({
                    summary: summary || { total: 0, active: 0, expired: 0, blocked: 0 },
                    call_stats: callStats || { total_calls: 0, accepted_calls: 0 },
                    type_distribution: typeDistribution || []
                });
            });
        });
    });
});

// 관리자 - 활동 로그 API
app.get('/admin/activity', authenticateAdmin, (req, res) => {
    // 최근 활동 로그 (실제로는 별도 activity_log 테이블 사용 권장)
    db.all(`
        SELECT 
            phone_number,
            last_auth,
            total_auths,
            type,
            status,
            registered_at
        FROM users 
        WHERE last_auth IS NOT NULL 
        ORDER BY last_auth DESC 
        LIMIT 50
    `, (err, activities) => {
        if (err) {
            return res.status(500).json({ error: '활동 로그 조회 오류' });
        }
        
        // 활동 로그 포맷팅
        const formattedActivities = activities.map(activity => ({
            timestamp: activity.last_auth,
            type: 'auth',
            message: `${activity.phone_number} 사용자 인증 (${getTypeDisplayName(activity.type)})`
        }));
        
        res.json({
            activities: formattedActivities
        });
    });
});

// 유형 표시명 헬퍼 함수
function getTypeDisplayName(type) {
    const typeNames = {
        'trial3': '무료체험',
        'trial7': '체험',
        'month1': '1달',
        'month3': '3달',
        'month6': '6달',
        'year1': '1년',
        'custom': '사용자정의'
    };
    return typeNames[type] || type;
}

// 기본 라우트
app.get('/', (req, res) => {
    res.json({
        message: '🎵 Media Enhanced Auth Server (SQLite)',
        version: '1.0.0',
        status: 'running',
        database: 'SQLite',
        endpoints: {
            auth: '/api/auth/verify',
            admin: '/admin/users',
            dashboard: '/dashboard.html'
        }
    });
});

// 서버 상태 확인
app.get('/health', (req, res) => {
    res.json({
        status: 'healthy',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        database: 'SQLite'
    });
});

// 에러 핸들러
app.use((err, req, res, next) => {
    console.error('서버 에러:', err.stack);
    res.status(500).json({
        error: '서버 내부 오류',
        message: '관리자에게 문의해주세요.'
    });
});

// 서버 시작
app.listen(PORT, () => {
    console.log(`🚀 Media Enhanced Auth Server (SQLite) 시작됨`);
    console.log(`📡 포트: ${PORT}`);
    console.log(`🌐 관리자 페이지: http://localhost:${PORT}/dashboard.html`);
    console.log(`📊 API 엔드포인트: http://localhost:${PORT}/api`);
    console.log(`💾 데이터베이스: ${dbPath}`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('🛑 서버 종료 중...');
    db.close((err) => {
        if (err) {
            console.error('DB 종료 오류:', err.message);
        } else {
            console.log('📊 SQLite 연결 종료됨');
        }
        process.exit(0);
    });
});