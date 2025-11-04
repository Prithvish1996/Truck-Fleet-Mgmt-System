# Startup Guide / 启动指南

This guide provides detailed instructions on how to run both the frontend and backend of the TFMS application in development and production modes.
本指南提供了如何在开发和生产模式下运行 TFMS 应用程序的前端和后端的详细说明。

---

## Prerequisites / 前置条件

Before running the application, ensure you have the following installed:
在运行应用程序之前，请确保已安装以下内容：

- **Java JDK 17+**
- **Maven 3.8+** (or use the included Maven Wrapper `mvnw`)
- **PostgreSQL 15**
- **Node.js 16+** and **npm** (for frontend)
- **Git Bash** (for running shell scripts on Windows)

---

## Database Setup / 数据库设置

### Step 1: Create Database / 步骤1：创建数据库

1. Connect to PostgreSQL / 连接到 PostgreSQL
2. Create database and user / 创建数据库和用户：

```sql
CREATE DATABASE tfmsdb;
CREATE USER tfms_user WITH PASSWORD 'tfms_pass';
GRANT ALL PRIVILEGES ON DATABASE tfmsdb TO tfms_user;
```

3. Verify connection / 验证连接：
```bash
psql -U tfms_user -d tfmsdb -h localhost -p 5432
```

### Step 2: Configure Database Connection / 步骤2：配置数据库连接

Edit `tfms-starter/src/main/resources/application-dev.properties`:
编辑 `tfms-starter/src/main/resources/application-dev.properties`：

```properties
# PostgreSQL 15 connection
spring.datasource.url=jdbc:postgresql://localhost:5432/tfmsdb?ssl=false
spring.datasource.username=tfms_user
spring.datasource.password=tfms_pass
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate / JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Dev server
server.port=8080
server.ssl.enabled=false
```

---

## Development Mode / 开发模式

In development mode, the frontend and backend run separately for hot-reloading.
在开发模式下，前端和后端分开运行以实现热重载。

### Method 1: Manual Start (Recommended) / 方法1：手动启动（推荐）

#### Start Backend / 启动后端

**Windows (PowerShell) / Windows (PowerShell)：**

```powershell
# Method 1: Using environment variable (Recommended)
cd tfms-starter
$env:SPRING_PROFILES_ACTIVE="dev"
..\mvnw.cmd spring-boot:run

# Method 2: Using quoted parameter
cd tfms-starter
..\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# Method 3: Using Maven directly
cd tfms-starter
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Windows (Git Bash) / Windows (Git Bash)：**

```bash
cd tfms-starter
export SPRING_PROFILES_ACTIVE=dev
../mvnw spring-boot:run
```

**Linux/Mac / Linux/Mac：**

```bash
cd tfms-starter
export SPRING_PROFILES_ACTIVE=dev
../mvnw spring-boot:run
```

#### Start Frontend / 启动前端

Open a new terminal and run:
打开新终端并运行：

```bash
cd frontend/my-app
npm install  # First time only / 仅首次需要
npm start
```

### Method 2: Using Scripts / 方法2：使用脚本

```bash
# Start both frontend and backend
bash scripts/start-dev.sh
```

---

## Production Mode / 生产模式

In production mode, the frontend is built and integrated into the backend JAR file.
在生产模式下，前端被构建并集成到后端 JAR 文件中。

### Build and Run / 构建并运行

```bash
# Build production JAR
cd tfms-starter
../mvnw clean package -Pprod

# Run production JAR
java -jar target/tfms-1.0.0-dev.jar --spring.profiles.active=prod
```

Or use the script:
或使用脚本：

```bash
bash scripts/start-prod.sh
```

---

## Verification / 验证

### Backend Verification / 后端验证

1. Check backend logs for startup messages / 检查后端日志中的启动消息
2. Visit health check endpoint / 访问健康检查端点：
   - http://localhost:8080/api/test/health
   - http://localhost:8080/actuator/health

### Frontend Verification / 前端验证

1. Open browser to / 在浏览器中打开：
   - Development: http://localhost:3000
   - Production: http://localhost:8080

---

## Access URLs / 访问地址

### Development Mode / 开发模式

- **Backend API**: http://localhost:8080/api
- **Frontend**: http://localhost:3000
- **H2 Console** (if enabled): http://localhost:8080/h2-console
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Actuator**: http://localhost:8080/actuator

### Production Mode / 生产模式

- **Application**: http://localhost:8080 (Frontend + Backend)
- **API Endpoints**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/actuator/health

---

## IntelliJ IDEA Integration / IntelliJ IDEA 集成

### Enable Git Bash Terminal / 启用 Git Bash 终端

1. Open **File → Settings → Tools → Terminal**
2. Set **Shell path** to your Git Bash executable:
   - Example: `C:\Program Files\Git\bin\bash.exe`
3. Click **Apply → OK**
4. Open IntelliJ terminal (`Alt+F12`) to use Git Bash
5. Run scripts like: `bash scripts/start-dev.sh`

### Run Configuration / 运行配置

1. Create new "Spring Boot" run configuration
2. Set **Main class**: `com.saxion.proj.tfms.TfmsApplication`
3. Set **Active profiles**: `dev`
4. Set **Working directory**: `$MODULE_DIR$/tfms-starter`
5. Click **Run**

---

## Troubleshooting / 故障排除

### Issue: Port Already in Use / 问题：端口已被占用

**Solution / 解决方案：**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Issue: Maven Parameter Error in PowerShell / 问题：PowerShell 中的 Maven 参数错误

**Symptoms / 症状：**
```
Unknown lifecycle phase ".run.profiles=dev"
```

**Solution / 解决方案：**
Use environment variable or quoted parameters:
使用环境变量或带引号的参数：

```powershell
# Method 1: Environment variable (Recommended)
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run

# Method 2: Quoted parameter
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### Issue: Frontend Not Loading / 问题：前端无法加载

**Solution / 解决方案：**
1. Check if `npm install` completed successfully
   检查 `npm install` 是否成功完成
2. Clear npm cache and reinstall:
   清除 npm 缓存并重新安装：

```bash
cd frontend/my-app
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
npm start
```

### Issue: Database Connection Failed / 问题：数据库连接失败

**Solution / 解决方案：**
1. Verify PostgreSQL is running
   验证 PostgreSQL 正在运行
2. Check database credentials in `application-dev.properties`
   检查 `application-dev.properties` 中的数据库凭据
3. Verify database exists: `psql -U tfms_user -d tfmsdb`
   验证数据库是否存在：`psql -U tfms_user -d tfmsdb`

---

## Stopping Services / 停止服务

### Stop Development Services / 停止开发服务

```bash
# Stop backend (Ctrl+C in terminal)
# Or kill process:
pkill -f "spring-boot:run"

# Stop frontend (Ctrl+C in terminal)
# Or kill process:
pkill -f "react-scripts"
```

### Stop Production Service / 停止生产服务

```bash
# Kill JAR process
pkill -f "tfms-1.0.0-dev.jar"
```

Or use the script:
或使用脚本：

```bash
bash scripts/stop-all.sh
```

---

## Additional Resources / 其他资源

- [LOGIN_TROUBLESHOOTING_GUIDE.md](LOGIN_TROUBLESHOOTING_GUIDE.md) - Login problem troubleshooting / 登录问题排查指南
- [QUICK_USER_SETUP_GUIDE.md](QUICK_USER_SETUP_GUIDE.md) - Quick guide to create test users / 快速创建测试用户指南
- [FRONTEND_MOCK_MODE_GUIDE.md](FRONTEND_MOCK_MODE_GUIDE.md) - Frontend mock mode usage / 前端 Mock 模式使用指南

---

## Notes / 注意事项

- Development mode allows hot-reloading for frontend changes
  开发模式允许前端更改的热重载
- Backend changes require restart
  后端更改需要重启
- Production mode builds optimized bundles
  生产模式构建优化的包
- Always check logs for detailed error messages
  始终检查日志以获取详细的错误消息
