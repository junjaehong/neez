# BBEY / Neez Backend (Spring Boot + MyBatis + MySQL)

## Quickstart (VSCode)
1. Install extensions: **Extension Pack for Java**, **Spring Boot Extension Pack**, **Lombok Annotations Support**.
2. Copy `.env.sample` to `.env` and adjust if needed.
3. Open folder `neez` (this folder) in VSCode.
4. Run: **Run and Debug → Spring Boot: NeezApplication**.
5. Test:
   - `GET http://localhost:8080/health` → `ok`
   - `GET http://localhost:8080/db/ping` → `select 1 = 1`
   - `GET http://localhost:8080/db/tables?schema=Insa6_aiservice_p3_1` → list of tables.

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

## 명함 정보 수기 등록 메뉴얼
- 아래 형식에 맞는 **json 형식**으로 데이터 전송 필요 (FROM FrontENd)
   {
      "user_idx": "1",
      "name": "김현대",
      "company": "비비와이",
      "department": "AI서비스개발팀",
      "position": "주니어 백엔드",
      "email": "hyundai.kim@example.com",
      "mobile": "010-1234-5678",
      "tel": "02-123-4567",
      "fax": "02-123-4568",
      "address": "광주광역시 북구 첨단과기로",
      "memo": "인공지능사관학교 동기"
   }

