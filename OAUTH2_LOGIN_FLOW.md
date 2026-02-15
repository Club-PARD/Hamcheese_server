# Google OAuth2 로그인 흐름 완벽 가이드

## 📊 전체 흐름도

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         GOOGLE OAuth2 로그인 전체 흐름                        │
└─────────────────────────────────────────────────────────────────────────────┘

[STEP 1: 로그인 시작]
프론트엔드 (localhost:3000)
    ↓
    사용자가 "구글로 로그인" 버튼 클릭
    ↓
    구글 로그인 페이지로 리다이렉트
    GET /oauth2/authorization/google (백엔드 경유)
    ↓
[STEP 2: 구글 인증]
구글 서버
    ↓
    사용자가 구글 계정으로 로그인 & 권한 허가
    ↓
[STEP 3: 백엔드로 인증 코드 전달]
백엔드 (localhost:8080)
    ↓
    /login/oauth2/code/google?code=xxxxx (구글에서 보낸 코드)
    ↓
[STEP 4: 토큰 발급 및 DB 저장]
    ├─ OAuth2UserCustomService.loadUser()
    │  └─ 구글 API에서 사용자 정보(email, name) 가져오기
    │
    ├─ DB에 사용자 저장 또는 업데이트
    │  (email로 검색 → 없으면 신규 저장, 있으면 그대로)
    │
    ├─ OAuth2SuccessHandler.onAuthenticationSuccess()
    │  ├─ RefreshToken 발급 (14일 유효)
    │  ├─ RefreshToken을 DB에 저장
    │  ├─ RefreshToken을 HttpOnly 쿠키에 저장
    │  ├─ AccessToken 발급 (1분 유효)
    │  └─ AccessToken을 URL 쿼리 파라미터로 담아 프론트로 리다이렉트
    │
[STEP 5: 프론트엔드로 리다이렉트]
    ↓
    기존 사용자: http://localhost:3000/?view=feed&token=<accessToken>
    또는
    신규 사용자: http://localhost:3000/?view=setup&token=<accessToken>
    ↓
[STEP 6: API 요청]
프론트엔드가 Authorization 헤더에 AccessToken 포함
    ↓
    Authorization: Bearer <accessToken>
    ↓
[STEP 7: 토큰 검증]
TokenAuthenticationFilter
    ├─ Authorization 헤더에서 토큰 추출
    ├─ TokenProvider.validToken() 으로 유효성 검사
    ├─ 유효하면 SecurityContext에 인증정보 저장
    └─ 무효하면 401 반환 (프론트는 RefreshToken으로 새 AccessToken 획득)
    ↓
[STEP 8: 요청 처리]
컨트롤러가 인증된 요청 처리
```

---

## 🔐 상세 구간별 설명

### STEP 1: 로그인 시작 (프론트엔드 → 백엔드)

**프론트엔드 코드 예시:**
```javascript
// 사용자가 "구글로 로그인" 버튼 클릭 시
window.location.href = "http://localhost:8080/oauth2/authorization/google";
```

**백엔드 처리:**
- `WebOAuthSecurityConfig.filterChain()` 에서 `/oauth2/authorization/**` 은 `.permitAll()` 로 설정
- Spring Security가 자동으로 이 URL을 감지하고 구글로 리다이렉트

```java
// WebOAuthSecurityConfig.java 라인 77
.requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
```

---

### STEP 2: 구글 인증 (프론트엔드 ↔ 구글 서버)

```
프론트엔드
    ↓ (리다이렉트)
    https://accounts.google.com/o/oauth2/v2/auth?
        client_id=<YOUR_CLIENT_ID>
        &redirect_uri=http://localhost:8080/login/oauth2/code/google
        &scope=openid email profile
        &state=<random_state>
        &response_type=code
    ↓
구글 로그인 페이지 (사용자가 로그인 & 권한 허가)
    ↓
```

**이 단계에서 중요한 것:**
- `client_id`, `client_secret`, `redirect_uri` 는 `application.yaml` 에 설정되어야 함
- `state` 는 CSRF 공격 방지를 위한 난수값 (쿠키에 저장)
- `redirect_uri` 는 반드시 구글 개발자 콘솔에 등록된 주소여야 함

---

### STEP 3: 백엔드로 인증 코드 전달 (구글 서버 → 백엔드)

구글이 사용자 동의를 받으면, 백엔드의 `/login/oauth2/code/google` 로 리다이렉트:

```
https://localhost:8080/login/oauth2/code/google?code=4/0AY0...&state=xyz
                                                   ↑ 인증 코드
```

**백엔드의 Spring Security가 자동 처리:**
1. 인증 코드를 받아서
2. 구글 서버에 다시 POST 요청: `code` + `client_id` + `client_secret` → `access_token` 교환
3. 교환받은 `access_token` 으로 사용자 정보 API 호출

---

### STEP 4: 사용자 정보 로드 및 DB 저장

#### 4-1. `OAuth2UserCustomService.loadUser()` 실행

```java
// OAuth2UserCustomService.java 라인 24-29
@Override
public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User user = super.loadUser(userRequest); // ← 구글 API에서 사용자 정보 가져옴
    saveOrUpdate(user); // ← DB에 저장 또는 업데이트
    return user;
}
```

**구글 서버에서 받아오는 정보 (attributes):**
```json
{
  "sub": "1234567890",
  "email": "user@gmail.com",
  "name": "John Doe",
  "picture": "https://...",
  "given_name": "John",
  "family_name": "Doe",
  "locale": "en"
}
```

#### 4-2. 데이터베이스에 저장/업데이트

```java
// OAuth2UserCustomService.java 라인 32-46
private User saveOrUpdate(OAuth2User oAuth2User) {
    Map<String, Object> attributes = oAuth2User.getAttributes();

    String email = (String) attributes.get("email");
    String name = (String) attributes.get("name");

    return userRepository.findByEmail(email)
            .orElseGet(() -> userRepository.save(
                    User.builder()
                            .email(email)
                            .name(name)
                            .isProfileCompleted(false)  // ← 새 사용자 플래그
                            .build()
            ));
}
```

**로직:**
- `email` 으로 DB 조회
- **있으면**: 기존 사용자 (업데이트 없음, 그대로 반환)
- **없으면**: 신규 사용자 생성
  - `email` 과 `name` 만 저장
  - `isProfileCompleted = false` (아직 인적사항 미입력)
  - 다른 필드 (학년, 전공, GPA 등)는 나중에 `/user/create` 또는 `/user/update` 에서 입력

---

### STEP 5: 토큰 발급 및 쿠키 설정

#### 5-1. `OAuth2SuccessHandler.onAuthenticationSuccess()` 실행

```java
// OAuth2SuccessHandler.java 라인 40-61
@Override
public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    User user = userService.findByEmail((String) oAuth2User.getAttributes().get("email"));

    // Step 1: RefreshToken 발급
    String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION); // 14일
    saveRefreshToken(user.getUserId(), refreshToken); // DB 저장
    addRefreshTokenToCookie(request, response, refreshToken); // HttpOnly 쿠키 저장

    // Step 2: AccessToken 발급
    String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION); // 1분

    // Step 3: 리다이렉트 URL 결정
    String targetUrl = getTargetUrl(accessToken, user);

    // Step 4: 인증 임시 데이터 정리
    clearAuthenticationAttributes(request, response);

    // Step 5: 프론트로 리다이렉트
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
}
```

#### 5-2. Refresh Token 발급 및 저장

```java
// OAuth2SuccessHandler.java 라인 64-70
private void saveRefreshToken(Long userId, String newRefreshToken) {
    RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
            .map(entity -> entity.update(newRefreshToken)) // 기존 있으면 업데이트
            .orElse(new RefreshToken(userId, newRefreshToken)); // 없으면 신규 생성

    refreshTokenRepository.save(refreshToken); // DB 저장
}
```

**Refresh Token 특징:**
- 유효기간: **14일**
- 저장 위치: **DB (RefreshToken 테이블)** + **HttpOnly 쿠키**
- 용도: AccessToken 만료 시 새로운 AccessToken 발급받기

#### 5-3. Refresh Token을 HttpOnly 쿠키에 저장

```java
// OAuth2SuccessHandler.java 라인 72-79
private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
    int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds(); // 14일 = 1209600초
    CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME); // 기존 쿠키 삭제
    CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge); // 새 쿠키 추가
}
```

**HttpOnly 쿠키 설정:**
- 이름: `refresh_token`
- HttpOnly: `true` (JavaScript에서 접근 불가)
- Secure: `true` (HTTPS만 전송)
- SameSite: `Strict` (CSRF 방지)
- 유효기간: 14일

#### 5-4. Access Token 발급

```java
// OAuth2SuccessHandler.java 라인 49-50
String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION); // 1분
```

**Access Token 특징:**
- 유효기간: **1분**
- 저장 위치: **메모리 (localStorage/sessionStorage)**
- 용도: API 요청 시 Authorization 헤더에 담아 전송
- 형식: `Bearer <token>`

#### 5-5. JWT 토큰 구조

```java
// TokenProvider.java 라인 34-46
public String generateToken(User user, Duration expiredAt) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expiredAt.toMillis());
    return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // "typ": "JWT"
            .setIssuer(jwtProperties.getIssuer()) // "iss": "mate-check"
            .setIssuedAt(now) // "iat": 현재시간
            .setExpiration(expiry) // "exp": 만료시간
            .setSubject(user.getEmail()) // "sub": "user@gmail.com"
            .claim("userId", user.getUserId()) // 커스텀 클레임
            .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
            .compact();
}
```

**JWT 페이로드 예시:**
```json
{
  "typ": "JWT",
  "alg": "HS256"
}
.
{
  "iss": "mate-check",
  "iat": 1681234567,
  "exp": 1681234627,
  "sub": "user@gmail.com",
  "userId": 123
}
.
<signature>
```

#### 5-6. 리다이렉트 URL 결정

```java
// OAuth2SuccessHandler.java 라인 89-103
private String getTargetUrl(String token, User user) {
    if(user.isProfileCompleted()) { // 기존 사용자
        return UriComponentsBuilder.fromUriString(REDIRECT_MAINPAGE) // http://localhost:3000/?view=feed
                .queryParam("token", token)
                .build()
                .toUriString();
    } else { // 신규 사용자
        return UriComponentsBuilder.fromUriString(REDIRECT_SET_PROFILE) // http://localhost:3000/?view=setup
                .queryParam("token", token)
                .build()
                .toUriString();
    }
}
```

**리다이렉트 주소:**
- **기존 사용자**: `http://localhost:3000/?view=feed&token=<accessToken>` (메인 피드 페이지)
- **신규 사용자**: `http://localhost:3000/?view=setup&token=<accessToken>` (프로필 입력 페이지)

---

### STEP 6: 프론트엔드에서 토큰 저장 및 사용

#### 6-1. 프론트엔드가 받는 리다이렉트

```
브라우저 주소창:
http://localhost:3000/?view=feed&token=eyJhbGc...
                                     ↑ AccessToken
```

#### 6-2. AccessToken을 localStorage에 저장 (프론트 처리)

```javascript
// 프론트엔드 코드 (React 예시)
const params = new URLSearchParams(window.location.search);
const accessToken = params.get('token');

if (accessToken) {
    localStorage.setItem('accessToken', accessToken);
    // 또는 sessionStorage.setItem('accessToken', accessToken);
}
```

#### 6-3. API 요청 시 Authorization 헤더에 포함

```javascript
// API 요청
const response = await fetch('http://localhost:8080/user/myProfile/123', {
    method: 'GET',
    headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        'Content-Type': 'application/json'
    }
});
```

**프론트엔드에서 보내는 요청:**
```
GET /user/myProfile/123
Authorization: Bearer eyJhbGc...
Content-Type: application/json
```

---

### STEP 7: 백엔드의 토큰 검증

#### 7-1. TokenAuthenticationFilter에서 토큰 추출

```java
// TokenAuthenticationFilter.java 라인 28-78
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION); // "Authorization"
    String token = getAccessToken(authorizationHeader); // "Bearer " 제거하고 토큰만 추출

    if (token == null) {
        // 토큰 없음 → 다음 필터로 (공개 API는 계속 진행)
        filterChain.doFilter(request, response);
        return;
    }

    // 토큰 유효성 검사
    boolean valid = tokenProvider.validToken(token);

    if (valid) {
        // 유효 → 인증정보를 SecurityContext에 저장
        Authentication authentication = tokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    // 무효 → 인증정보 저장 안 함 (401 발생)

    filterChain.doFilter(request, response);
}

private String getAccessToken(String authorizationHeader) {
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        return authorizationHeader.substring(7); // "Bearer " 제거
    }
    return null;
}
```

#### 7-2. TokenProvider에서 토큰 유효성 검사

```java
// TokenProvider.java 라인 49-59
public boolean validToken(String token) {
    try {
        Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token); // 서명 검증 & 만료 확인

        return true;
    } catch (Exception e) { // 만료되었거나 서명이 맞지 않으면
        return false;
    }
}
```

**검증 내용:**
- ✅ 서명(signature) 검증
- ✅ 만료 시간(expiration) 확인
- ✅ 필수 클레임 확인

#### 7-3. 인증정보를 SecurityContext에 저장

```java
// TokenProvider.java 라인 64-74
public Authentication getAuthentication(String token) {
    Claims claims = getClaims(token); // JWT 페이로드 추출

    Long userId = claims.get("userId", Long.class); // 커스텀 클레임에서 userId 추출
    String email = claims.getSubject(); // "sub" 클레임에서 email 추출

    var principal = new CustomPrincipal(userId, email);
    var authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
}
```

**저장되는 인증정보:**
```java
public record CustomPrincipal(Long userId, String email) {}

Authentication {
    principal: CustomPrincipal(123, "user@gmail.com"),
    credentials: "<token>",
    authorities: ["ROLE_USER"]
}
```

---

### STEP 8: 인가(Authorization) 처리

```java
// WebOAuthSecurityConfig.java 라인 75-85
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
    .requestMatchers("/error").permitAll()
    .requestMatchers(apiToken).permitAll()
    .requestMatchers(mateFindAll, mateFilter, recruitingFindAll, recruitingFilter).permitAll()
    .requestMatchers("/chat/inbox/**").permitAll()
    .anyRequest().authenticated() // ← 나머지 모든 요청은 인증 필수
)
```

**인가 흐름:**
1. SecurityContext에 인증정보가 있으면 (토큰이 유효하면)
   - → 요청 진행 (200 OK)
2. SecurityContext에 인증정보가 없으면 (토큰이 없거나 무효하면)
   - → 401 Unauthorized 반환
   - → 프론트엔드가 RefreshToken으로 새 AccessToken 획득

---

## 🔄 AccessToken 만료 후 갱신 흐름

### 시나리오: AccessToken이 만료됨

```
프론트엔드가 API 요청
    ↓
백엔드: 401 Unauthorized (AccessToken 만료)
    ↓
프론트엔드가 갱신 엔드포인트 호출
    ↓
POST /api/token
Content-Type: application/json

{
  "refreshToken": "eyJhbGc..." (쿠키에서 자동 전송됨)
}
    ↓
백엔드: 새로운 AccessToken 발급
    ↓
{
  "accessToken": "eyJhbGc..." (새 토큰)
}
    ↓
프론트엔드: 새 AccessToken을 localStorage에 저장
    ↓
원래 요청 재시도
```

### 백엔드 처리 코드

```java
// TokenApiController.java 라인 22-28
@PostMapping("/api/token")
public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(
        @RequestBody CreateAccessTokenRequest request) {
    String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CreateAccessTokenResponse(newAccessToken));
}
```

```java
// TokenService.java 라인 22-33
public String createNewAccessToken(String refreshToken) {
    if(!tokenProvider.validToken(refreshToken)) { // RefreshToken 유효성 검사
        throw new IllegalArgumentException("Unexpected token");
    }

    Long userId = refreshTokenService.findByRefreshToken(refreshToken).getUserId();
    User user = userService.findById(userId);

    return tokenProvider.generateToken(user, Duration.ofHours(2)); // 새로운 AccessToken 발급
}
```

---

## 🔌 CORS 설정 (프론트-백엔드 통신)

```java
// WebOAuthSecurityConfig.java 라인 131-145
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of("http://localhost:3000")); // 프론트엔드 주소
    config.setAllowCredentials(true); // 쿠키 포함 허용
    config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Authorization")); // Authorization 헤더 노출

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**설정 의미:**
- `setAllowedOrigins`: 프론트엔드 도메인만 요청 허용
- `setAllowCredentials(true)`: 쿠키(RefreshToken) 포함된 요청 허용
- `setExposedHeaders`: 프론트가 응답 헤더의 Authorization 접근 가능

---

## 📋 환경설정 (application.yaml)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: <YOUR_GOOGLE_CLIENT_ID>
            client-secret: <YOUR_GOOGLE_CLIENT_SECRET>
            redirect-uri: "http://localhost:8080/login/oauth2/code/google"
            scope:
              - email
              - profile
        provider:
          google:
            authorization-uri: "https://accounts.google.com/o/oauth2/v2/auth"
            token-uri: "https://www.googleapis.com/oauth2/v4/token"
            user-info-uri: "https://www.googleapis.com/oauth2/v1/userinfo"
            jwk-set-uri: "https://www.googleapis.com/oauth2/v3/certs"

app:
  jwt:
    issuer: "mate-check"
    secret-key: "<YOUR_SECRET_KEY_AT_LEAST_32_CHARS>"
```

---

## 🚀 구글 개발자 콘솔 설정 체크리스트

- [ ] **프로젝트 생성**: Google Cloud Console에서 새 프로젝트 생성
- [ ] **OAuth 동의 화면**: OAuth 동의 화면 구성 (사용자 타입: 외부)
- [ ] **클라이언트 ID 생성**: 크리덴셜 → OAuth 2.0 클라이언트 ID → 웹 애플리케이션
- [ ] **리다이렉트 URI 등록**:
  - 개발: `http://localhost:8080/login/oauth2/code/google`
  - 운영: `https://matecheck.co.kr/login/oauth2/code/google`
- [ ] **클라이언트 ID & Secret 복사**: `application.yaml` 에 저장
- [ ] **필요한 스코프 설정**: `openid email profile`

---

## 🔒 보안 사항

| 항목 | 현재 상태 | 개선 필요 |
|-----|---------|--------|
| AccessToken 저장 | localStorage | sessionStorage 권장 |
| RefreshToken 저장 | HttpOnly 쿠키 | ✅ 안전함 |
| HTTPS | ❌ 개발 환경 | ✅ 운영 환경 필수 |
| CSRF 토큰 | 비활성화 | ✅ CSRF 방어 활성화 권장 |
| 토큰 유효기간 | AccessToken: 1분, RefreshToken: 14일 | ✅ 적절함 |

---

## 💡 추가 설명

### 왜 RefreshToken과 AccessToken을 분리하나?

- **AccessToken (단명)**: 매 요청마다 사용하므로 자주 검증됨 → 짧은 유효기간 (1분)
- **RefreshToken (장명)**: 토큰 갱신 시에만 사용 → 긴 유효기간 (14일)

만약 AccessToken만 14일로 설정하면:
- 해커가 AccessToken을 탈취했을 때 14일 동안 사용 가능 (위험)
- RefreshToken은 안전한 쿠키에만 저장되므로 탈취 위험 낮음

### 왜 HttpOnly 쿠키를 사용하나?

- **localStorage**: JavaScript에서 접근 가능 → XSS 공격으로 탈취 위험
- **HttpOnly 쿠키**: JavaScript 접근 불가 → XSS 공격으로도 안전
- **자동 전송**: 도메인의 HTTP 요청에 자동으로 쿠키 포함

### 왜 AccessToken을 URL 쿼리 파라미터로 전달하나?

- OAuth2 로그인 후 리다이렉트되는 순간만 프론트에 전달할 방법이 제한적
- 쿠키로는 도메인이 다르면 (localhost:3000 vs localhost:8080) 전달 불가
- URL 쿼리 파라미터로 전달 후 프론트의 localStorage에 저장
