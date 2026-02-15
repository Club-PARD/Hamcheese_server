# OAuth2 로그인 성공 후 리다이렉트 방식 비교 분석

## Context

사용자가 Google OAuth2 로그인 성공 후 프론트엔드로 돌아가는 방식에 대해 두 가지 접근법을 비교 분석해달라고 요청했습니다:

1. **현재 방식**: 서버가 직접 리다이렉트 URL을 제공 (302 redirect + 쿼리 파라미터에 토큰)
2. **대안 방식**: 서버가 JSON 응답으로 boolean 값과 토큰 반환, 프론트가 리다이렉트 처리

현재 프로젝트는 방식 1을 사용하고 있으며, OAuth2SuccessHandler에서 `isProfileCompleted` 플래그에 따라 다른 URL로 리다이렉트합니다. 이 분석은 프로덕션 배포 전 보안과 아키텍처를 개선하기 위한 의사결정을 돕기 위함입니다.

---

## 비교 분석 결과

### 1. 현재 방식: Server-side Redirect (302 with Query Parameter)

#### 구현 방식
```
OAuth2 로그인 성공
    ↓
OAuth2SuccessHandler.onAuthenticationSuccess()
    ↓
getTargetUrl(accessToken, user)
    ├─ isProfileCompleted == true  → http://localhost:3000/?view=feed&token=<accessToken>
    └─ isProfileCompleted == false → http://localhost:3000/?view=setup&token=<accessToken>
    ↓
302 Redirect (브라우저가 프론트로 자동 이동)
```

**관련 파일**:
- `src/main/java/pard/server/com/longkathon/config/oauth/OAuth2SuccessHandler.java` (라인 89-103)

#### 장점
| 항목 | 설명 |
|------|------|
| **구현 단순성** | Spring Security의 `SimpleUrlAuthenticationSuccessHandler` 상속, 기본 메커니즘 활용 |
| **프레임워크 호환성** | OAuth2 표준 패턴과 일치, 추가 커스터마이징 불필요 |
| **빠른 개발** | 프론트엔드에 추가 로직 불필요, URL을 받으면 바로 표시 |
| **브라우저 히스토리** | 자연스러운 페이지 전환, 뒤로가기 지원 |

#### 단점 (심각한 보안 위험)
| 항목 | 설명 |
|------|------|
| **⚠️ URL에 토큰 노출** | `?token=eyJhbGciOiJIUzI1NiJ9...` 형태로 AccessToken이 URL에 평문 노출 |
| **브라우저 히스토리** | 토큰이 브라우저 히스토리에 영구 저장됨 |
| **웹 서버 로그** | Nginx/Apache access log에 토큰이 기록될 수 있음 |
| **Referer 헤더 유출** | 사용자가 외부 링크 클릭 시 Referer 헤더로 토큰 전달됨 |
| **공유 링크 위험** | 사용자가 URL을 복사하면 토큰도 함께 공유됨 |
| **캐싱 위험** | 프록시나 CDN에 URL이 캐시될 수 있음 |
| **RFC 6750 위반** | "Bearer tokens SHOULD NOT be passed in page URLs" 명시적 권고 위반 |
| **표준 위반** | REST API 원칙과 불일치, SPA 패턴과 맞지 않음 |

#### 보안 위험 예시
```bash
# 1. 브라우저 히스토리
http://localhost:3000/?view=feed&token=eyJhbGc... ← 토큰 영구 저장

# 2. Nginx Access Log
GET /?view=feed&token=eyJhbGc... HTTP/1.1 200

# 3. Referer 헤더 (사용자가 외부 링크 클릭 시)
Referer: http://localhost:3000/?view=feed&token=eyJhbGc...
```

---

### 2. 대안 방식: Client-side Redirect (JSON Response)

#### 구현 방식
```
OAuth2 로그인 성공
    ↓
Custom OAuth2SuccessHandler
    ↓
JSON 응답 작성
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "...",  // 또는 쿠키로만
  "isProfileCompleted": true/false,
  "redirectUrl": "/feed" 또는 "/setup"
}
    ↓
HTTP 200 (JSON Body로 응답)
    ↓
프론트엔드에서 응답 처리
    ├─ localStorage.setItem('accessToken', accessToken)
    └─ navigate(redirectUrl)
```

**필요한 파일 수정**:
- `OAuth2SuccessHandler.java` - Custom handler로 교체
- 프론트엔드 - OAuth2 콜백 페이지 추가

#### 장점
| 항목 | 설명 |
|------|------|
| **✅ 보안 강화** | 토큰이 POST 응답 Body에만 존재, URL/로그/히스토리에 노출 안 됨 |
| **RFC 6750 준수** | "Bearer tokens SHOULD be passed in HTTP message bodies" 권장사항 준수 |
| **REST API 원칙** | JSON 응답은 RESTful API 표준 |
| **유연성** | 프론트엔드가 라우팅과 상태 관리 제어 가능 |
| **캐싱 제어** | POST 응답은 기본적으로 캐시되지 않음 |
| **확장성** | 추가 메타데이터 (userId, profileUrl 등) 전달 용이 |

#### 단점
| 항목 | 설명 |
|------|------|
| **구현 복잡도** | Custom AuthenticationSuccessHandler 작성 필요 |
| **프론트 로직 추가** | OAuth2 콜백 처리 페이지 및 리다이렉트 로직 구현 |
| **프레임워크 패턴 오버라이드** | Spring Security 기본 흐름 변경 |
| **CORS Preflight** | JSON 응답 시 Preflight 요청 처리 필요 (이미 설정됨) |

---

## 종합 평가

### 점수 비교표

| 평가 항목 | 현재 방식 (Server Redirect) | 대안 방식 (JSON Response) |
|-----------|---------------------------|-------------------------|
| **보안** | ⚠️ 2/5 (URL 노출, 로그 위험) | ✅ 5/5 (Body만, 안전) |
| **표준 준수** | ⚠️ 2/5 (RFC 6750 위반) | ✅ 5/5 (RFC, REST 준수) |
| **유지보수** | ✅ 4/5 (Spring 표준) | ⚠️ 3/5 (커스텀 핸들러) |
| **사용자 경험** | ✅ 4/5 (빠른 리다이렉트) | ✅ 4/5 (약간 느림) |
| **구현 복잡도** | ✅ 5/5 (매우 단순) | ⚠️ 3/5 (백/프론트 수정) |
| **SPA 적합성** | ⚠️ 2/5 (MVC 패턴) | ✅ 5/5 (REST API) |

---

## 최종 권장사항

### 즉시 적용 (단기 해결책)

현재 방식을 유지하되, 다음 보안 패치 필수:

#### 1. 프론트엔드: URL에서 토큰 즉시 제거
```javascript
// React 예시
useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');

    if (token) {
        // localStorage에 저장
        localStorage.setItem('accessToken', token);

        // ⚠️ 중요: URL에서 토큰 즉시 제거
        window.history.replaceState({}, document.title, window.location.pathname);

        // view 파라미터에 따라 리다이렉트
        const view = params.get('view');
        if (view === 'setup') {
            navigate('/profile-setup');
        } else if (view === 'feed') {
            navigate('/feed');
        }
    }
}, []);
```

**효과**: 브라우저 히스토리에 토큰이 남지 않음

#### 2. 백엔드: AccessToken 만료 시간 단축
```java
// OAuth2SuccessHandler.java
public static final Duration ACCESS_TOKEN_DURATION = Duration.ofSeconds(30);  // 1분 → 30초
```

**효과**: URL 노출 시간을 최소화

#### 3. 백엔드: CSRF 보호 활성화
```java
// WebOAuthSecurityConfig.java
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCSRFTokenRepository.withHttpOnlyFalse())
    .ignoringRequestMatchers("/oauth2/**", "/login/oauth2/**")
)
```

**효과**: RefreshToken 쿠키를 CSRF 공격으로부터 보호

#### 4. 백엔드: 쿠키 보안 강화
CookieUtil에서 SameSite 속성 추가:
```java
cookie.setSecure(true);      // HTTPS only (운영 환경)
cookie.setHttpOnly(true);    // JavaScript 접근 차단
cookie.setSameSite("Strict"); // CSRF 방어
```

---

### 장기 개선 (리팩토링 권장)

프로덕션 배포 또는 다음 스프린트에서 적용:

#### JSON 응답 방식으로 전환 시 동작 흐름

**백엔드 (Java/Spring)**:
1. OAuth2 로그인 성공 시 Custom AuthenticationSuccessHandler 실행
2. 토큰 발급 (AccessToken, RefreshToken)
3. **JSON 응답 생성 및 전송** (여기서 `response.getWriter().write()` 사용):
   ```json
   {
     "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
     "isProfileCompleted": true,
     "redirectUrl": "/feed"
   }
   ```
4. HTTP 200 OK로 응답 (302 Redirect 대신)

**프론트엔드 (JavaScript/React)**:
1. OAuth2 콜백 URL (`/login/oauth2/code/google`)을 처리하는 페이지 생성
2. 백엔드에서 JSON 응답 받기:
   ```javascript
   const response = await fetch('http://localhost:8080/login/oauth2/code/google');
   const data = await response.json();
   ```
3. **프론트가 JSON의 `isProfileCompleted`를 보고 스스로 판단**:
   ```javascript
   if (data.isProfileCompleted) {
       navigate('/feed');     // 기존 사용자
   } else {
       navigate('/profile-setup');  // 신규 사용자
   }
   ```
4. AccessToken을 localStorage에 저장

**핵심 차이점**:
- **현재 방식**: 서버가 302 Redirect → 브라우저가 자동 이동 (서버 제어)
- **JSON 방식**: 서버가 200 JSON → 프론트가 데이터 보고 navigate() 호출 (프론트 제어)

**주요 수정 파일**:
- `OAuth2SuccessHandler.java`: JSON 응답 로직 추가
- `WebOAuthSecurityConfig.java`: Success handler 교체
- 프론트엔드: OAuth2 콜백 처리 페이지 신규 생성

---

## 보안 체크리스트 (프로덕션 배포 전 필수)

- [ ] **URL에서 토큰 제거**: `window.history.replaceState()` 구현
- [ ] **HTTPS 적용**: 운영 환경에서 필수
- [ ] **CSRF 보호 활성화**: RefreshToken 쿠키 보호
- [ ] **SameSite 쿠키 속성**: `SameSite=Strict` 설정
- [ ] **Secure 쿠키 플래그**: HTTPS에서 `Secure=true`
- [ ] **민감정보 환경변수화**: `client-secret`, AWS keys 등
- [ ] **보안 헤더 추가**: CSP, X-Frame-Options, HSTS
- [ ] **RefreshToken Rotation**: 재사용 방지
- [ ] **로그 마스킹**: AccessToken이 로그에 남지 않도록

---

## 핵심 메커니즘 설명

### Q1: `response.getWriter().write(objectMapper.writeValueAsString(responseBody))`가 프론트로 JSON을 보내는 거야?

**답: 네, 정확합니다!**

이 코드의 동작 과정:

```java
// 1. Java 객체 생성
Map<String, Object> responseBody = Map.of(
    "accessToken", "eyJhbGc...",
    "isProfileCompleted", true,
    "redirectUrl", "/feed"
);

// 2. Java 객체 → JSON 문자열 변환 (Jackson ObjectMapper 사용)
ObjectMapper objectMapper = new ObjectMapper();
String jsonString = objectMapper.writeValueAsString(responseBody);
// jsonString = '{"accessToken":"eyJhbGc...","isProfileCompleted":true,"redirectUrl":"/feed"}'

// 3. HTTP 응답 Body에 JSON 문자열 작성 → 프론트로 전송
response.getWriter().write(jsonString);
```

**HTTP 응답 구조**:
```
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 156

{"accessToken":"eyJhbGc...","isProfileCompleted":true,"redirectUrl":"/feed"}
```

프론트엔드는 이 HTTP 응답 Body를 `response.json()`으로 파싱하여 JavaScript 객체로 사용합니다.

---

### Q2: 프론트는 JSON의 isProfileCompleted를 보고 스스로 redirect 하는거야?

**답: 네, 맞습니다!**

**현재 방식 (Server Redirect)**:
```
서버: "302 Redirect → http://localhost:3000/?view=feed"
브라우저: 자동으로 해당 URL로 이동 (프론트 개입 없음)
```

**JSON 방식 (Client Redirect)**:
```
서버: "200 OK + JSON 응답"
프론트: JSON을 받아서 분석 → 조건에 따라 navigate() 호출
```

**프론트엔드 코드 예시**:

```javascript
// 백엔드에서 JSON 응답 받기
const response = await fetch('http://localhost:8080/login/oauth2/code/google', {
    credentials: 'include'  // 쿠키(RefreshToken) 자동 포함
});

// JSON 파싱
const data = await response.json();
// data = {
//   "accessToken": "eyJhbGc...",
//   "isProfileCompleted": true,
//   "redirectUrl": "/feed"
// }

// AccessToken 저장
localStorage.setItem('accessToken', data.accessToken);

// 프론트가 스스로 판단하여 리다이렉트
if (data.isProfileCompleted) {
    navigate('/feed');           // 기존 사용자 → 메인 페이지
} else {
    navigate('/profile-setup');  // 신규 사용자 → 프로필 입력 페이지
}

// 또는 서버가 제공한 redirectUrl 사용
navigate(data.redirectUrl);
```

**제어권의 차이**:

| 방식 | 제어 주체 | 리다이렉트 방법 |
|------|----------|---------------|
| **현재 (Server Redirect)** | 서버 | `getRedirectStrategy().sendRedirect()` → 브라우저가 자동 이동 |
| **JSON 방식 (Client Redirect)** | 프론트 | 프론트가 JSON 분석 → `navigate()`/`window.location.href` 호출 |

**JSON 방식의 장점**:
- 프론트가 추가 로직 실행 가능 (로딩 스피너, 분석 이벤트 전송 등)
- 조건 분기를 프론트에서 제어 (더 유연함)
- URL에 토큰 노출 안 됨 (보안 향상)

---

## 결론

**단기적으로는** URL에서 토큰을 즉시 제거하는 프론트엔드 로직과 CSRF 보호 활성화가 **필수**입니다. 이 두 가지만으로도 현재 방식의 보안 위험을 크게 줄일 수 있습니다.

**장기적으로는** JSON 응답 방식으로 전환하는 것이 **강력히 권장**됩니다. RFC 6750 표준을 준수하고, REST API 원칙과 SPA 아키텍처에 맞으며, 보안을 근본적으로 향상시킵니다. 구현 복잡도가 높지만, 프로덕션 환경의 보안과 확장성을 고려하면 투자할 가치가 충분합니다.

**현재 선택지**:
- **빠른 출시 우선**: 단기 해결책 적용 (URL 토큰 제거 + CSRF 활성화)
- **보안 우선**: JSON 응답 방식으로 리팩토링 (권장)

사용자의 프로젝트 일정과 우선순위에 따라 선택하시면 됩니다.
