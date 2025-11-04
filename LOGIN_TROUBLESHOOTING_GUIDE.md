# Login Troubleshooting Guide / 登录问题排查指南

> 💡 **Quick Start / 快速开始**：If you are setting up for the first time, please check [QUICK_USER_SETUP_GUIDE.md](QUICK_USER_SETUP_GUIDE.md) to create test accounts.
> 如果你是第一次设置，请先查看 [QUICK_USER_SETUP_GUIDE.md](QUICK_USER_SETUP_GUIDE.md) 来创建测试账号。

## 🔍 Problem Diagnosis / 问题诊断

When you can enter your account and password at the login interface at `http://localhost:3000` but cannot log in, the possible causes are:
当你能在 `http://localhost:3000` 登录界面输入账号密码后无法登录，可能是以下原因：

### 1. Backend Status Check / 检查后端状态

**Check if the backend is running / 检查后端是否在运行：**

```bash
# Windows PowerShell
curl http://localhost:8080/api/test/health

# Or visit in browser
http://localhost:8080/api/test/health
```

**Expected Response / 预期响应：**
```json
{
  "status": "UP",
  "service": "tfms-starter"
}
```

**If the backend is not running / 如果后端未运行：**
- Start the backend using the methods in [STARTUP_GUIDE.md](STARTUP_GUIDE.md)
- 使用 [STARTUP_GUIDE.md](STARTUP_GUIDE.md) 中的方法启动后端
- Or use mock mode as described in [FRONTEND_MOCK_MODE_GUIDE.md](FRONTEND_MOCK_MODE_GUIDE.md)
- 或使用 [FRONTEND_MOCK_MODE_GUIDE.md](FRONTEND_MOCK_MODE_GUIDE.md) 中描述的 mock 模式

---

### 2. CORS Configuration / CORS 配置

**Symptoms / 症状：**
- Browser console shows CORS errors / 浏览器控制台显示 CORS 错误
- Network requests fail with "CORS policy" errors / 网络请求失败并显示 "CORS policy" 错误

**Solution / 解决方案：**
- Ensure the backend is running in `dev` profile / 确保后端以 `dev` 配置文件运行
- Check that `DevCorsConfiguration.java` is properly configured / 检查 `DevCorsConfiguration.java` 是否正确配置
- Verify `application-dev.properties` has correct CORS settings / 验证 `application-dev.properties` 有正确的 CORS 设置

---

### 3. Missing User Data / 缺少用户数据

**Symptoms / 症状：**
- Login always fails / 登录总是失败
- Backend returns "User not found" or "Invalid credentials" / 后端返回"用户未找到"或"凭据无效"

**Solution / 解决方案：**
Follow the guide in [QUICK_USER_SETUP_GUIDE.md](QUICK_USER_SETUP_GUIDE.md) to create test users.
按照 [QUICK_USER_SETUP_GUIDE.md](QUICK_USER_SETUP_GUIDE.md) 中的指南创建测试用户。

**Test Users / 测试用户：**
- Email: `admin@example.com`, Password: `Admin123`
- Email: `driver@example.com`, Password: `Driver123`
- Email: `planner@example.com`, Password: `Planner123`

---

### 4. Browser Console Errors / 浏览器控制台错误

**Steps / 步骤：**

1. Open browser Developer Tools (F12) / 打开浏览器开发者工具（F12）
2. Check the Console tab / 检查控制台标签页
3. Look for error messages / 查找错误消息

**Common Errors / 常见错误：**

- **Network Error / 网络错误**
  - Backend might be down / 后端可能已关闭
  - Check backend status / 检查后端状态

- **401 Unauthorized / 401 未授权**
  - Wrong credentials / 凭据错误
  - Check username and password / 检查用户名和密码

- **CORS Error / CORS 错误**
  - See CORS Configuration section above / 参见上面的 CORS 配置部分

---

### 5. API Testing / API 测试

**Test login API directly / 直接测试登录 API：**

```bash
# Windows PowerShell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"admin@example.com\",\"password\":\"Admin123\"}'
```

**Expected Response / 预期响应：**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "userType": "ADMIN"
  }
}
```

**If API test fails / 如果 API 测试失败：**
- Check if user exists in database / 检查数据库中是否存在用户
- Verify password hash is correct / 验证密码哈希是否正确
- Check backend logs for detailed errors / 检查后端日志以获取详细错误

---

## 🔧 Quick Fixes / 快速修复

### Fix 1: Create Test Users / 修复1：创建测试用户

```bash
# Connect to PostgreSQL
psql -U tfms_user -d tfmsdb

# Run SQL (see QUICK_USER_SETUP_GUIDE.md for complete script)
INSERT INTO users (username, email, password, user_type, active, created_at, updated_at)
VALUES (
  'admin',
  'admin@example.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'ADMIN',
  true,
  NOW(),
  NOW()
);
```

### Fix 2: Enable Mock Mode / 修复2：启用 Mock 模式

If the backend is not available, enable mock mode in the frontend:
如果后端不可用，在前端启用 mock 模式：

See [FRONTEND_MOCK_MODE_GUIDE.md](FRONTEND_MOCK_MODE_GUIDE.md) for details.
详见 [FRONTEND_MOCK_MODE_GUIDE.md](FRONTEND_MOCK_MODE_GUIDE.md)。

---

## 📝 Additional Resources / 其他资源

- [QUICK_USER_SETUP_GUIDE.md](QUICK_USER_SETUP_GUIDE.md) - Quick guide to create test users / 快速创建测试用户指南
- [STARTUP_GUIDE.md](STARTUP_GUIDE.md) - How to start frontend and backend / 如何启动前后端
- [FRONTEND_MOCK_MODE_GUIDE.md](FRONTEND_MOCK_MODE_GUIDE.md) - Frontend mock mode usage / 前端 Mock 模式使用指南

---

## ❓ Still Having Issues? / 仍然有问题？

1. Check backend logs: `tfms-starter/backend.log`
   检查后端日志：`tfms-starter/backend.log`
2. Check browser console for errors
   检查浏览器控制台的错误
3. Verify database connection and user data
   验证数据库连接和用户数据
4. Try mock mode for frontend development
   尝试使用 mock 模式进行前端开发
