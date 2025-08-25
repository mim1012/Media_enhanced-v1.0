const sqlite3 = require('sqlite3').verbose();

/**
 * 관리자 권한 시스템
 */
class AdminManager {
    constructor(db) {
        this.db = db;
        this.initializeAdminTable();
    }
    
    // 관리자 테이블 초기화
    initializeAdminTable() {
        this.db.run(`
            CREATE TABLE IF NOT EXISTS admins (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT NOT NULL,  -- 'super', 'manager'
                name TEXT NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                last_login DATETIME,
                active INTEGER DEFAULT 1
            )
        `);
        
        // 기본 관리자 계정 생성 (최초 1회만)
        this.createDefaultAdmins();
    }
    
    // 기본 관리자 계정 생성
    createDefaultAdmins() {
        const defaultAdmins = [
            {
                username: 'super_admin',
                password: 'dev_master_2024!',  // 실제로는 해시 처리 필요
                role: 'super',
                name: '총 책임자 (개발자)'
            },
            {
                username: 'manager1',
                password: 'mgr_2024_01!',
                role: 'manager', 
                name: '관리직원 1'
            },
            {
                username: 'manager2',
                password: 'mgr_2024_02!',
                role: 'manager',
                name: '관리직원 2'
            }
        ];
        
        defaultAdmins.forEach(admin => {
            this.db.run(`
                INSERT OR IGNORE INTO admins (username, password, role, name)
                VALUES (?, ?, ?, ?)
            `, [admin.username, admin.password, admin.role, admin.name]);
        });
        
        console.log('👤 기본 관리자 계정 초기화 완료');
    }
    
    // 관리자 인증
    authenticate(username, password) {
        return new Promise((resolve, reject) => {
            this.db.get(`
                SELECT * FROM admins 
                WHERE username = ? AND password = ? AND active = 1
            `, [username, password], (err, admin) => {
                if (err) {
                    reject(err);
                    return;
                }
                
                if (admin) {
                    // 로그인 시간 업데이트
                    this.db.run(`
                        UPDATE admins SET last_login = CURRENT_TIMESTAMP 
                        WHERE id = ?
                    `, [admin.id]);
                    
                    resolve({
                        id: admin.id,
                        username: admin.username,
                        role: admin.role,
                        name: admin.name,
                        permissions: this.getPermissions(admin.role)
                    });
                } else {
                    resolve(null);
                }
            });
        });
    }
    
    // 권한 정의
    getPermissions(role) {
        const permissions = {
            super: [
                'user_register',    // 사용자 등록
                'user_extend',      // 기간 연장  
                'user_block',       // 차단/해제
                'user_delete',      // 삭제
                'view_statistics',  // 통계 조회
                'admin_manage',     // 관리자 관리
                'system_control'    // 시스템 제어
            ],
            manager: [
                'user_register',    // 사용자 등록
                'user_extend',      // 기간 연장
                'user_block',       // 차단/해제 (삭제 제외)
                'view_statistics'   // 통계 조회
            ]
        };
        
        return permissions[role] || [];
    }
    
    // 권한 확인
    hasPermission(userRole, action) {
        const permissions = this.getPermissions(userRole);
        return permissions.includes(action);
    }
}

module.exports = AdminManager;