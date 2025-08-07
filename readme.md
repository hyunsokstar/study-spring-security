# Security Demo 서버 관리 가이드

이 문서는 `C:\study-spring-boot\security-demo\readme.md` 파일에 작성되는 서버 관리 및 개발 가이드입니다.

---

## 프로젝트 개요

* **기술 스펙**: Spring Boot 3.4.1, Spring Security, Spring AI (OpenAI), JWT 기반 인증
* **목표**: Spring Security + Spring AI를 활용한 RAG 기반 챗봇 자동 응답 기능 구현

---

## 1️⃣ 주요 엔드포인트

* Swagger UI: 🔗 `http://43.200.234.52:8080/swagger-ui/index.html`
* OpenAPI JSON: `http://43.200.234.52:8080/v3/api-docs`

---

## 2️⃣ 빌드·Jar 파일 설정

1. `build.gradle`에서 프로젝트 버전 및 Jar 파일 이름 지정

   ```groovy
   version = '0.0.1-SNAPSHOT'        // build/libs/security-demo-0.0.1-SNAPSHOT.jar 생성

   tasks.named('bootJar') {
       archiveBaseName.set('security-demo')   // (선택) base name 커스터마이징
       archiveVersion.set(version)
   }
   ```
2. `.env` 또는 `application.yml` 환경변수 설정

   ```yaml
   spring:
     config:
       import: "optional:file:.env"

     datasource:
       jdbc-url: ${DB_URL:jdbc:postgresql://localhost:5432/security_db}
       username: ${DB_USERNAME:postgres}
       password: ${DB_PASSWORD:postgres123}

     jpa:
       properties:
         hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect

     ai:
       openai:
         api-key: ${OPENAI_API_KEY}
         chat:
           options:
             model: gpt-4-1106-preview
   ```

---

## 3️⃣ 서버 관리 절차

### A) 서버 중지

```bash
# 1) Java 프로세스 조회
ps aux | grep java

# 2) 원하는 PID로 종료
kill <PID>
# (강제) kill -9 <PID>
```

### B) 빌드

```bash
./gradlew clean bootJar
# → build/libs/security-demo-0.0.1-SNAPSHOT.jar 생성
```

### C) 서버 시작

```bash
nohup java -jar build/libs/security-demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

* 마지막 줄에 뜨는 PID 확인

### D) 상태 확인

```bash
# 프로세스 확인
ps aux | grep security-demo

# 로그 실시간 모니터링
tail -f app.log
```

### E) 재시작

```bash
# 서버 중지 (A 참고)
ps aux | grep security-demo
kill <PID>

# 서버 시작 (C 참고)
nohup java -jar build/libs/security-demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

---

## 4️⃣ 기타 팁

* **Alias 활용** (`~/.bashrc`):

  ```bash
  alias stopapp="ps aux | grep security-demo | awk '{print \$2}' | xargs kill"
  ```
* **systemd 서비스 등록**:

  ```ini
  [Unit]
  Description=Security Demo Spring Boot App

  [Service]
  ExecStart=/usr/bin/java -jar /home/ubuntu/study-spring-security/build/libs/security-demo-0.0.1-SNAPSHOT.jar
  SuccessExitStatus=143
  Restart=on-failure
  User=ubuntu

  [Install]
  WantedBy=multi-user.target
  ```

  ```bash
  sudo mv security-demo.service /etc/systemd/system/
  sudo systemctl daemon-reload
  sudo systemctl start security-demo
  sudo systemctl enable security-demo
  ```

---

## 5️⃣ 개발 가이드

* **Spring Security**: JWT 필터, AuthenticationEntryPoint, SecurityFilterChain
* **Spring AI**: `spring-ai-starter-model-openai`, RAG(Retrieval-Augmented Generation) 패턴 적용
* **챗봇 자동 응답**: OpenAI Chat API 호출, DB 또는 벡터 스토어(예: Elastic, Redis)에서 문서 검색 후 컨텍스트 제공
* **테스트**: Swagger UI, Postman을 활용한 API 검증

---

> 문서가 최신이 아닐 경우, 이 `readme.md`를 업데이트해주세요.
