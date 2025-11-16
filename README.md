## Docker Deployment

### Quick Start with Docker Compose

The easiest way to run the full application (frontend + backend + database) is using Docker Compose:

#### Start the Application
```bash
docker-compose up -d
```

This will:
- Start PostgreSQL database
- Build the TFMS application (frontend + backend)
- Start the application
- Wait for database to be ready before starting the app

#### Access the Application
- **Application**: https://localhost:8443
- **API Endpoints**: https://localhost:8443/api
- **Health Check**: https://localhost:8443/actuator/health

> **Note**: The application uses HTTPS with a self-signed certificate. Your browser will show a security warning - this is normal for development. Click "Advanced" and "Proceed to localhost" to continue.

#### View Logs
```bash
# All services
docker-compose logs -f

# Application only
docker-compose logs -f tfms-app

# Database only
docker-compose logs -f postgres
```

#### Stop Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clears database data)
docker-compose down -v
```

#### Rebuild After Code Changes
```bash
docker-compose down
docker-compose build tfms-app
docker-compose up -d
```

### Important Notes

#### Database Reset
If you encounter database initialization errors (duplicate records), clear the database and restart:
```bash
docker-compose down -v
docker-compose up -d
```

**Reason**: The `-v` flag removes the database volume, clearing all data. This is necessary when the database initialization code encounters duplicate records from previous runs. The application will recreate the database schema and seed data on the next startup.

#### Location Permissions
If you experience issues with location permissions in the browser:
- The browser may require you to **manually allow location access** for `localhost:8443`
- Go to your browser settings → Site Settings → Location
- Find `https://localhost:8443` and ensure it's set to "Allow"
- You may need to refresh the page after changing the permission

---

## Modules/ Services

- [Parcel Service](tfms-modules/planner-modules/PARCEL-README.md) – Handles parcel operations such as create, retrieve by ID, and retrieve by warehouse.