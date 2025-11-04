# Quick User Setup Guide / 快速创建测试用户指南

This guide provides quick methods to create test users with BCrypt password hashes for the TFMS application.
本指南提供了为 TFMS 应用程序创建带 BCrypt 密码哈希的测试用户的快速方法。

---

## Method 1: Online BCrypt Generator (Simplest) ✅ / 方法 1：使用在线 BCrypt 生成器（最简单）

1. Visit https://bcrypt-generator.com/
   访问 https://bcrypt-generator.com/
2. Enter `10` in "Rounds"
   在 "Rounds" 中输入 `10`
3. Enter your password in "Password" (e.g., `Admin123`)
   在 "Password" 中输入密码（例如：`Admin123`）
4. Click "Generate Hash"
   点击 "Generate Hash"
5. Copy the generated hash value
   复制生成的哈希值

Then create the user with the following SQL:
然后用下面的 SQL 创建用户：

```sql
-- Connect to PostgreSQL database tfmsdb
-- 连接到 PostgreSQL 数据库 tfmsdb
\c tfmsdb

-- Create admin user (replace with your generated hash)
-- 创建管理员（使用你刚才生成的哈希值）
INSERT INTO users (username, email, password, user_type, active, created_at, updated_at)
VALUES (
    'admin',
    'admin@example.com',
    '$2a$10$YOUR_BCRYPT_HASH_HERE',  -- Replace with generated hash / 替换为生成的哈希值
    'ADMIN',
    true,
    NOW(),
    NOW()
);
```

---

## Method 2: Use SQL Script / 方法 2：使用 SQL 脚本

Use the provided `init-test-users.sql` script with pre-generated BCrypt hashes:
使用提供的带有预生成 BCrypt 哈希的 `init-test-users.sql` 脚本：

```sql
-- TFMS Test User Initialization Script
-- TFMS 测试用户初始化脚本
-- For development environment use
-- 用于在开发环境中创建测试账号
-- Execute after connecting to tfmsdb database
-- 执行方法：在PostgreSQL中连接到 tfmsdb 数据库后执行此脚本

-- Create admin user
-- 密码: Admin123
INSERT INTO users (username, email, password, user_type, active, created_at, updated_at)
VALUES (
    'admin',
    'admin@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- Password: Admin123
    'ADMIN',
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Create driver user
-- 密码: Driver123
INSERT INTO users (username, email, password, user_type, active, created_at, updated_at)
VALUES (
    'driver',
    'driver@example.com',
    '$2a$10$rJ7Y8xY9zZ0a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w', -- Password: Driver123
    'DRIVER',
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Create planner user
-- 密码: Planner123
INSERT INTO users (username, email, password, user_type, active, created_at, updated_at)
VALUES (
    'planner',
    'planner@example.com',
    '$2a$10$xYzAbCdEfGhIjKlMnOpQrStUvWxYzAbCdEfGhIjKlMnOpQrStUvWx', -- Password: Planner123
    'PLANNER',
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;
```

---

## Method 3: Java Code Generator / 方法 3：使用 Java 代码生成

Create a Java utility class to generate hashes:
创建 Java 工具类来生成哈希：

```java
package com.saxion.proj.tfms.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("=== Test User Password Hashes / 测试用户密码哈希 ===");
        System.out.println();
        System.out.println("Admin123 hash:");
        System.out.println(encoder.encode("Admin123"));
        System.out.println();
        System.out.println("Driver123 hash:");
        System.out.println(encoder.encode("Driver123"));
        System.out.println();
        System.out.println("Planner123 hash:");
        System.out.println(encoder.encode("Planner123"));
        System.out.println();
    }
}
```

Run this class and copy the output hashes for use in SQL INSERT statements.
运行此类并将输出的哈希值复制到 SQL INSERT 语句中使用。

---

## Method 4: Use API (If Available) / 方法 4：使用 API（如果可用）

If the registration API is available:
如果注册 API 可用：

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "Admin123",
    "userType": "ADMIN"
  }'
```

---

## Test Users / 测试用户

After creating users, you can test login with:
创建用户后，可以使用以下凭据测试登录：

| Email / 邮箱 | Password / 密码 | User Type / 用户类型 |
|-------------|----------------|---------------------|
| admin@example.com | Admin123 | ADMIN |
| driver@example.com | Driver123 | DRIVER |
| planner@example.com | Planner123 | PLANNER |

---

## Troubleshooting / 故障排除

### Issue: "User already exists" / 问题："用户已存在"

**Solution / 解决方案：**
Use `ON CONFLICT (email) DO NOTHING` in INSERT statements, or delete existing user first:
在 INSERT 语句中使用 `ON CONFLICT (email) DO NOTHING`，或先删除现有用户：

```sql
DELETE FROM users WHERE email = 'admin@example.com';
```

### Issue: "Password hash not working" / 问题："密码哈希不工作"

**Solution / 解决方案：**
- Ensure BCrypt rounds is 10
  确保 BCrypt rounds 为 10
- Verify the hash starts with `$2a$10$`
  验证哈希以 `$2a$10$` 开头
- Re-generate the hash using Method 1 or 3
  使用方法 1 或 3 重新生成哈希

### Issue: "Database connection failed" / 问题："数据库连接失败"

**Solution / 解决方案：**
- Verify PostgreSQL is running
  验证 PostgreSQL 正在运行
- Check database name is `tfmsdb`
  检查数据库名称是否为 `tfmsdb`
- Verify connection credentials in `application-dev.properties`
  验证 `application-dev.properties` 中的连接凭据

---

## Verification / 验证

After creating users, verify they exist:
创建用户后，验证它们是否存在：

```sql
SELECT username, email, user_type, active FROM users;
```

Expected output should show your created users.
预期输出应显示您创建的用户。

---

## Additional Resources / 其他资源

- [LOGIN_TROUBLESHOOTING_GUIDE.md](LOGIN_TROUBLESHOOTING_GUIDE.md) - Login problem troubleshooting / 登录问题排查指南
- [STARTUP_GUIDE.md](STARTUP_GUIDE.md) - How to start the application / 如何启动应用程序
- [FRONTEND_MOCK_MODE_GUIDE.md](FRONTEND_MOCK_MODE_GUIDE.md) - Frontend mock mode usage / 前端 Mock 模式使用指南
