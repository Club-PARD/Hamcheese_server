# OAuth2 로그인 플로우 - 빠른 참고 가이드

## 🎯 5단계 요약

```
1️⃣ 프론트 → 백엔드
   사용자 "구글 로그인" 클릭
   → GET /oauth2/authorization/google

2️⃣ 백엔드 → 구글 (자동)
   Spring Security가 자동으로 리다이렉트
   → https://accounts.google.com/o/oauth2/...

3️⃣ 사용자 ↔ 구글
   구글 로그인 페이지 (사용자 로그인 & 권한 허가)

4️⃣ 구글 → 백엔드 (자동)
   인증 코드 전달
   → GET /login/oauth2/code/google?code=xxx

5️⃣ 백엔드 → 프론트 (자동)
   토큰 발급 후 리다이렉트
   → http://localhost:3000/?token=<accessToken>&view=...
```

---

## 🔐 토큰 발급 프로세스

```
구글 API 호출 (자동)
    ↓
사용자 정보 추출 (email, name)
    ↓
DB 저장 또는 업데이트
    ↓
RefreshToken 발급 (14일)
    ├─ DB에 저장
    └─ HttpOnly 쿠키에 저장
    ↓
AccessToken 발급 (1분)
    └─ URL 쿼리 파라미터로 전달
    ↓
프론트: localStorage에 저장
    ↓
API 요청 시 Authorization 헤더에 포함
    Authorization: Bearer <accessToken>
```

---

## 📝 프론트엔드가 할 일

### 1. 로그인 시작
```javascript
// 구글 로그인 버튼 클릭 핸들러
const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
};
```

### 2. 리다이렉트 후 토큰 추출
```javascript
// http://localhost:3000/?view=setup&token=eyJhbGc... 로 리다이렉트됨

useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const accessToken = params.get('token');
    const view = params.get('view');

    if (accessToken) {
        // localStorage에 저장
        localStorage.setItem('accessToken', accessToken);

        // view에 따라 페이지 분기
        if (view === 'setup') {
            // 신규 사용자: 프로필 입력 페이지로
            navigate('/profile-setup');
        } else if (view === 'feed') {
            // 기존 사용자: 메인 페이지로
            navigate('/feed');
        }
    }
}, []);
```

### 3. API 요청할 때 토큰 포함
```javascript
const apiCall = async (endpoint) => {
    const accessToken = localStorage.getItem('accessToken');

    const response = await fetch(`http://localhost:8080${endpoint}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json'
        }
    });

    if (response.status === 401) {
        // AccessToken 만료 → 갱신
        await refreshAccessToken();
        // 원래 요청 재시도
    }

    return response.json();
};
```

### 4. AccessToken 갱신 (만료 시)
```javascript
const refreshAccessToken = async () => {
    const response = await fetch('http://localhost:8080/api/token', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include', // ← 중요! 쿠키 자동 포함
        body: JSON.stringify({
            refreshToken: '' // 실제로는 쿠키에서 자동 전송됨
        })
    });

    if (response.ok) {
        const { accessToken } = await response.json();
        localStorage.setItem('accessToken', accessToken);
        return true;
    }

    // 갱신 실패 → 로그아웃
    logout();
};
```

### 5. 로그아웃
```javascript
const handleLogout = async () => {
    const accessToken = localStorage.getItem('accessToken');

    // 서버에서 RefreshToken 삭제
    await fetch('http://localhost:8080/api/refresh-token', {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        },
        credentials: 'include' // 쿠키 포함
    });

    // 로컬에서 AccessToken 삭제
    localStorage.removeItem('accessToken');

    // 로그인 페이지로 리다이렉트
    window.location.href = '/';
};
```

---

## 🔑 백엔드 구현 파일 위치

| 역할 | 파일 | 주요 메서드 |
|-----|------|-----------|
| Security 설정 | `config/WebOAuthSecurityConfig.java` | `filterChain()` |
| OAuth2 처리 | `config/oauth/OAuth2UserCustomService.java` | `loadUser()` |
| 로그인 성공 | `config/oauth/OAuth2SuccessHandler.java` | `onAuthenticationSuccess()` |
| 토큰 생성 | `config/jwt/TokenProvider.java` | `generateToken()` |
| 토큰 검증 | `config/TokenAuthenticationFilter.java` | `doFilterInternal()` |
| 토큰 갱신 | `config/jwt/token/TokenApiController.java` | `createNewAccessToken()` |

---

## ⚙️ 설정 파일 (`application.yaml`)

### Google OAuth2 설정 필수
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: "YOUR_CLIENT_ID.apps.googleusercontent.com"
            client-secret: "YOUR_CLIENT_SECRET"
            redirect-uri: "http://localhost:8080/login/oauth2/code/google"
            scope: email, profile

app:
  jwt:
    issuer: "mate-check"
    secret-key: "your-secret-key-at-least-32-characters-long!!!!"
```

---

## 🎯 API 엔드포인트

### 로그인 (프론트에서 호출)
```
GET /oauth2/authorization/google
→ 구글 로그인 페이지로 리다이렉트
```

### 콜백 (구글에서 호출, 사용자가 직접 호출하지 않음)
```
GET /login/oauth2/code/google?code=xxx&state=yyy
→ 토큰 발급 후 프론트로 리다이렉트
```

### 토큰 갱신 (AccessToken 만료 시)
```
POST /api/token
Content-Type: application/json

{
  "refreshToken": "xxx" // 선택사항, 쿠키에서 자동 전송
}

Response:
{
  "accessToken": "eyJhbGc..."
}
```

### 로그아웃
```
DELETE /api/refresh-token

Response: 200 OK
```

---

## 🔒 보안 체크리스트

- [ ] Google Client ID & Secret 설정됨
- [ ] JWT Secret Key 32자 이상
- [ ] Redirect URI를 Google 콘솔에 등록
- [ ] CORS 설정에서 프론트 도메인 확인
- [ ] HTTPS 설정 (운영 환경)
- [ ] RefreshToken은 HttpOnly 쿠키 사용
- [ ] AccessToken은 localStorage 사용
- [ ] CSRF 보호 활성화 권장

---

## ❌ 자주 하는 실수

### 1. Redirect URI 불일치
```
❌ 백엔드: http://localhost:8080/login/oauth2/code/google
✅ Google 콘솔: http://localhost:8080/login/oauth2/code/google
(정확하게 일치해야 함)
```

### 2. AccessToken을 쿠키에 저장
```javascript
❌ document.cookie = `token=${accessToken}`
✅ localStorage.setItem('accessToken', accessToken)
(localStorage/sessionStorage 사용, HTTPS 환경에서 쿠키는 RefreshToken만)
```

### 3. API 요청 시 토큰 미포함
```javascript
❌ await fetch('/user/myProfile/123')

✅ await fetch('/user/myProfile/123', {
    headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
    }
})
```

### 4. 쿠키 자동 전송 미설정
```javascript
❌ await fetch('/api/token', {
    method: 'POST',
    body: JSON.stringify({...})
})

✅ await fetch('/api/token', {
    method: 'POST',
    credentials: 'include',  // ← 쿠키 자동 전송
    body: JSON.stringify({...})
})
```

### 5. CORS Credentials 미설정
```java
❌ config.setAllowCredentials(false);

✅ config.setAllowCredentials(true);  // 쿠키 허용
```

---

## 📊 흐름 다이어그램 (아스키 아트)

```
시간 →

USER                    FRONTEND                BACKEND                 GOOGLE
 │                         │                        │                     │
 │                         │                        │                     │
 │  클릭 (로그인)           │                        │                     │
 │────────────────────────→│                        │                     │
 │                         │  /oauth2/auth/google  │                     │
 │                         │───────────────────────→│                     │
 │                         │                        │  Redirect to Google │
 │                         │←────────────────────────────────────────────→│
 │                         │                        │ (사용자 로그인)      │
 │  구글 로그인            │                        │                     │
 │─────────────────────────────────────────────────→│                     │
 │                        │                        │ Authorization Code  │
 │                         │←─────────────────────────────────────────────│
 │                         │  /login/oauth2/code/google?code=xxx         │
 │                         │←────────────────────────│                     │
 │                         │                        │  code + secret → access_token
 │                         │                        │────────────────────→│
 │                         │                        │←────────────────────│
 │                         │                        │ User Info           │
 │                         │                        │───────────────────→│
 │                         │                        │←───────────────────│
 │                         │                        │ email, name        │
 │                         │                        │ (DB 저장/갱신)     │
 │                         │  token + redirect URL │                     │
 │                         │←────────────────────────│                     │
 │  리다이렉트             │                        │                     │
 │←────────────────────────│                        │                     │
 │  (메인 또는 설정 페이지) │                        │                     │
 │                         │                        │                     │
```

---

## 🚀 로컬 테스트 가이드

### 1. 백엔드 시작
```bash
./gradlew bootRun
# http://localhost:8080 에서 시작
```

### 2. 프론트엔드 시작
```bash
npm start
# http://localhost:3000 에서 시작
```

### 3. Swagger UI에서 테스트
```
http://localhost:8080/swagger-ui/index.html
```

### 4. 로그인 테스트
- 프론트에서 "구글 로그인" 버튼 클릭
- 구글 계정으로 로그인
- 프론트의 콘솔에서 AccessToken 확인
- `localStorage.getItem('accessToken')` 확인

---

## 🆘 트러블슈팅

### "Redirect URI mismatch"
```
원인: Google 콘솔의 Redirect URI와 application.yaml이 다름
해결: 정확하게 맞춰주기
```

### "Invalid client secret"
```
원인: 잘못된 Client ID/Secret
해결: Google 콘솔에서 다시 복사해서 설정
```

### "CORS error"
```
원인: 프론트 도메인이 CORS 설정에 없음
해결: WebOAuthSecurityConfig.corsConfigurationSource() 에서 도메인 추가
```

### "401 Unauthorized on API"
```
원인1: AccessToken이 없거나 잘못됨
       → Authorization 헤더 확인

원인2: AccessToken이 만료됨
       → /api/token 으로 갱신
```

### "RefreshToken not found"
```
원인: 쿠키가 자동 전송되지 않음
해결: fetch의 credentials: 'include' 확인
```
