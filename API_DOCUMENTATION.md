# 🚀 Longkathon API 명세서

> **프론트엔드 개발자를 위한 완전한 API 가이드**

## 📋 목차
1. [기본 정보](#기본-정보)
2. [인증 및 로그인 플로우](#인증-및-로그인-플로우)
3. [REST API 엔드포인트](#rest-api-엔드포인트)
4. [WebSocket 채팅 API](#websocket-채팅-api)
5. [에러 처리](#에러-처리)

---

## 기본 정보

### Base URL
```
http://localhost:8080
```

### CORS 설정
- **Allowed Origin**: `http://localhost:3000`
- **Credentials**: `true` (쿠키 포함)
- **Allowed Methods**: `GET, POST, PUT, PATCH, DELETE, OPTIONS`

### 인증 방식
- **OAuth2**: Google 로그인
- **JWT**: Access Token (Header) + Refresh Token (HTTP-only Cookie)

### 공통 헤더

**인증이 필요한 요청:**
```http
Authorization: Bearer {access_token}
Content-Type: application/json
```

**파일 업로드 요청:**
```http
Authorization: Bearer {access_token}
Content-Type: multipart/form-data
```

---

## 인증 및 로그인 플로우

### 🔐 OAuth2 Google 로그인 전체 플로우

#### 1단계: 로그인 시작
프론트엔드에서 사용자가 "Google 로그인" 버튼을 클릭하면:

```javascript
// 사용자를 구글 로그인 페이지로 리다이렉트
window.location.href = 'http://localhost:8080/oauth2/authorization/google';
```

#### 2단계: 구글 인증 후 콜백
구글 인증이 완료되면 서버가 다음 중 하나의 URL로 리다이렉트합니다:

**신규 회원 (프로필 미완성):**
```
http://localhost:3000/?view=setup&token={access_token}
```

**기존 회원 (프로필 완성됨):**
```
http://localhost:3000/?view=feed&token={access_token}
```

#### 3단계: 토큰 저장 및 사용

```javascript
// URL에서 access token 추출
const urlParams = new URLSearchParams(window.location.search);
const accessToken = urlParams.get('token');

// localStorage에 저장
localStorage.setItem('access_token', accessToken);

// 이후 모든 API 요청에 포함
const headers = {
  'Authorization': `Bearer ${accessToken}`,
  'Content-Type': 'application/json'
};

// Refresh token은 자동으로 HTTP-only 쿠키에 저장됨 (JavaScript에서 접근 불가)
```

#### 4단계: Access Token 갱신

Access Token이 만료되면 (401 응답 시):

**Request:**
```http
POST /api/token
Content-Type: application/json

{
  "refreshToken": "refresh_token_from_cookie"
}
```

**Response:**
```json
{
  "accessToken": "new_access_token_here"
}
```

**프론트엔드 구현 예시:**
```javascript
// axios interceptor 예시
axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    if (error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Refresh token은 쿠키에 자동으로 포함됨
        const response = await axios.post('/api/token', {
          refreshToken: '' // 쿠키에서 자동으로 가져옴
        });

        const newAccessToken = response.data.accessToken;
        localStorage.setItem('access_token', newAccessToken);

        // 원래 요청 재시도
        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
        return axios(originalRequest);
      } catch (refreshError) {
        // Refresh token도 만료됨 - 다시 로그인 필요
        localStorage.removeItem('access_token');
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  }
);
```

#### 5단계: 로그아웃

**Request:**
```http
DELETE /api/refresh-token
Authorization: Bearer {access_token}
```

**Response:**
```
200 OK
```

**프론트엔드 구현:**
```javascript
async function logout() {
  try {
    await axios.delete('/api/refresh-token', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('access_token')}`
      }
    });
  } catch (error) {
    console.error('Logout failed', error);
  } finally {
    // 로컬 토큰 제거
    localStorage.removeItem('access_token');
    // 로그인 페이지로 이동
    window.location.href = '/login';
  }
}
```

---

## REST API 엔드포인트

### 1️⃣ 토큰 관리 API

#### 1.1 Access Token 갱신
```http
POST /api/token
```

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

**Response:** `201 Created`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**설명:** Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.

---

#### 1.2 로그아웃 (Refresh Token 삭제)
```http
DELETE /api/refresh-token
Authorization: Bearer {access_token}
```

**Response:** `200 OK`

**설명:** DB에서 Refresh Token을 삭제하고 쿠키를 만료시킵니다.

---

### 2️⃣ 사용자 관리 API

#### 2.1 프로필 소유자 확인
```http
GET /users/equal/{userId}
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `userId` (String): 확인할 사용자 ID

**Response:** `200 OK`
```json
true
```
또는
```json
false
```

**설명:** 현재 로그인한 사용자가 해당 프로필의 소유자인지 확인합니다.

---

#### 2.2 메이트 프로필 조회
```http
GET /users/mateProfile/{userId}
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `userId` (Long): 조회할 사용자 ID

**Response:** `200 OK`
```json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "department": "소프트웨어학부",
  "firstMajor": "컴퓨터공학",
  "secondMajor": "경영학",
  "gpa": "4.2",
  "studentId": "2021123456",
  "semester": "6",
  "imageUrl": "https://s3.amazonaws.com/profile/image.jpg",
  "grade": "3",
  "introduction": "안녕하세요! 백엔드 개발에 관심이 많습니다.",
  "skillList": [
    "Java",
    "Spring Boot",
    "MySQL",
    "AWS"
  ],
  "activity": [
    {
      "year": 2023,
      "title": "PARD 5기 Server Part",
      "link": "https://we-pard.com"
    },
    {
      "year": 2024,
      "title": "해커톤 대상 수상",
      "link": "https://example.com"
    }
  ],
  "peerGoodKeyword": {
    "책임감": 5,
    "커뮤니케이션": 3,
    "시간약속": 4,
    "협업능력": 6
  },
  "goodKeywordCount": 18,
  "peerBadKeyword": {
    "지각": 1,
    "의견무시": 2
  },
  "badKeywordCount": 3,
  "peerReviewRecent": [
    {
      "startDate": "2024-03-15",
      "meetSpecific": "캡스톤 디자인 프로젝트",
      "goodKeywordList": ["책임감", "협업능력"],
      "badKeywordList": []
    }
  ]
}
```

**필드 설명:**
| 필드 | 타입 | 설명 |
|-----|------|------|
| `name` | String | 사용자 이름 |
| `email` | String | 이메일 (OAuth2에서 가져옴) |
| `department` | String | 학부 |
| `firstMajor` | String | 제1전공 |
| `secondMajor` | String | 제2전공 (없으면 null) |
| `gpa` | String | 학점 (예: "4.2") |
| `studentId` | String | 학번 |
| `semester` | String | 학기 (예: "6") |
| `imageUrl` | String | 프로필 이미지 URL |
| `grade` | String | 학년 (예: "3") |
| `introduction` | String | 자기소개 |
| `skillList` | String[] | 보유 기술 목록 |
| `activity` | ActivityDTO[] | 활동 내역 |
| `peerGoodKeyword` | Object | 긍정 키워드 및 개수 |
| `goodKeywordCount` | Integer | 받은 긍정 키워드 총 개수 |
| `peerBadKeyword` | Object | 부정 키워드 및 개수 |
| `badKeywordCount` | Integer | 받은 부정 키워드 총 개수 |
| `peerReviewRecent` | PeerReviewDTO[] | 최근 동료 평가 목록 |

---

#### 2.3 내 프로필 조회
```http
GET /users/myProfile
Authorization: Bearer {access_token}
```

**Response:** `200 OK`
```json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "department": "소프트웨어학부",
  "firstMajor": "컴퓨터공학",
  "secondMajor": "경영학",
  "gpa": "4.2",
  "studentId": "2021123456",
  "grade": "3",
  "semester": "6",
  "imageUrl": "https://s3.amazonaws.com/profile/image.jpg",
  "introduction": "안녕하세요! 백엔드 개발에 관심이 많습니다.",
  "skillList": ["Java", "Spring Boot", "MySQL"],
  "activity": [
    {
      "year": 2023,
      "title": "PARD 5기",
      "link": "https://we-pard.com"
    }
  ]
}
```

**설명:** 현재 로그인한 사용자의 프로필을 조회합니다. (동료평가 제외)

---

#### 2.4 회원가입 (프로필 생성)
```http
PATCH /users/create
Authorization: Bearer {access_token}
Content-Type: multipart/form-data
```

**Request Body (multipart/form-data):**
```javascript
const formData = new FormData();

// 프로필 이미지 (선택사항)
formData.append('profileImage', imageFile); // File 객체

// 사용자 정보 (JSON 문자열)
const userData = {
  "name": "홍길동",
  "studentId": "2021123456",
  "grade": "3",
  "semester": "6",
  "department": "소프트웨어학부",
  "firstMajor": "컴퓨터공학",
  "secondMajor": "경영학",
  "phoneNumber": "010-1234-5678",
  "gpa": "4.2"
};
formData.append('data', JSON.stringify(userData));

// 전송
await axios.patch('/users/create', formData, {
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'multipart/form-data'
  }
});
```

**Request JSON 필드:**
| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| `name` | String | ✅ | 이름 |
| `studentId` | String | ✅ | 학번 |
| `grade` | String | ✅ | 학년 ("1", "2", "3", "4") |
| `semester` | String | ✅ | 학기 ("1"~"8") |
| `department` | String | ✅ | 학부 |
| `firstMajor` | String | ✅ | 제1전공 |
| `secondMajor` | String | ❌ | 제2전공 |
| `phoneNumber` | String | ✅ | 전화번호 |
| `gpa` | String | ✅ | 학점 (예: "4.2") |

**Response:** `200 OK`
```json
{
  "name": "홍길동",
  "imageUrl": "https://s3.amazonaws.com/profile/image.jpg"
}
```

---

#### 2.5 프로필 사진 삭제
```http
DELETE /users/myProfile
Authorization: Bearer {access_token}
```

**Response:** `200 OK`

**설명:** 프로필 사진을 S3와 DB에서 삭제합니다.

---

#### 2.6 프로필 사진 업데이트
```http
POST /users/updateImage
Authorization: Bearer {access_token}
Content-Type: multipart/form-data
```

**Request Body:**
```javascript
const formData = new FormData();
formData.append('profileImage', imageFile); // File 객체

await axios.post('/users/updateImage', formData, {
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'multipart/form-data'
  }
});
```

**Response:** `200 OK`

**설명:** 기존 프로필 사진을 삭제하고 새 사진으로 업데이트합니다.

---

#### 2.7 프로필 정보 수정
```http
PATCH /users/update
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "department": "소프트웨어학부",
  "firstMajor": "컴퓨터공학",
  "secondMajor": "경영학",
  "gpa": "4.3",
  "studentId": "2021123456",
  "grade": "3",
  "semester": "6",
  "imageUrl": "https://s3.amazonaws.com/profile/image.jpg",
  "introduction": "업데이트된 자기소개입니다.",
  "skillList": ["Java", "Spring Boot", "React", "AWS"],
  "activity": [
    {
      "year": 2024,
      "title": "PARD 6기 Server Part Leader",
      "link": "https://we-pard.com"
    }
  ]
}
```

**Response:** `200 OK`

**설명:** 프로필 정보를 수정합니다. (이미지 제외)

---

#### 2.8 내 동료평가 조회
```http
GET /users/myPeerReview
Authorization: Bearer {access_token}
```

**Response:** `200 OK`
```json
{
  "peerGoodKeyword": {
    "책임감": 5,
    "커뮤니케이션": 3,
    "시간약속": 4
  },
  "goodKeywordCount": 12,
  "peerBadKeyword": {
    "지각": 1
  },
  "badKeywordCount": 1,
  "peerReviewRecent": [
    {
      "startDate": "2024-03-15",
      "meetSpecific": "캡스톤 디자인 프로젝트",
      "goodKeywordList": ["책임감", "협업능력"],
      "badKeywordList": []
    },
    {
      "startDate": "2024-01-10",
      "meetSpecific": "웹 개발 팀 프로젝트",
      "goodKeywordList": ["커뮤니케이션"],
      "badKeywordList": ["지각"]
    }
  ]
}
```

---

#### 2.9 전체 메이트 조회
```http
GET /users/findAll
```

**🔓 인증 불필요**

**Response:** `200 OK`
```json
[
  {
    "userId": 1,
    "name": "홍길동",
    "firstMajor": "컴퓨터공학",
    "secondMajor": "경영학",
    "studentId": "2021123456",
    "introduction": "안녕하세요!",
    "skillList": ["Java", "Spring"],
    "peerGoodKeywords": {
      "책임감": 5,
      "협업능력": 3
    },
    "imageUrl": "https://s3.amazonaws.com/profile/image1.jpg",
    "goodKeywordCount": 8
  },
  {
    "userId": 2,
    "name": "김철수",
    "firstMajor": "전자공학",
    "secondMajor": null,
    "studentId": "2022654321",
    "introduction": "반갑습니다!",
    "skillList": ["Python", "AI"],
    "peerGoodKeywords": {
      "시간약속": 4
    },
    "imageUrl": "https://s3.amazonaws.com/profile/image2.jpg",
    "goodKeywordCount": 4
  }
]
```

---

#### 2.10 메이트 필터링 조회
```http
GET /users/filter?departments=컴퓨터공학,전자공학&name=홍
```

**🔓 인증 불필요**

**Query Parameters:**
- `departments` (String[]): 학과 목록 (쉼표로 구분)
- `name` (String): 이름 검색어

**예시:**
```javascript
// axios 예시
const response = await axios.get('/users/filter', {
  params: {
    departments: ['컴퓨터공학', '전자공학'],
    name: '홍'
  },
  paramsSerializer: params => {
    return qs.stringify(params, { arrayFormat: 'comma' });
  }
});
```

**Response:** `200 OK`
```json
[
  {
    "userId": 1,
    "name": "홍길동",
    "firstMajor": "컴퓨터공학",
    "secondMajor": "경영학",
    "studentId": "2021123456",
    "introduction": "안녕하세요!",
    "skillList": ["Java", "Spring"],
    "peerGoodKeywords": {
      "책임감": 5
    },
    "imageUrl": "https://s3.amazonaws.com/profile/image.jpg",
    "goodKeywordCount": 8
  }
]
```

---

#### 2.11 첫 페이지 데이터 조회
```http
GET /users/firstPage
Authorization: Bearer {access_token}
```

**Response:** `200 OK`
```json
{
  "profileFeedList": [
    {
      "userId": 1,
      "name": "홍길동",
      "firstMajor": "컴퓨터공학",
      "secondMajor": "경영학",
      "studentId": "2021123456",
      "introduction": "안녕하세요!",
      "skillList": ["Java", "Spring"],
      "peerGoodKeywords": {
        "책임감": 5
      },
      "imageUrl": "https://s3.amazonaws.com/profile/image.jpg",
      "goodKeywordCount": 8
    }
  ],
  "recruitingFeedList": [
    {
      "recruitingId": 1,
      "name": "홍길동",
      "projectType": "수업",
      "projectSpecific": "웹프로그래밍",
      "classes": "01분반",
      "topic": "쇼핑몰 웹사이트",
      "totalPeople": 4,
      "recruitPeople": 2,
      "title": "프론트엔드 개발자 구합니다"
    }
  ]
}
```

**설명:** 서비스 소개 페이지에 표시할 프로필 피드와 모집글 피드를 반환합니다.

---

### 3️⃣ 동료평가 API

#### 3.1 동료평가 작성
```http
POST /peerReview/{userId}
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Path Parameters:**
- `userId` (Long): 평가할 사용자 ID

**Request Body:**
```json
{
  "startDate": "2024-03-15",
  "meetSpecific": "캡스톤 디자인 프로젝트",
  "goodKeywordList": [
    "책임감",
    "커뮤니케이션",
    "시간약속"
  ],
  "badKeywordList": [
    "의견무시"
  ]
}
```

**필드 설명:**
| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| `startDate` | String | ✅ | 협업 시작 날짜 (YYYY-MM-DD) |
| `meetSpecific` | String | ✅ | 협업 내용 (프로젝트명 등) |
| `goodKeywordList` | String[] | ✅ | 긍정 키워드 목록 |
| `badKeywordList` | String[] | ✅ | 부정 키워드 목록 (빈 배열 가능) |

**Response:** `200 OK`

---

### 4️⃣ 모집글 API

#### 4.1 전체 모집글 조회
```http
GET /recruiting/findAll
```

**🔓 인증 불필요**

**Response:** `200 OK`
```json
[
  {
    "recruitingId": 1,
    "name": "홍길동",
    "projectType": "수업",
    "projectSpecific": "웹프로그래밍",
    "classes": "01분반",
    "topic": "쇼핑몰 웹사이트",
    "totalPeople": 4,
    "recruitPeople": 2,
    "title": "프론트엔드 개발자 구합니다",
    "myKeyword": ["React", "TypeScript", "디자인"],
    "date": "2024-03-15"
  },
  {
    "recruitingId": 2,
    "name": "김철수",
    "projectType": "공모전",
    "projectSpecific": "2024 빅데이터 해커톤",
    "classes": null,
    "topic": "AI 기반 추천 시스템",
    "totalPeople": 5,
    "recruitPeople": 3,
    "title": "AI 개발자 급구!",
    "myKeyword": ["Python", "TensorFlow", "협업"],
    "date": "2024-03-20"
  }
]
```

**필드 설명:**
| 필드 | 타입 | 설명 |
|-----|------|------|
| `recruitingId` | Long | 모집글 ID |
| `name` | String | 작성자 이름 |
| `projectType` | String | 프로젝트 유형 ("수업", "공모전", "사이드프로젝트" 등) |
| `projectSpecific` | String | 구체적인 이름 (수업명, 공모전명 등) |
| `classes` | String | 분반 (수업일 경우) |
| `topic` | String | 주제 |
| `totalPeople` | Integer | 전체 인원 |
| `recruitPeople` | Integer | 모집 인원 |
| `title` | String | 제목 |
| `myKeyword` | String[] | 키워드 목록 |
| `date` | String | 작성일 |

---

#### 4.2 모집글 상세 조회
```http
GET /recruiting/{recruitingId}
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `recruitingId` (Long): 모집글 ID

**Response:** `200 OK`
```json
{
  "name": "홍길동",
  "projectType": "수업",
  "projectSpecific": "웹프로그래밍",
  "classes": "01분반",
  "topic": "쇼핑몰 웹사이트",
  "totalPeople": 4,
  "recruitPeople": 2,
  "title": "프론트엔드 개발자 구합니다",
  "context": "웹프로그래밍 수업 팀 프로젝트로 쇼핑몰 웹사이트를 만들려고 합니다. React를 사용할 예정이며, 디자인에도 관심이 있으신 분을 찾습니다!",
  "studentId": "2021123456",
  "firstMajor": "컴퓨터공학",
  "secondMajor": "경영학",
  "imageUrl": "https://s3.amazonaws.com/profile/image.jpg",
  "myKeyword": ["React", "TypeScript", "디자인"],
  "date": "2024-03-15",
  "postingList": [
    {
      "recruitingId": 3,
      "name": "홍길동",
      "projectType": "수업",
      "totalPeople": 3,
      "recruitPeople": 1,
      "title": "모바일앱 팀원 구합니다",
      "date": "2024-02-10"
    }
  ],
  "canEdit": true
}
```

**필드 설명:**
| 필드 | 타입 | 설명 |
|-----|------|------|
| `context` | String | 모집글 내용 (상세 설명) |
| `studentId` | String | 작성자 학번 |
| `firstMajor` | String | 작성자 제1전공 |
| `secondMajor` | String | 작성자 제2전공 |
| `imageUrl` | String | 작성자 프로필 이미지 |
| `postingList` | Array | 작성자의 최근 게시글 목록 |
| `canEdit` | Boolean | 현재 사용자가 수정 가능한지 여부 |

---

#### 4.3 모집글 필터링 조회
```http
GET /recruiting/filter?type=수업,공모전&departments=컴퓨터공학&name=홍
```

**🔓 인증 불필요**

**Query Parameters:**
- `type` (String[]): 프로젝트 유형 목록
- `departments` (String[]): 학과 목록
- `name` (String): 작성자 이름 검색어

**예시:**
```javascript
const response = await axios.get('/recruiting/filter', {
  params: {
    type: ['수업', '공모전'],
    departments: ['컴퓨터공학'],
    name: '홍'
  }
});
```

**Response:** `200 OK`
```json
[
  {
    "recruitingId": 1,
    "name": "홍길동",
    "projectType": "수업",
    "projectSpecific": "웹프로그래밍",
    "classes": "01분반",
    "topic": "쇼핑몰 웹사이트",
    "totalPeople": 4,
    "recruitPeople": 2,
    "title": "프론트엔드 개발자 구합니다",
    "myKeyword": ["React", "TypeScript"],
    "date": "2024-03-15"
  }
]
```

---

#### 4.4 내 모집글 조회
```http
GET /recruiting
Authorization: Bearer {access_token}
```

**Response:** `200 OK`
```json
[
  {
    "recruitingId": 1,
    "name": "홍길동",
    "projectType": "수업",
    "projectSpecific": "웹프로그래밍",
    "classes": "01분반",
    "topic": "쇼핑몰 웹사이트",
    "totalPeople": 4,
    "recruitPeople": 2,
    "title": "프론트엔드 개발자 구합니다",
    "myKeyword": ["React", "TypeScript"],
    "date": "2024-03-15"
  }
]
```

---

#### 4.5 모집글 작성
```http
POST /recruiting
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "projectType": "수업",
  "projectSpecific": "웹프로그래밍",
  "classes": "01분반",
  "topic": "쇼핑몰 웹사이트",
  "totalPeople": 4,
  "recruitPeople": 2,
  "title": "프론트엔드 개발자 구합니다",
  "context": "웹프로그래밍 수업 팀 프로젝트로 쇼핑몰 웹사이트를 만들려고 합니다.",
  "myKeyword": [
    "React",
    "TypeScript",
    "디자인"
  ]
}
```

**필드 설명:**
| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| `projectType` | String | ✅ | 프로젝트 유형 |
| `projectSpecific` | String | ✅ | 구체적인 이름 |
| `classes` | String | ❌ | 분반 (수업일 경우) |
| `topic` | String | ✅ | 주제 |
| `totalPeople` | Integer | ✅ | 전체 인원 |
| `recruitPeople` | Integer | ✅ | 모집 인원 |
| `title` | String | ✅ | 제목 |
| `context` | String | ✅ | 내용 |
| `myKeyword` | String[] | ✅ | 키워드 목록 |

**Response:** `200 OK`

---

#### 4.6 모집글 수정
```http
PATCH /recruiting/{recruitingId}
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Path Parameters:**
- `recruitingId` (Long): 모집글 ID

**Request Body:**
```json
{
  "projectType": "수업",
  "projectSpecific": "웹프로그래밍",
  "classes": "01분반",
  "topic": "쇼핑몰 웹사이트 (업데이트)",
  "totalPeople": 5,
  "recruitPeople": 3,
  "title": "프론트엔드/백엔드 개발자 구합니다",
  "context": "업데이트된 내용입니다.",
  "keyword": [
    "React",
    "TypeScript",
    "Spring Boot"
  ]
}
```

**참고:** 수정 시에는 `myKeyword` 대신 `keyword` 필드명을 사용합니다.

**Response:** `200 OK`

---

#### 4.7 모집글 삭제
```http
DELETE /recruiting/{recruitingId}
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `recruitingId` (Long): 모집글 ID

**Response:** `200 OK`

---

### 5️⃣ 찌르기 (Poking) API

#### 5.1 모집글에서 찌르기
```http
POST /poking/{recruitingId}
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `recruitingId` (Long): 모집글 ID

**Response:** `200 OK`
```json
{
  "pokingId": 1,
  "name": "홍길동"
}
```

**설명:** 모집글 작성자에게 찌르기를 보냅니다.

---

#### 5.2 유저 프로필에서 찌르기
```http
POST /poking/user/{userId}
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `userId` (Long): 사용자 ID

**Response:** `200 OK`
```json
{
  "pokingId": 2,
  "name": "김철수"
}
```

**설명:** 특정 사용자에게 직접 찌르기를 보냅니다.

---

#### 5.3 찌르기 가능 여부 확인 (모집글)
```http
GET /poking/{recruitingId}
Authorization: Bearer {access_token}
```

⚠️ **경고**: 이 엔드포인트는 `/poking/{userId}`와 경로 충돌이 있습니다.
실제 사용 시 서버 측에서 어떤 것이 우선되는지 확인이 필요합니다.

**Response:** `200 OK`
```json
{
  "canPoke": true,
  "reason": "OK"
}
```

또는

```json
{
  "canPoke": false,
  "reason": "ALREADY_POKED"
}
```

**가능한 reason 값:**
- `OK`: 찌르기 가능
- `SELF`: 본인에게는 찌를 수 없음
- `ALREADY_POKED`: 이미 찌르기를 보냄
- `USER_NOT_FOUND`: 사용자를 찾을 수 없음

---

#### 5.4 받은 찌르기 목록 조회
```http
GET /poking
Authorization: Bearer {access_token}
```

**Response:** `200 OK`
```json
[
  {
    "pokingId": 1,
    "recruitingId": 5,
    "senderId": 3,
    "projectSpecific": "웹프로그래밍",
    "senderName": "김철수",
    "date": "2024-03-20",
    "imageUrl": "https://s3.amazonaws.com/profile/sender.jpg"
  },
  {
    "pokingId": 2,
    "recruitingId": null,
    "senderId": 7,
    "projectSpecific": null,
    "senderName": "이영희",
    "date": "2024-03-19",
    "imageUrl": "https://s3.amazonaws.com/profile/sender2.jpg"
  }
]
```

**필드 설명:**
| 필드 | 타입 | 설명 |
|-----|------|------|
| `pokingId` | Long | 찌르기 ID |
| `recruitingId` | Long | 모집글 ID (프로필에서 보낸 경우 null) |
| `senderId` | Long | 보낸 사람 ID |
| `projectSpecific` | String | 프로젝트명 (모집글에서 보낸 경우) |
| `senderName` | String | 보낸 사람 이름 |
| `date` | String | 날짜 |
| `imageUrl` | String | 보낸 사람 프로필 이미지 |

---

#### 5.5 찌르기 응답/삭제
```http
DELETE /poking/{pokingId}
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Path Parameters:**
- `pokingId` (Long): 찌르기 ID

**Request Body:**
```json
{
  "ok": true
}
```

**필드 설명:**
- `ok` (Boolean): `true` = 수락, `false` = 거절

**Response:** `200 OK`

**설명:**
- `ok: true` → 알림(Alarm) 생성 + 찌르기 삭제
- `ok: false` → 찌르기만 삭제

---

### 6️⃣ 알림 API

#### 6.1 알림 목록 조회
```http
GET /alarm
Authorization: Bearer {access_token}
```

**Response:** `200 OK`
```json
[
  {
    "alarmId": 1,
    "senderName": "김철수",
    "ok": true
  },
  {
    "alarmId": 2,
    "senderName": "이영희",
    "ok": false
  }
]
```

**필드 설명:**
| 필드 | 타입 | 설명 |
|-----|------|------|
| `alarmId` | Long | 알림 ID |
| `senderName` | String | 보낸 사람 이름 |
| `ok` | Boolean | 수락 여부 (true: 수락, false: 거절) |

---

#### 6.2 알림 삭제
```http
DELETE /alarm/{alarmId}
Authorization: Bearer {access_token}
```

**Path Parameters:**
- `alarmId` (Long): 알림 ID

**Response:** `200 OK`

**설명:** 알림을 확인하고 삭제합니다.

---

### 7️⃣ 채팅방 REST API

#### 7.1 채팅방 생성/입장
```http
POST /v1/chatRoom
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "sellerId": 3
}
```

**필드 설명:**
- `sellerId` (Long): 대화 상대방의 사용자 ID

**Response:** `201 Created`
```json
{
  "chatRoomId": 1,
  "userId": 5,
  "sellerId": 3,
  "messages": [
    {
      "messageId": 1,
      "chatRoomId": 1,
      "senderId": 5,
      "content": "안녕하세요!",
      "nickname": "홍길동",
      "createdAt": "2024-03-20T10:30:00"
    },
    {
      "messageId": 2,
      "chatRoomId": 1,
      "senderId": 3,
      "content": "네 안녕하세요!",
      "nickname": "김철수",
      "createdAt": "2024-03-20T10:31:00"
    }
  ]
}
```

**필드 설명:**
| 필드 | 타입 | 설명 |
|-----|------|------|
| `chatRoomId` | Long | 채팅방 ID |
| `userId` | Long | 현재 사용자 ID |
| `sellerId` | Long | 대화 상대방 ID |
| `messages` | Array | 이전 메시지 목록 |
| `messages[].messageId` | Long | 메시지 ID |
| `messages[].senderId` | Long | 보낸 사람 ID |
| `messages[].content` | String | 메시지 내용 |
| `messages[].nickname` | String | 보낸 사람 닉네임 |
| `messages[].createdAt` | String | 생성 시간 (ISO 8601) |

**설명:**
- 두 사용자 간의 채팅방이 이미 존재하면 기존 채팅방을 반환합니다.
- 존재하지 않으면 새로운 채팅방을 생성합니다.
- 이전 메시지 목록도 함께 반환됩니다.

---

## WebSocket 채팅 API

### 🔌 WebSocket 연결 및 실시간 채팅 가이드

#### 1. 라이브러리 설치

```bash
npm install sockjs-client stompjs
# 또는
yarn add sockjs-client stompjs
```

#### 2. 전체 채팅 구현 예시 (React)

```javascript
import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import axios from 'axios';

function ChatRoom({ chatRoomId, userId, sellerId }) {
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [connected, setConnected] = useState(false);
  const stompClientRef = useRef(null);

  // 1. 채팅방 입장 (REST API)
  useEffect(() => {
    const enterChatRoom = async () => {
      try {
        const token = localStorage.getItem('access_token');
        const response = await axios.post(
          'http://localhost:8080/v1/chatRoom',
          { sellerId: sellerId },
          {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          }
        );

        // 이전 메시지 로드
        setMessages(response.data.messages);

        // WebSocket 연결 시작
        connectWebSocket(response.data.chatRoomId);
      } catch (error) {
        console.error('채팅방 입장 실패:', error);
      }
    };

    enterChatRoom();

    // cleanup: 컴포넌트 언마운트 시 WebSocket 연결 해제
    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.disconnect();
      }
    };
  }, [sellerId]);

  // 2. WebSocket 연결
  const connectWebSocket = (roomId) => {
    const token = localStorage.getItem('access_token');

    // SockJS 소켓 생성
    const socket = new SockJS('http://localhost:8080/chat/inbox');

    // STOMP 클라이언트 생성
    const stompClient = Stomp.over(socket);

    // 연결
    stompClient.connect(
      {
        // STOMP 헤더에 JWT 토큰 포함 (인증)
        Authorization: `Bearer ${token}`
      },
      (frame) => {
        console.log('WebSocket 연결 성공:', frame);
        setConnected(true);

        // 채팅방 채널 구독
        stompClient.subscribe(`/sub/channel/${roomId}`, (message) => {
          // 메시지 수신
          const receivedMessage = JSON.parse(message.body);
          console.log('메시지 수신:', receivedMessage);

          // 메시지를 상태에 추가
          setMessages((prevMessages) => [...prevMessages, receivedMessage]);
        });
      },
      (error) => {
        console.error('WebSocket 연결 실패:', error);
        setConnected(false);
      }
    );

    stompClientRef.current = stompClient;
  };

  // 3. 메시지 전송
  const sendMessage = () => {
    if (!inputMessage.trim()) return;

    if (stompClientRef.current && stompClientRef.current.connected) {
      const messageData = {
        chatRoomId: chatRoomId,
        content: inputMessage
      };

      // /pub/message로 메시지 발행
      stompClientRef.current.send(
        '/pub/message',
        {},
        JSON.stringify(messageData)
      );

      setInputMessage('');
    } else {
      console.error('WebSocket이 연결되지 않았습니다.');
    }
  };

  return (
    <div className="chat-room">
      <div className="connection-status">
        {connected ? '🟢 연결됨' : '🔴 연결 끊김'}
      </div>

      <div className="messages">
        {messages.map((msg) => (
          <div
            key={msg.messageId}
            className={msg.senderId === userId ? 'my-message' : 'other-message'}
          >
            <div className="sender">{msg.nickname}</div>
            <div className="content">{msg.content}</div>
            <div className="time">
              {new Date(msg.createdAt).toLocaleTimeString()}
            </div>
          </div>
        ))}
      </div>

      <div className="input-area">
        <input
          type="text"
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="메시지를 입력하세요..."
        />
        <button onClick={sendMessage} disabled={!connected}>
          전송
        </button>
      </div>
    </div>
  );
}

export default ChatRoom;
```

#### 3. WebSocket 연결 플로우 다이어그램

```
[프론트엔드]                                    [백엔드]
     |                                              |
     |  1. POST /v1/chatRoom (REST)                |
     |-------------------------------------------->|
     |                                              | (채팅방 생성/조회)
     |<--------------------------------------------|
     |  Response: chatRoomId, messages            |
     |                                              |
     |  2. Connect to /chat/inbox (SockJS)         |
     |-------------------------------------------->|
     |  Headers: { Authorization: Bearer token }   |
     |                                              | (StompHandler에서 JWT 검증)
     |<--------------------------------------------|
     |  CONNECTED frame                            |
     |                                              |
     |  3. SUBSCRIBE /sub/channel/{chatRoomId}     |
     |-------------------------------------------->|
     |                                              |
     |  4. SEND /pub/message                       |
     |  Body: { chatRoomId, content }              |
     |-------------------------------------------->|
     |                                              | (메시지 저장)
     |                                              | (브로드캐스트)
     |<--------------------------------------------|
     |  MESSAGE /sub/channel/{chatRoomId}          |
     |  Body: ChatMessageResponse                  |
     |                                              |
```

#### 4. WebSocket 메시지 형식

**클라이언트 → 서버 (SEND)**

```javascript
// Destination: /pub/message
{
  "chatRoomId": 1,
  "content": "안녕하세요!"
}
```

**서버 → 클라이언트 (MESSAGE)**

```javascript
// Topic: /sub/channel/{chatRoomId}
{
  "messageId": 123,
  "chatRoomId": 1,
  "senderId": 5,
  "content": "안녕하세요!",
  "nickname": "홍길동",
  "createdAt": "2024-03-20T10:30:00"
}
```

#### 5. WebSocket 설정 정보

| 항목 | 값 | 설명 |
|-----|-----|------|
| **STOMP Endpoint** | `/chat/inbox` | SockJS 연결 엔드포인트 |
| **Subscribe Prefix** | `/sub` | 클라이언트가 구독하는 경로 prefix |
| **Publish Prefix** | `/pub` | 클라이언트가 메시지를 보내는 경로 prefix |
| **Subscribe Channel** | `/sub/channel/{chatRoomId}` | 특정 채팅방 메시지 수신 |
| **Publish Destination** | `/pub/message` | 메시지 전송 |
| **Authentication** | JWT in STOMP headers | `Authorization: Bearer {token}` |

#### 6. 에러 처리 및 재연결

```javascript
const connectWithRetry = (roomId, retryCount = 0, maxRetries = 5) => {
  const token = localStorage.getItem('access_token');
  const socket = new SockJS('http://localhost:8080/chat/inbox');
  const stompClient = Stomp.over(socket);

  stompClient.connect(
    { Authorization: `Bearer ${token}` },
    (frame) => {
      console.log('연결 성공');
      setConnected(true);

      stompClient.subscribe(`/sub/channel/${roomId}`, (message) => {
        const receivedMessage = JSON.parse(message.body);
        setMessages((prev) => [...prev, receivedMessage]);
      });
    },
    (error) => {
      console.error('연결 실패:', error);
      setConnected(false);

      // 재연결 시도
      if (retryCount < maxRetries) {
        console.log(`재연결 시도 중... (${retryCount + 1}/${maxRetries})`);
        setTimeout(() => {
          connectWithRetry(roomId, retryCount + 1, maxRetries);
        }, 3000); // 3초 후 재시도
      } else {
        console.error('최대 재연결 횟수 초과');
        alert('채팅 서버에 연결할 수 없습니다. 페이지를 새로고침해주세요.');
      }
    }
  );

  stompClientRef.current = stompClient;
};
```

#### 7. 주의사항

⚠️ **중요한 사항들:**

1. **JWT 인증**
   - WebSocket 연결 시 STOMP 헤더에 JWT 토큰을 반드시 포함해야 합니다.
   - 서버의 `StompHandler`가 `CONNECT`와 `SEND` 프레임에서 토큰을 검증합니다.

2. **연결 해제**
   - 컴포넌트 언마운트 시 반드시 `stompClient.disconnect()`를 호출하세요.
   - 메모리 누수를 방지합니다.

3. **채팅방 ID**
   - REST API로 먼저 채팅방을 생성/조회한 후, 받은 `chatRoomId`로 WebSocket 구독을 해야 합니다.

4. **메시지 중복**
   - 같은 채팅방을 여러 번 구독하지 않도록 주의하세요.
   - 구독 해제 시 `subscription.unsubscribe()`를 호출하세요.

5. **브로드캐스트**
   - 메시지를 전송하면 해당 채팅방을 구독한 모든 클라이언트(자신 포함)에게 전송됩니다.
   - UI에서 내가 보낸 메시지를 중복으로 추가하지 않도록 주의하세요.

#### 8. 디버깅 팁

```javascript
// STOMP 디버그 활성화
stompClient.debug = (str) => {
  console.log('STOMP Debug:', str);
};

// 연결 상태 확인
console.log('연결 상태:', stompClient.connected);

// 구독 목록 확인
console.log('구독 목록:', stompClient.subscriptions);
```

---

## 에러 처리

### HTTP 상태 코드

| 상태 코드 | 설명 |
|----------|------|
| `200 OK` | 요청 성공 |
| `201 Created` | 생성 성공 |
| `400 Bad Request` | 잘못된 요청 (필수 필드 누락 등) |
| `401 Unauthorized` | 인증 실패 (토큰 없음 또는 만료) |
| `403 Forbidden` | 권한 없음 (본인이 아닌 데이터 수정 시도 등) |
| `404 Not Found` | 리소스를 찾을 수 없음 |
| `500 Internal Server Error` | 서버 오류 |

### 에러 응답 예시

```json
{
  "timestamp": "2024-03-20T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is expired",
  "path": "/users/myProfile"
}
```

### 일반적인 에러 처리 예시

```javascript
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // Access Token 만료 - Refresh Token으로 갱신
          return refreshTokenAndRetry(error);
        case 403:
          alert('권한이 없습니다.');
          break;
        case 404:
          alert('요청한 리소스를 찾을 수 없습니다.');
          break;
        case 500:
          alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
          break;
        default:
          alert(`오류가 발생했습니다: ${error.response.data.message}`);
      }
    } else if (error.request) {
      alert('서버에 연결할 수 없습니다. 네트워크를 확인해주세요.');
    }
    return Promise.reject(error);
  }
);
```

---

## 부록: 전체 DTO 스키마

### ActivityDTO
```typescript
interface ActivityDTO {
  year: number;           // 활동 연도
  title: string;          // 활동 제목
  link: string;           // 관련 링크
}
```

### PeerReviewDTO
```typescript
interface PeerReviewReq1 {
  startDate: string;              // YYYY-MM-DD
  meetSpecific: string;           // 협업 내용
  goodKeywordList: string[];      // 긍정 키워드 목록
  badKeywordList: string[];       // 부정 키워드 목록
}
```

---

## 📞 문의 및 지원

- **GitHub**: [프로젝트 Repository 링크]
- **이메일**: support@longkathon.com
- **Slack**: #longkathon-api-support

---

**버전**: 1.0.0
**최종 수정일**: 2024-03-20
**작성자**: Backend Team
