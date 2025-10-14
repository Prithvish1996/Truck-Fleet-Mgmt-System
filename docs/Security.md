# Security Documentation

## Security Design Choices

Security mechanisms implemented in the TFMS system are described below. Only features present in the codebase are documented.

### Signing Algorithm

- **Symmetric Signing (HS256):**
  - JWT tokens are signed using HMAC with SHA-256 (HS256).
  - A single shared secret key is used for both signing and verification.

### Token Lifetimes

- **Access Token:**
  - Default expiration is 24 hours, configurable via the `jwt.expiration` property.

### Token Claims

- `userType`: Indicates the user's role (e.g., ADMIN, DRIVER).
- `userID`: Unique identifier for the user (if present in token generation logic).

### Token Blacklisting

- Blacklisted tokens are stored in-memory using `TokenBlacklistService`.
- Tokens are checked for blacklisting on every authenticated request.
- Blacklisted tokens can be removed after a retention period (e.g., 2 years).

### Authentication Flow

1. User authenticates and receives a JWT access token.
2. Token is sent as a Bearer token in the Authorization header for subsequent requests.
3. Server validates the token and checks if it is blacklisted.
4. On logout, the token is added to the blacklist.

### Security Filters

- Security filters intercept HTTP requests and enforce authentication using JWT tokens.
- Filters extract the Bearer token, validate its signature and expiration, and check blacklisting status.
- Filters are registered in the Spring Security configuration (if present in code).

### HTTPS and Secure Transport

- HTTPS configuration is present if a keystore and related properties are set in the application configuration.
- For development, a self-signed TLS certificate may be generated using OpenSSL:

```bash
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes -subj "/CN=localhost"
```

- `key.pem`: Private key file
- `cert.pem`: Certificate file
- `-days 365`: Certificate validity period (1 year)
- `-subj "/CN=localhost"`: Common Name set to localhost

### Keystore Generation for HTTPS

A keystore is a secure storage file that contains the private key and certificate required for HTTPS communication. In Spring Boot, the keystore.p12 file is referenced in the application configuration to enable encrypted connections between the server and clients. The private key is used to establish secure SSL/TLS sessions, ensuring confidentiality and integrity of data in transit. The certificate verifies the server's identity to clients.

A PKCS#12 keystore file (keystore.p12) is used for HTTPS configuration in Spring Boot. The following steps generate the keystore from a self-signed certificate and private key:

1. Generate a private key and certificate:

```bash
openssl req -newkey rsa:4096 -nodes -keyout key.pem -x509 -days 365 -out cert.pem -subj "/CN=localhost"
```

2. Convert the PEM files to a PKCS#12 keystore:

```bash
openssl pkcs12 -export -in cert.pem -inkey key.pem -out keystore.p12 -name "springboot" -password pass:changeit
```

- `keystore.p12`: PKCS#12 keystore file for Spring Boot HTTPS
- `-name "springboot"`: Key alias
- `-password pass:changeit`: Keystore password

#### Usage in Spring Boot

Reference the generated keystore.p12 in the Spring Boot configuration to enable HTTPS:

```
server.port=8443
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=springboot
```

This configuration enables HTTPS for the application, using the generated keystore for secure communication.

### Brute Force Protection

- Rate limiting is enabled via configuration:
  - `app.rate-limit.enabled=true`
  - `app.rate-limit.requests-per-minute=60`
  - `app.rate-limit.requests-per-hour=1000`
  - `app.rate-limit.burst-capacity=10`
- Limits the number of requests per minute and per hour to mitigate brute force attacks.

### Password Validation and Storage

Password validation is implemented in the LoginRequestDto class using Jakarta Bean Validation annotations:
- @NotBlank ensures the password field is not empty.
- @Size enforces a minimum length of 6 characters.
- @Pattern requires the password to contain both letters and numbers.

These constraints are validated automatically during login requests, ensuring that only passwords meeting these criteria are accepted. Password encoding and storage are handled separately using BCryptPasswordEncoder before saving to the database.

Example:
- `private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();`
- `user.setPassword(passwordEncoder.encode(password));`

---
_Last updated: October 14, 2025_
