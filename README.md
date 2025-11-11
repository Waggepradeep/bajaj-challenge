## Bajaj Finserv Health | Qualifier 1 | JAVA

Spring Boot app that:
- On startup, generates a webhook, receives an `accessToken`
- Chooses your final SQL query (from env or files)
- Submits the SQL to the test webhook with JWT in `Authorization`

### Your details
Configured in `src/main/resources/application.properties` under `contest.*`.

Defaults (as provided):
- name: W Pradeep
- regNo: U25UV22T029081
- email: waggepradeep369@gmail.com

### Supplying the final SQL
There are two ways:
1) Environment variable (recommended during submission)

```powershell
$env:FINAL_QUERY='SELECT 1;'
mvn -q -DskipTests spring-boot:run
```

2) File-based (no env var set)
- If the last two digits of `regNo` are odd, the app reads `src/main/resources/queries/odd.sql`
- If even, it reads `src/main/resources/queries/even.sql`

Replace the placeholder `SELECT 1;` with your actual answer.

### Build
```powershell
mvn -q -DskipTests clean package
```
JAR output: `target/bajaj-challenge-0.0.1-SNAPSHOT.jar`

### Run
```powershell
# Option A: with env var
$env:FINAL_QUERY='SELECT 1;'
java -jar target/bajaj-challenge-0.0.1-SNAPSHOT.jar

# Option B: without env var (uses odd/even file)
java -jar target/bajaj-challenge-0.0.1-SNAPSHOT.jar
```

### Notes
- No controllers are exposed; the flow triggers on startup via `ApplicationRunner`.
- HTTP client used: `WebClient` (reactive).
- Endpoints used:
  - `https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA`
  - `https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA`


