# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🚀 Project Overview

**mate check!** is a team-matching platform for university students to find suitable collaborators for projects, assignments, and clubs through profile browsing, peer reviews, and matching requests.

- **Backend**: Spring Boot 4.0.1, Java 17, Spring Security with Google OAuth2
- **Database**: MySQL 8.0 with JPA/Hibernate ORM
- **Real-time**: WebSocket for chat messaging
- **File Storage**: AWS S3 for profile images
- **Deployment**: AWS EC2 + Nginx (manual deploy, no CI/CD yet)

## 📋 Build and Development Commands

### Build
```bash
# Build with Gradle
./gradlew build

# Build and skip tests (faster during development)
./gradlew build -x test

# Clean build directory
./gradlew clean
```

### Run Application
```bash
# Run directly with Gradle
./gradlew bootRun

# Run from built JAR
java -jar build/libs/Longkathon-0.0.1-SNAPSHOT.jar

# With environment-specific profiles (if configured)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests TokenProviderTest

# Run tests matching a pattern
./gradlew test --tests "*TokenProvider*"

# Run tests with more verbose output
./gradlew test --info
```

### Local Development
1. **Prerequisites**: Java 17+, MySQL 8.0+, Gradle
2. **Database setup**: Create MySQL database matching `application.yaml`
3. **Environment config**: Set AWS credentials and MySQL details in `src/main/resources/application.yaml`
4. **Start**: `./gradlew bootRun` (server runs on http://localhost:8080)
5. **API Docs**: http://localhost:8080/swagger-ui/index.html

## 🏗️ Architecture and Code Organization

### Domain-Driven Structure
The codebase follows a modular architecture with features as separate domains:

```
src/main/java/pard/server/com/longkathon/
├── config/                    # Configuration & infrastructure
│   ├── jwt/                   # JWT token generation/validation + refresh tokens
│   ├── oauth/                 # Google OAuth2 setup and handlers
│   ├── webSocket/             # WebSocket configuration and chat infrastructure
│   ├── SecurityConfig.java    # Spring Security configuration
│   ├── SwaggerConfig.java     # Swagger/OpenAPI setup
│   └── TokenAuthenticationFilter.java
├── MyPage/                    # User profile domain
│   ├── user/                  # Core user entity and profile management
│   ├── activity/              # User activity records
│   ├── skillStackList/        # User skill tags
│   ├── peerReview/            # Peer review entities
│   ├── introduction/          # User introduction/bio
│   └── userFile/              # Profile image references
├── posting/                   # Team recruiting domain
│   ├── recruiting/            # Recruitment post CRUD
│   └── myKeyword/             # Keywords for recruiting posts
├── poking/                    # Mate check (matching request) domain
├── alarm/                     # Notification domain
├── BaseEntity/                # Base JPA entity with timestamp fields
├── s3/                        # AWS S3 file upload/delete operations
└── util/                      # Utility classes
```

### Key Design Patterns

**Time Zone Handling** (Critical):
- All date/time fields must use `Asia/Seoul` timezone
- Add `@PrePersist` to set `LocalDateTime.now(ZoneId.of("Asia/Seoul"))` for `Recruiting` and `Poking` entities
- Without this, AWS server's default timezone (often UTC) causes time mismatch with frontend expectations

**File Upload Pattern**:
- User profile images use multipart/form-data with two parts:
  - `profileImage`: File binary
  - `data`: JSON string of user details
- See `/user/create` and `/user/updateImage/{myId}` endpoints in README.md

**OAuth2 + JWT Flow**:
1. Frontend sends Google `idToken` to `/auth/google/exists`
2. Backend validates token, extracts email/socialId, checks if user exists
3. Returns `exists` flag + `myId` (for existing users)
4. On signup completion, frontend receives JWT `accessToken` (header) + `refreshToken` (HTTP-only cookie)
5. All subsequent requests use `Authorization: Bearer {accessToken}` header

**Peer Review Aggregation**:
- Top 3 good/bad keywords are computed and stored in separate aggregate tables
- This improves query performance vs. counting on-the-fly

### Domain Responsibilities

| Domain | Key Responsibility |
|--------|-------------------|
| **MyPage** | User profile CRUD, education/skill info, peer review history |
| **posting** | Create/edit/delete recruiting posts, filter by type/department |
| **poking** | Send/receive/accept/reject matching requests, prevent duplicates |
| **alarm** | Generate notifications on acceptance/rejection, list/delete notifications |
| **config/webSocket** | Real-time chat after matching, message persistence |
| **s3** | Upload/store/delete profile images via AWS S3 with UUID naming |

## 🔐 Important Technical Notes

### Authentication & Security
- **No traditional sessions**: Uses OAuth2 + JWT pattern
- **HTTP-only cookies**: Refresh tokens stored in HTTP-only cookies (CSRF protection via same-site)
- **CSRF disabled**: Currently disabled in SecurityConfig for simplicity (enable in production)
- **Scope**: All endpoints that modify user data require valid JWT (enforced by filter)

### Database Configuration
- `application.yaml` controls JPA behavior: `ddl-auto: update` (dev) vs. `validate` (prod)
- ORM handles lazy loading on relationships (be aware of N+1 query issues in list endpoints)
- For large result sets, pagination should be added to filter/findAll endpoints

### WebSocket Chat
- Located in `config/webSocket/` with `ChatRoom`, `ChatMessage`, and message broker configuration
- Uses STOMP protocol (enabled in `WebSocketConfig`)
- Persistence via `ChatMessageRepository` (messages stored in DB)

### File Uploads to S3
- UUID-based filenames prevent collisions
- Delete endpoint removes file from S3 + deletes DB reference
- AWS credentials must be set in `application.yaml` (`cloud.aws.credentials`)

## 🛠️ Common Development Workflows

### Adding a New Feature/Endpoint
1. **Create Entity**: Add JPA entity extending `BaseEntity` in appropriate domain folder
2. **Add Repository**: Extend `JpaRepository<EntityType, Long>` in same folder
3. **Add Service**: Implement business logic; handle authorization checks
4. **Add Controller**: Create REST endpoints with proper HTTP methods, path variables, request bodies
5. **Update Tests**: Add unit tests in `src/test/java/` following existing test patterns
6. **Document**: Update API_DOCUMENTATION.md or Swagger annotations

### Modifying User/Authentication Flow
- Changes to `MyPage/user/` domain may affect signup (`/user/create`), profile display (`/user/myProfile/{myId}`), and filtering (`/user/filter`)
- Ensure time zone handling is applied to any new date fields
- Update Swagger annotations for API documentation

### Debugging Common Issues
- **Time mismatch**: Check that `@PrePersist` is applied to entities with date fields
- **CORS errors**: Verify `CorsConfig` allows frontend origin (currently hardcoded to `http://localhost:3000`)
- **File upload 415 errors**: Check multipart field names (`profileImage`, `data`) match request
- **S3 upload failures**: Verify AWS credentials and bucket permissions in `application.yaml`
- **JWT token errors**: Check `TokenProvider` expiration times and refresh token storage

## 🔄 Deployment Notes

**Current Approach**: Manual EC2 deployment
1. Local push to GitHub
2. SSH into EC2, pull latest code
3. `./gradlew build` on server
4. Systemd or manual `java -jar` to start
5. Nginx reverse proxy on port 80/443

**Future**: Docker + CI/CD via GitHub Actions planned

**Environment Checklist** (before deploying to EC2):
- [ ] AWS credentials set in `application.yaml`
- [ ] MySQL connection string updated for server database
- [ ] S3 bucket name and region correct
- [ ] Timezone set to `Asia/Seoul` in server/application
- [ ] Frontend CORS origin updated in `CorsConfig.java`
- [ ] SSL certificates configured in Nginx

## 📚 Related Documentation

- **API Spec**: See [README.md](./README.md) (Section "📋 상세 API 문서") and [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)
- **Database Schema**: See ERD image in README.md or check MySQL for current schema
- **Troubleshooting & Lessons Learned**: See README.md (Section "### 트러블슈팅")
- **Swagger/OpenAPI**: Live at `/swagger-ui/index.html` when server runs
- **Google OAuth2 Login Flow**: See [OAUTH2_LOGIN_FLOW.md](./OAUTH2_LOGIN_FLOW.md) (detailed with diagrams)
- **OAuth2 Quick Reference**: See [OAUTH2_QUICK_REFERENCE.md](./OAUTH2_QUICK_REFERENCE.md) (for quick lookups)

## 📝 Code Style Notes

- **Lombok**: Used for `@Data`, `@Getter`, `@Setter`, `@Builder` to reduce boilerplate
- **Naming**: REST endpoints follow RESTful conventions; domain packages group related entities/services
- **Error Handling**: Currently minimal; consider structured error responses in future
- **Logging**: `application.yaml` sets `DEBUG` level for package—monitor for excessive logs in production
