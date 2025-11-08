# BBEY / Neez Backend (Spring Boot + MyBatis + MySQL)

## Quickstart (VSCode)
1. Install extensions: **Extension Pack for Java**, **Spring Boot Extension Pack**, **Lombok Annotations Support**.
2. Copy `.env.sample` to `.env` and adjust if needed.
3. Open folder `neez` (this folder) in VSCode.
4. Run: **Run and Debug → Spring Boot: NeezApplication**.
5. Test:
   - `GET http://localhost:8080/health` → `ok`
   - `GET http://localhost:8080/db/ping` → `select 1 = 1`
   - `GET http://localhost:8080/db/tables?schema=Insa6_aiservice_p3_1` → `list of tables.`

## Build
```bash
./mvnw spring-boot:run
# or
./mvnw clean package && java -jar target/neez-0.0.1-SNAPSHOT.jar
```

## DB Config
Configured via environment variables in `application.yml`:
- DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME

## application.yml
'application.yml'는 DB 관련 민감 정보 존재 -> GitHub를 사용하지 않고 개인적으로 전달
- 저장 장소 : 'C:\Users\USER\Desktop\neez\BackEnd\neez\src\main\resources\application.yml'

# 📒 BizCard API 사용 매뉴얼

본 문서는 `Spring Boot (v2.7.12)` 기반으로 만든 명함 관리 API를 테스트할 때 사용할 수 있는 예시 JSON을 정리한 것이다.  
프론트나 Postman에서 그대로 붙여써서 호출하면 된다.

---

## 0. 공통 응답 포맷

모든 API는 아래와 같은 형식을 기본으로 응답한다.

```json
{
  "success": true,
  "message": "ok",
  "data": { }
}
```
- success: 처리 성공 여부 (true / false)
- message: 처리 결과 메시지 (ok, already exists, updated, 에러메시지 등)
- data: 실제 응답 데이터 (DTO, 리스트, null 등)

## 1. 명함 정보 수기 등록 (Manual)

- POST /api/bizcards/manual
- 프론트에서 사용자가 직접 입력한 값을 명함으로 등록할 때 사용.

RequestBody 예시
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
Response 예시
```
{
  "success": true,
  "message": "ok",
  "data": {
    "idx": 5,
    "user_idx": 1,
    "name": "홍길동",
    "company_name": "네이버",
    "department": "개발1팀",
    "position": "주임",
    "email": "gildong@naver.com",
    "phone_number": "010-1234-5678",
    "line_number": "02-987-6543",
    "fax_number": "02-333-2222",
    "address": "서울시 강남구 테헤란로 123",
    "memo_content": "네이버 담당자, 다음 주 회의 예정"
  }
}
```

## 2. 명함 정보 OCR 등록

- POST /api/bizcards/read
- 서버에 있는 이미지 파일명을 넘기면 OCR → 파싱 → DB 저장까지 처리.
RequestBody 예시
```
{
  "fileName": "biz1.jpg",
  "user_idx": 1
}
```
Response 예시
```
{
  "success": true,
  "message": "ok",
  "data": {
    "idx": 6,
    "user_idx": 1,
    "name": "",
    "company_name": "",
    "department": "",
    "position": "",
    "email": "",
    "phone_number": "",
    "line_number": "",
    "fax_number": "",
    "address": "",
    "memo_content": null
  }
}
```
|  실제 OCR 연결 전이라면 빈 값으로 올 수 있음.

## 3. 단일 명함 상세 조회
```
GET /api/bizcards/{idx}
```
- 명함 하나 클릭했을 때 상세정보 + 회사명 + 메모내용까지 보고 싶을 때.

호출 예시
```
GET /api/bizcards/5
```

Response 예시
```
{
  "success": true,
  "message": "ok",
  "data": {
    "idx": 5,
    "user_idx": 1,
    "name": "홍길동",
    "company_name": "네이버",
    "department": "개발1팀",
    "position": "주임",
    "email": "gildong@naver.com",
    "phone_number": "010-1234-5678",
    "line_number": "02-987-6543",
    "fax_number": "02-333-2222",
    "address": "서울시 강남구 테헤란로 123",
    "memo_content": "네이버 담당자, 다음 주 회의 예정"
  }
}
```

## 4. 특정 사용자(userIdx)의 명함 전체 조회
```
GET /api/bizcards/user/{userIdx}
```
- 마이페이지나 “내 명함함” 같은 화면에서 사용하는 API.

호출 예시
```
GET /api/bizcards/user/1
```
Response 예시
```
{
  "success": true,
  "message": "ok",
  "data": [
    {
      "idx": 5,
      "user_idx": 1,
      "name": "홍길동",
      "company_name": "네이버",
      "department": "개발1팀",
      "position": "주임",
      "email": "gildong@naver.com",
      "phone_number": "010-1234-5678",
      "line_number": "02-987-6543",
      "fax_number": "02-333-2222",
      "address": "서울시 강남구 테헤란로 123",
      "memo_content": "네이버 담당자, 다음 주 회의 예정"
    },
    {
      "idx": 6,
      "user_idx": 1,
      "name": "김영희",
      "company_name": "카카오",
      "department": "플랫폼기획",
      "position": "매니저",
      "email": "younghee@kakao.com",
      "phone_number": "010-7777-8888",
      "line_number": null,
      "fax_number": null,
      "address": "경기 성남시 분당구",
      "memo_content": ""
    }
  ]
}
```

## 5. 명함 정보 수정
```
PUT /api/bizcards/{idx}
```
- 명함의 기본 정보만 바꿀 때 (메모 제외).

RequestBody 예시
```
{
  "name": "홍길동",
  "department": "AI사업부",
  "position": "팀장",
  "email": "gildong.ai@naver.com",
  "mobile": "010-5555-7777",
  "tel": "02-111-2222",
  "fax": "02-111-3333",
  "address": "서울시 성동구 뚝섬로 321",
  "company_idx": "2"
}
```

필요한 필드만 보내도 됨. null이거나 안 보낸 건 그대로 유지.

Response 예시
```
{
  "success": true,
  "message": "updated",
  "data": {
    "idx": 5,
    "user_idx": 1,
    "name": "홍길동",
    "company_name": null,
    "department": "AI사업부",
    "position": "팀장",
    "email": "gildong.ai@naver.com",
    "phone_number": "010-5555-7777",
    "line_number": "02-111-2222",
    "fax_number": "02-111-3333",
    "address": "서울시 성동구 뚝섬로 321",
    "memo_content": null
  }
}
```
## 6. 명함 메모 단일 조회
```
GET /api/bizcards/{id}/memo
```
- 메모만 따로 띄우는 팝업/모달에서 사용.

호출 예시
```
GET /api/bizcards/5/memo
```
Response 예시
```
{
  "success": true,
  "message": "ok",
  "data": {
    "bizcard_id": 5,
    "memo_content": "네이버 담당자, 다음 주 회의 예정",
    "memo_path": "card-5.txt"
  }
}
```

## 7. 명함 메모만 수정
```
PATCH /api/bizcards/{id}/memo
```
- 명함 기본정보는 그대로 두고 메모 txt만 갱신.

RequestBody 예시
```
{
  "memo": "12월 3주차로 미팅 일정 변경됨. 담당자 박대리 → 김대리."
}
```

Response 예시
```
{
  "success": true,
  "message": "memo updated",
  "data": {
    "bizcard_id": 5,
    "memo_content": "12월 3주차로 미팅 일정 변경됨. 담당자 박대리 → 김대리.",
    "memo_path": "card-5.txt"
  }
}
```

## 8. 에러 응답 예시

DB에 명함이 없을 때, ID가 잘못됐을 때 등
```
{
  "success": false,
  "message": "BizCard not found: 999",
  "data": null
}
```
## 9. 테스트 시 권장 순서

   1) POST /api/bizcards/manual 로 수기 등록해보고
   2) GET /api/bizcards/user/1 로 리스트 확인
   3) GET /api/bizcards/{idx} 로 상세 확인
   4) PATCH /api/bizcards/{idx}/memo 로 메모만 수정
   5) 필요하면 PUT /api/bizcards/{idx} 로 정보 전체 수정
   7) 이렇게 적어두면 README 하나로 프론트, 백 둘 다 테스트 가능.


## SWAGGER TEST URL
- http://localhost:8083/swagger-ui/#