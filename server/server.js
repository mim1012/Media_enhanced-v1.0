const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const bodyParser = require('body-parser');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// 미들웨어
app.use(cors({
    origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000']
}));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// 정적 파일 서빙 (관리자 페이지용)
app.use(express.static('public'));

// MongoDB 연결
mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/media_enhanced', {
    useNewUrlParser: true,
    useUnifiedTopology: true
});

mongoose.connection.on('connected', () => {
    console.log('📊 MongoDB 연결 성공');
});

mongoose.connection.on('error', (err) => {
    console.error('❌ MongoDB 연결 실패:', err);
});

// 라우트
const authRoutes = require('./routes/auth');
const adminRoutes = require('./routes/admin');

app.use('/api/auth', authRoutes);
app.use('/admin', adminRoutes);

// 기본 라우트
app.get('/', (req, res) => {
    res.json({
        message: '🎵 Media Enhanced Auth Server',
        version: '1.0.0',
        status: 'running',
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
        uptime: process.uptime()
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

// 404 핸들러
app.use((req, res) => {
    res.status(404).json({
        error: '페이지를 찾을 수 없습니다.',
        path: req.path
    });
});

// 서버 시작
app.listen(PORT, () => {
    console.log(`🚀 Media Enhanced Auth Server 시작됨`);
    console.log(`📡 포트: ${PORT}`);
    console.log(`🌐 관리자 페이지: http://localhost:${PORT}/dashboard.html`);
    console.log(`📊 API 엔드포인트: http://localhost:${PORT}/api`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('🛑 서버 종료 중...');
    mongoose.connection.close(() => {
        console.log('📊 MongoDB 연결 종료됨');
        process.exit(0);
    });
});