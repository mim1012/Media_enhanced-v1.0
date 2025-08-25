const { Pool } = require('pg');

// Supabase PostgreSQL 연결 설정
const pool = new Pool({
    connectionString: process.env.SUPABASE_DATABASE_URL || process.env.DATABASE_URL,
    ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false
});

// 테이블 초기화 SQL
const initSQL = `
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    device_id VARCHAR(255),
    type VARCHAR(20) DEFAULT 'trial' CHECK (type IN ('trial', 'premium')),
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'blocked')),
    memo TEXT DEFAULT '',
    last_auth TIMESTAMP,
    total_auths INTEGER DEFAULT 0,
    total_calls INTEGER DEFAULT 0,
    accepted_calls INTEGER DEFAULT 0,
    last_active TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_phone_number ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_users_expires_at ON users(expires_at);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
`;

// 데이터베이스 초기화
async function initializeDatabase() {
    try {
        await pool.query(initSQL);
        console.log('📊 PostgreSQL 테이블 초기화 완료');
    } catch (error) {
        console.error('❌ PostgreSQL 테이블 초기화 실패:', error);
    }
}

// User 클래스 정의
class UserPostgres {
    constructor(userData) {
        Object.assign(this, userData);
    }

    // 남은 일수 계산
    get remaining_days() {
        if (!this.expires_at) return 0;
        const diff = new Date(this.expires_at).getTime() - Date.now();
        return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
    }

    // 사용자 저장
    async save() {
        const updateSQL = `
            UPDATE users SET 
                device_id = $1, 
                last_auth = $2, 
                total_auths = $3,
                updated_at = CURRENT_TIMESTAMP
            WHERE phone_number = $4 
            RETURNING *
        `;
        
        const result = await pool.query(updateSQL, [
            this.device_id,
            this.last_auth,
            this.total_auths,
            this.phone_number
        ]);
        
        if (result.rows.length > 0) {
            Object.assign(this, result.rows[0]);
        }
        return this;
    }

    // 단일 사용자 조회
    static async findOne(condition) {
        if (condition.phone_number) {
            const result = await pool.query(
                'SELECT * FROM users WHERE phone_number = $1',
                [condition.phone_number]
            );
            return result.rows.length > 0 ? new UserPostgres(result.rows[0]) : null;
        }
        return null;
    }

    // ID로 사용자 조회
    static async findById(id) {
        const result = await pool.query('SELECT * FROM users WHERE id = $1', [id]);
        return result.rows.length > 0 ? new UserPostgres(result.rows[0]) : null;
    }

    // ID로 사용자 삭제
    static async findByIdAndDelete(id) {
        const result = await pool.query(
            'DELETE FROM users WHERE id = $1 RETURNING *',
            [id]
        );
        return result.rows.length > 0 ? new UserPostgres(result.rows[0]) : null;
    }

    // 새 사용자 생성
    static async create(userData) {
        const insertSQL = `
            INSERT INTO users (phone_number, type, expires_at, memo, status)
            VALUES ($1, $2, $3, $4, $5)
            RETURNING *
        `;
        
        const result = await pool.query(insertSQL, [
            userData.phone_number,
            userData.type,
            userData.expires_at,
            userData.memo,
            userData.status
        ]);
        
        return new UserPostgres(result.rows[0]);
    }

    // 사용자 목록 조회
    static async find(filter = {}, options = {}) {
        let whereClause = 'WHERE 1=1';
        const params = [];
        let paramCount = 0;

        if (filter.status) {
            whereClause += ` AND status = $${++paramCount}`;
            params.push(filter.status);
        }

        if (filter.type) {
            whereClause += ` AND type = $${++paramCount}`;
            params.push(filter.type);
        }

        if (filter.$or) {
            const searchTerm = filter.$or[0].phone_number?.$regex;
            if (searchTerm) {
                whereClause += ` AND (phone_number ILIKE $${++paramCount} OR memo ILIKE $${++paramCount})`;
                params.push(`%${searchTerm}%`, `%${searchTerm}%`);
            }
        }

        if (filter.expires_at?.$gt) {
            whereClause += ` AND expires_at > $${++paramCount}`;
            params.push(filter.expires_at.$gt);
        }

        if (filter.expires_at?.$lt) {
            whereClause += ` AND expires_at < $${++paramCount}`;
            params.push(filter.expires_at.$lt);
        }

        const orderClause = options.sort?.registered_at === -1 ? 'ORDER BY registered_at DESC' : '';
        const limitClause = options.limit ? `LIMIT ${options.limit}` : '';

        const sql = `SELECT * FROM users ${whereClause} ${orderClause} ${limitClause}`;
        const result = await pool.query(sql, params);
        
        return result.rows.map(row => new UserPostgres(row));
    }

    // 문서 개수 조회
    static async countDocuments(filter = {}) {
        let whereClause = 'WHERE 1=1';
        const params = [];
        let paramCount = 0;

        if (filter.status) {
            whereClause += ` AND status = $${++paramCount}`;
            params.push(filter.status);
        }

        if (filter.expires_at?.$gt) {
            whereClause += ` AND expires_at > $${++paramCount}`;
            params.push(filter.expires_at.$gt);
        }

        if (filter.expires_at?.$lt) {
            whereClause += ` AND expires_at < $${++paramCount}`;
            params.push(filter.expires_at.$lt);
        }

        const sql = `SELECT COUNT(*) as count FROM users ${whereClause}`;
        const result = await pool.query(sql, params);
        
        return parseInt(result.rows[0].count);
    }
}

// 연결 상태 확인용 프로퍼티
UserPostgres.db = { readyState: 1 }; // PostgreSQL은 항상 연결 상태로 가정

module.exports = { UserPostgres, initializeDatabase, pool };