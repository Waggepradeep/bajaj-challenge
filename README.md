## Bajaj Finserv Health | Qualifier 1 | JAVA

Spring Boot app that on startup:
- Generates a webhook and receives an `accessToken` (JWT)
- Loads your final SQL from env or file
- Submits the SQL to the test webhook with the JWT in `Authorization`

### Prerequisites
- Java 17+ (tested with Java 22)
- Maven 3.9+

### Configure your details
Edit `src/main/resources/application.properties` (prefix `contest.*`):
- name: W Pradeep
- regNo: U25UV22T029081
- email: waggepradeep369@gmail.com

### Provide the final SQL
Two options (the app prefers env var when present):

1) Environment variable (no rebuild needed)

```powershell
$env:FINAL_QUERY = '<your full SQL>'
java -jar target\bajaj-challenge-0.0.1-SNAPSHOT.jar
```

2) File-based (rebuild required after changes)
- If the last two digits of `regNo` are odd → `src/main/resources/queries/odd.sql`
- If even → `src/main/resources/queries/even.sql`

Replace the placeholder with your final SQL. Then rebuild.

### Build
```powershell
mvn -q -DskipTests clean package
```
Output JAR: `target\bajaj-challenge-0.0.1-SNAPSHOT.jar`

### Run
```powershell
# Option A: with env var (recommended)
$env:FINAL_QUERY = 'SELECT 1;'  # replace with your real SQL
java -jar target\bajaj-challenge-0.0.1-SNAPSHOT.jar

# Option B: file-based (your regNo ends with 81 → odd.sql)
java -jar target\bajaj-challenge-0.0.1-SNAPSHOT.jar
```

Expected console flow:
1) “Starting Bajaj challenge flow”
2) “Webhook generated … hasToken=true”
3) “Submission status=200, body=…”

### Publish to GitHub (to make a public JAR link)
```powershell
git init
git add .
git commit -m "Initial commit: code"
git branch -M main
git remote add origin https://github.com/Waggepradeep/bajaj-challenge.git

# Copy the built JAR into a tracked folder for a stable raw URL
mkdir jar 2>$null
Copy-Item target\bajaj-challenge-0.0.1-SNAPSHOT.jar jar\bajaj-challenge-0.0.1-SNAPSHOT.jar -Force
git add jar\bajaj-challenge-0.0.1-SNAPSHOT.jar
git commit -m "Add compiled Spring Boot JAR"
git push -u origin main
```

Links for the submission form:
- Public repo: https://github.com/Waggepradeep/bajaj-challenge
- Public JAR (raw, direct download):  
  https://raw.githubusercontent.com/Waggepradeep/bajaj-challenge/main/jar/bajaj-challenge-0.0.1-SNAPSHOT.jar

### Notes
- No HTTP controllers; the flow is triggered by an `ApplicationRunner`.
- HTTP client: Spring `WebClient`.
- Endpoints:
  - `https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA`
  - `https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA`