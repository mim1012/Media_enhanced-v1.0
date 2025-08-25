const mongoose = require('mongoose');

/**
 * 등록된 사용자 스키마
 */
const UserSchema = new mongoose.Schema({
    // 기본 정보
    phone_number: {
        type: String,
        required: true,
        unique: true,
        match: /^010-\d{4}-\d{4}$/
    },
    device_id: {
        type: String,
        default: null
    },
    
    // 구독 정보
    type: {
        type: String,
        enum: ['trial', 'premium'],
        default: 'trial'
    },
    registered_at: {
        type: Date,
        default: Date.now
    },
    expires_at: {
        type: Date,
        required: true
    },
    
    // 상태 관리
    status: {
        type: String,
        enum: ['active', 'blocked'],
        default: 'active'
    },
    memo: {
        type: String,
        default: ''
    },
    
    // 사용 통계
    last_auth: {
        type: Date,
        default: null
    },
    total_auths: {
        type: Number,
        default: 0
    },
    
    // 활동 통계
    stats: {
        total_calls: { type: Number, default: 0 },
        accepted_calls: { type: Number, default: 0 },
        last_active: { type: Date, default: null },
        regions: [String],
        avg_distance: { type: Number, default: 0 }
    }
}, {
    timestamps: true
});

// 인덱스 생성
UserSchema.index({ phone_number: 1 });
UserSchema.index({ expires_at: 1 });
UserSchema.index({ status: 1 });

// 가상 필드 - 남은 일수
UserSchema.virtual('remaining_days').get(function() {
    if (!this.expires_at) return 0;
    const diff = this.expires_at.getTime() - Date.now();
    return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
});

// JSON 변환 시 가상 필드 포함
UserSchema.set('toJSON', { virtuals: true });

module.exports = mongoose.model('User', UserSchema);