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
