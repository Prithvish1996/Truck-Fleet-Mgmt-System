# Frontend Mock Mode Usage Guide / 前端 Mock 模式使用指南

## ✅ Feature Description / 功能说明

The frontend now supports **Mock Mode**, which allows you to completely bypass the backend for development. Mock mode will:
前端现在支持 **Mock 模式**，可以完全绕过后端进行开发。Mock 模式会：

- Use local mock data for authentication and business logic
  使用本地的 mock 数据进行认证和业务逻辑
- Automatically switch to mock mode when backend is unavailable
  自动在后端不可用时切换到 mock 模式
- Support login for all test accounts
  支持所有测试账号的登录

---

## 🚀 Quick Start / 快速开始

### Method 1: Automatic Switch (Recommended) / 方法 1：自动切换（推荐）

**Nothing needs to be done!** If the backend is unavailable, the frontend will automatically switch to Mock mode.
**什么都不需要做！** 如果后端不可用，前端会自动切换到 Mock 模式。

1. Make sure the frontend is running: `npm start`
   确保前端正在运行：`npm start`
2. Open `http://localhost:3000`
   打开 `http://localhost:3000`
3. Use test accounts to log in (see below)
   使用测试账号登录（见下方）
4. If the backend is not running, the system will automatically use Mock mode
   如果后端未运行，系统会自动使用 Mock 模式

---

### Method 2: Manual Enable / 方法 2：手动启用

#### Option A: Environment Variable / 选项 A：环境变量

Create a `.env` file in `frontend/my-app/` directory:
在 `frontend/my-app/` 目录中创建 `.env` 文件：

```env
REACT_APP_USE_MOCK_AUTH=true
```

Then restart the frontend:
然后重启前端：

```bash
cd frontend/my-app
npm start
```

#### Option B: Local Storage / 选项 B：本地存储

1. Open browser Developer Tools (F12)
   打开浏览器开发者工具（F12）
2. Go to **Application** tab → **Local Storage** → `http://localhost:3000`
   转到 **Application** 标签页 → **Local Storage** → `http://localhost:3000`
3. Add a new key-value pair:
   添加新的键值对：
   - **Key**: `useMockAuth`
   - **Value**: `true`
4. Refresh the page
   刷新页面

---

## 🔐 Test Accounts / 测试账号

The following test accounts are available in Mock mode:
以下测试账号在 Mock 模式下可用：

| Email / 邮箱 | Password / 密码 | User Type / 用户类型 | Username / 用户名 |
|-------------|----------------|---------------------|------------------|
| admin@example.com | Admin123 | ADMIN | admin |
| driver@example.com | Driver123 | DRIVER | driver |
| planner@example.com | Planner123 | PLANNER | planner |
| driver@tfms.com | Driver123 | DRIVER | driver |
| test@example.com | Driver123 | DRIVER | test |

---

## 🔄 How It Works / 工作原理

### Automatic Detection / 自动检测

The frontend will attempt to connect to the backend API. If the connection fails, it automatically switches to Mock mode:
前端将尝试连接到后端 API。如果连接失败，它会自动切换到 Mock 模式：

1. Frontend tries to call backend API
   前端尝试调用后端 API
2. If backend is unavailable (network error, timeout, etc.)
   如果后端不可用（网络错误、超时等）
3. Frontend automatically uses Mock authentication
   前端自动使用 Mock 认证
4. Login succeeds with mock data
   使用 mock 数据登录成功

### Mock Authentication Flow / Mock 认证流程

```
User enters credentials
    ↓
Frontend checks if backend is available
    ↓
If unavailable → Use Mock Login
    ↓
Validate against mock user database
    ↓
Generate mock JWT token
    ↓
Store token and user data
    ↓
Redirect to dashboard
```

---

## 🛠️ Configuration / 配置

### Enable/Disable Mock Mode / 启用/禁用 Mock 模式

**In code** (`frontend/my-app/src/services/authService.ts`):
**在代码中** (`frontend/my-app/src/services/authService.ts`)：

```typescript
// Check environment variable, then local storage
const USE_MOCK_AUTH = 
  process.env.REACT_APP_USE_MOCK_AUTH === 'true' || 
  localStorage.getItem('useMockAuth') === 'true';
```

**To disable**:
**要禁用**：
- Remove `.env` file or set `REACT_APP_USE_MOCK_AUTH=false`
  删除 `.env` 文件或设置 `REACT_APP_USE_MOCK_AUTH=false`
- Remove `useMockAuth` from Local Storage
  从 Local Storage 中删除 `useMockAuth`

---

## 📝 Mock Data / Mock 数据

### Mock Users / Mock 用户

Mock users are defined in `authService.ts`:
Mock 用户定义在 `authService.ts` 中：

```typescript
const mockUsers: { [key: string]: { password: string; userType: string; username: string } } = {
  'admin@example.com': {
    password: 'Admin123',
    userType: 'ADMIN',
    username: 'admin'
  },
  // ... more users
};
```

### Mock Token Generation / Mock Token 生成

Mock tokens are simple base64 encoded objects:
Mock tokens 是简单的 base64 编码对象：

```typescript
const mockToken = btoa(JSON.stringify({
  email: credentials.email,
  userType: user.userType,
  exp: Math.floor(Date.now() / 1000) + 86400 // 24 hours
}));
```

**Note**: Mock tokens are not real JWT tokens and cannot be validated by the backend.
**注意**：Mock tokens 不是真正的 JWT tokens，无法被后端验证。

---

## ✅ Verification / 验证

### Check if Mock Mode is Active / 检查 Mock 模式是否激活

1. Open browser Developer Tools (F12)
   打开浏览器开发者工具（F12）
2. Check **Console** tab
   检查 **Console** 标签页
3. Look for messages like:
   查找如下消息：
   ```
   Using mock authentication
   Login successful (Mock Mode)
   ```

### Verify Login / 验证登录

1. Use a test account to log in
   使用测试账号登录
2. Check if you're redirected to the dashboard
   检查是否被重定向到仪表板
3. Check if user role is correct in the dashboard
   检查仪表板中的用户角色是否正确

---

## 🔍 Troubleshooting / 故障排除

### Issue: Mock Mode Not Working / 问题：Mock 模式不工作

**Solution / 解决方案：**
1. Check if `USE_MOCK_AUTH` is set correctly
   检查 `USE_MOCK_AUTH` 是否正确设置
2. Verify Local Storage has `useMockAuth=true`
   验证 Local Storage 中有 `useMockAuth=true`
3. Check browser console for errors
   检查浏览器控制台中的错误
4. Ensure backend is actually unavailable (not just slow)
   确保后端确实不可用（不只是慢）

### Issue: Login Always Fails / 问题：登录总是失败

**Solution / 解决方案：**
1. Verify you're using the correct test account credentials
   验证您使用的是正确的测试账号凭据
2. Check mock user data in `authService.ts`
   检查 `authService.ts` 中的 mock 用户数据
3. Clear browser cache and Local Storage
   清除浏览器缓存和 Local Storage

### Issue: Cannot Disable Mock Mode / 问题：无法禁用 Mock 模式

**Solution / 解决方案：**
1. Remove `.env` file
   删除 `.env` 文件
2. Clear Local Storage: `localStorage.removeItem('useMockAuth')`
   清除 Local Storage：`localStorage.removeItem('useMockAuth')`
3. Restart frontend server
   重启前端服务器

---

## 🎯 Use Cases / 使用场景

### Scenario 1: Backend Development Not Started / 场景 1：后端开发未开始

**Use Mock mode** to develop frontend features independently.
**使用 Mock 模式**独立开发前端功能。

### Scenario 2: Backend API Changes / 场景 2：后端 API 变更

**Use Mock mode** to continue frontend work while backend is being refactored.
**使用 Mock 模式**在后端重构期间继续前端工作。

### Scenario 3: Offline Development / 场景 3：离线开发

**Use Mock mode** when you don't have internet or backend access.
**使用 Mock 模式**当您没有互联网或后端访问权限时。

---

## 📚 Additional Resources / 其他资源

- [LOGIN_TROUBLESHOOTING_GUIDE.md](LOGIN_TROUBLESHOOTING_GUIDE.md) - Login problem troubleshooting / 登录问题排查指南
- [QUICK_USER_SETUP_GUIDE.md](QUICK_USER_SETUP_GUIDE.md) - Quick guide to create test users / 快速创建测试用户指南
- [STARTUP_GUIDE.md](STARTUP_GUIDE.md) - How to start frontend and backend / 如何启动前后端

---

## ⚠️ Important Notes / 重要注意事项

1. **Mock mode is for development only**
   **Mock 模式仅用于开发**
   - Never use in production
     永远不要在生产环境使用
   - Mock tokens are not secure
     Mock tokens 不安全

2. **Mock data is limited**
   **Mock 数据是有限的**
   - Only authentication is mocked
     只有认证被 mock
   - Other API calls may still need backend
     其他 API 调用可能仍需要后端

3. **Backend integration**
   **后端集成**
   - When backend is ready, disable Mock mode
     当后端准备就绪时，禁用 Mock 模式
   - Test with real backend before deployment
     部署前使用真实后端测试
