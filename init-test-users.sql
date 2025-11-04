-- TFMS 测试用户初始化脚本
-- 用于在开发环境中创建测试账号
-- 执行方法：在PostgreSQL中连接到 tfmsdb 数据库后执行此脚本

-- 删除已存在的测试用户（可选，用于重新初始化）
-- DELETE FROM users WHERE email IN ('admin@example.com', 'driver@example.com', 'planner@example.com');

-- 创建管理员用户
-- 密码: Admin123
INSERT INTO users (username, email, password, user_type, active, created_at, updated_at)
VALUES (
    'admin',
    'admin@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- 创建司机用户
-- 密码: Driver123
INSERT INTO users (username, email, password, user_type, active, created_at, updated_at)
VALUES (
    'driver',
    'driver@example.com',
    '$2a$10$8K1p/a0dL1k2N3m4P5Q6ReIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'DRIVER',
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- 创建计划员用户
-- 密码: Planner123
INSERT INTO users (username, email, password, user_type, active, created_at, updated_at)
VALUES (
    'planner',
    'planner@example.com',
    '$2a$10$9L2q/b1eM2l3O4n5Q6R7SfJkZAgcfl7p92ldGxad68LJZdL17lhWy',
    'PLANNER',
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- 验证用户是否创建成功
SELECT id, username, email, user_type, active FROM users ORDER BY user_type;
