# Build Frontend
FROM node:18-alpine AS frontend-builder

WORKDIR /app/frontend

COPY frontend/my-app/package*.json ./

RUN npm ci

COPY frontend/my-app/ ./

RUN npm run build

# Build Backend
FROM maven:3.9-eclipse-temurin-17 AS backend-builder

WORKDIR /app

COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
COPY .mvn .mvn

COPY tfms-modules ./tfms-modules
COPY tfms-starter/pom.xml ./tfms-starter/

RUN mvn dependency:go-offline -B

COPY tfms-modules ./tfms-modules
COPY tfms-starter/src ./tfms-starter/src

COPY frontend/my-app ./frontend

RUN mvn clean package -Pprod -DskipTests -B

# Runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN mkdir -p /app/logs /app/ssl

RUN apt-get update && apt-get install -y --no-install-recommends wget && rm -rf /var/lib/apt/lists/*

RUN keytool -genkeypair \
    -alias tfms-prod \
    -keyalg RSA \
    -keysize 2048 \
    -storetype PKCS12 \
    -keystore /app/ssl/keystore.p12 \
    -validity 365 \
    -storepass changeit \
    -keypass changeit \
    -dname "CN=localhost, OU=TFMS, O=TFMS, L=Unknown, ST=Unknown, C=US" \
    -ext "SAN=DNS:localhost,IP:127.0.0.1" \
    -noprompt

COPY --from=backend-builder /app/tfms-starter/target/tfms-*.jar app.jar

RUN groupadd -r spring && useradd -r -g spring spring
RUN chown -R spring:spring /app
USER spring:spring

EXPOSE 8443

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider --no-check-certificate https://localhost:8443/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", \
  "-jar", \
  "app.jar", \
  "--spring.profiles.active=prod", \
  "--spring.jpa.hibernate.ddl-auto=update", \
  "--logging.file.name=/app/logs/tfms.log"]

