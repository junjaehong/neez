# 🪪 BBEY / Neez Backend  
> Spring Boot + MyBatis + MySQL 기반 명함 관리 백엔드 서비스

---

## 🚀 Quickstart (VSCode)

1. VSCode 확장 설치  
   - **Extension Pack for Java**  
   - **Spring Boot Extension Pack**  
   - **Lombok Annotations Support**

2. `.env.sample` → `.env`로 복사 후 환경변수 수정  
3. VSCode에서 `neez` 폴더 열기  
4. 실행 : Run and Debug → Spring Boot: NeezApplication
5. 테스트:  
- `GET http://localhost:8083/health` → `"ok"`  
- `GET http://localhost:8083/db/ping` → `"select 1 = 1"`  
- `GET http://localhost:8083/db/tables?schema=Insa6_aiservice_p3_1` → `"list of tables."`

---

## 🧱 Build

```bash
./mvnw spring-boot:run
# or
./mvnw clean package && java -jar target/neez-0.0.1-SNAPSHOT.jar
```
---

## 🗄️ Database Config

DB 연결 정보는 .env 또는 시스템 환경 변수에서 관리합니다.
| Key       | 설명        |
| --------- | --------- |
| `DB_HOST` | DB 호스트 주소 |
| `DB_PORT` | 포트번호      |
| `DB_USER` | 사용자 이름    |
| `DB_PASS` | 비밀번호      |
| `DB_NAME` | 데이터베이스 이름 |

- application.yml 예시 구조:
  ```
  spring:
    datasource:
      url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
      username: ${DB_USER}
      password: ${DB_PASS}
  ```
  ```
    ⚠️ 실제 application.yml에는 민감 정보가 포함되어 있으므로 GitHub에 업로드하지 않습니다.
    
    🖥  저장 위치: C:\Users\USER\Desktop\neez\BackEnd\neez\src\main\resources\application.yml
  ```

## 🧩 Tech Stack
| 구분                  | 기술                             |
| ------------------- | ------------------------------ |
| **Backend**         | Spring Boot 2.7.12, MyBatis    |
| **Database**        | MySQL 8.x                      |
| **Language**        | Java 8                         |
| **Docs / API Test** | Swagger UI (springdoc-openapi) |
| **Dependency 관리**   | Maven                          |
| **Annotation**      | Lombok                         |

## 🧾 Swagger UI

Swagger(OpenAPI)로 API 문서 확인 가능
- URL: http://localhost:8083/swagger-ui/index.html
- 의존성 추가 (pom.xml):
  ```
  <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-ui</artifactId>
      <version>1.6.15</version>
  </dependency>
  ```


## 🔗 주요 API 요약
| 구분  | 메서드      | 경로                                     | 설명                  |
| --- | -------- | -------------------------------------- | ------------------- |
| 1️⃣ | `POST`   | `/api/bizcards/manual`                 | 명함 수기 등록            |
| 2️⃣ | `POST`   | `/api/bizcards/read`                   | OCR로 등록 (파일명 기반)    |
| 3️⃣ | `POST`   | `/api/bizcards/read/upload`            | 이미지 업로드 + OCR 등록    |
| 4️⃣ | `GET`    | `/api/bizcards/user/{userIdx}/page`    | 사용자 명함 목록 조회        |
| 5️⃣ | `GET`    | `/api/bizcards/{idx}`                  | 명함 상세 조회            |
| 6️⃣ | `PATCH`  | `/api/bizcards/{idx}/memo`             | 메모만 수정              |
| 7️⃣ | `PUT`    | `/api/bizcards/{idx}`                  | 명함 전체 수정            |
| 8️⃣ | `DELETE` | `/api/bizcards/{idx}`                  | 명함 삭제 (Soft Delete) |
| 9️⃣ | `PATCH`  | `/api/bizcards/{idx}/restore`          | 명함 복구               |
| 🔟  | `GET`    | `/api/bizcards/user/{userIdx}/deleted` | 삭제된 명함(휴지통) 조회      |

## 💬 Example RequestBody
### ✏️ 수기 등록 (/manual)
```
{
  "user_idx": 1,
  "company": "네이버",
  "name": "홍길동",
  "department": "개발1팀",
  "position": "주임",
  "email": "gildong@naver.com",
  "mobile": "010-1234-5678",
  "tel": "02-987-6543",
  "fax": "02-333-2222",
  "address": "서울시 강남구 테헤란로 123",
  "memo": "네이버 담당자, 다음 주 회의 예정"
}
```

### 📸 OCR 등록 (/read)
```
{
  "fileName": "biz1.jpg",
  "user_idx": 1
}
```

### 📎 업로드 + OCR (/read/upload)
#### 폼데이터로 테스트:
```
file: (명함 이미지 업로드)
user_idx: 1
```

#### 🧾 메모 수정 (/{id}/memo)
```
{
  "memo": "회의 일정 조율 완료"
}
```
---
### ⚠️ 에러 응답 예시

- 명함이 존재하지 않거나 ID가 잘못된 경우:
  ``` 
  {
    "success": false,
    "message": "BizCard not found: 999",
    "data": null
  }
  ```

---

## 🧪 테스트 시 권장 순서

1️⃣ POST /api/bizcards/manual → 명함 수기 등록

2️⃣ GET /api/bizcards/user/1/page → 사용자 명함 목록 조회

3️⃣ GET /api/bizcards/{idx} → 명함 상세 확인

4️⃣ PATCH /api/bizcards/{idx}/memo → 메모 수정

5️⃣ PUT /api/bizcards/{idx} → 명함 정보 전체 수정

6️⃣ DELETE /api/bizcards/{idx} → 명함 삭제 (휴지통으로 이동)

7️⃣ PATCH /api/bizcards/{idx}/restore → 명함 복구

