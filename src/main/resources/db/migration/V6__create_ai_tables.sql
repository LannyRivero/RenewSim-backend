-- =====================================================
-- V6: Create AI chat tables
-- =====================================================

-- =====================================================
-- CHAT_SESSIONS TABLE
-- =====================================================
CREATE TABLE chat_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'Unique session identifier (UUID)',
    user_id BIGINT NOT NULL COMMENT 'User who owns the session',
    simulation_id BIGINT COMMENT 'Associated simulation (optional)',
    title VARCHAR(255) COMMENT 'Session title/topic',
    started_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_message_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Session is active',
    
    CONSTRAINT fk_chat_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_sessions_simulation FOREIGN KEY (simulation_id) REFERENCES simulations(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI chat sessions';

-- Indexes for session queries
CREATE INDEX idx_chat_sessions_session_id ON chat_sessions(session_id);
CREATE INDEX idx_chat_sessions_user ON chat_sessions(user_id, is_active);
CREATE INDEX idx_chat_sessions_simulation ON chat_sessions(simulation_id);
CREATE INDEX idx_chat_sessions_last_message ON chat_sessions(last_message_at);

-- =====================================================
-- CHAT_MESSAGES TABLE
-- =====================================================
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL COMMENT 'Parent chat session',
    role ENUM('USER', 'ASSISTANT', 'SYSTEM') NOT NULL COMMENT 'Message sender role',
    content TEXT NOT NULL COMMENT 'Message content',
    metadata JSON COMMENT 'Additional metadata (tokens, model, confidence, etc.)',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI chat messages';

-- Index for message retrieval by session
CREATE INDEX idx_chat_messages_session_id ON chat_messages(session_id, created_at);
CREATE INDEX idx_chat_messages_role ON chat_messages(role);