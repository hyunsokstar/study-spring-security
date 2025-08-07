# Security Demo 서버 관리 가이드

이 문서는 `C:\study-spring-boot\security-demo\readme.md` 파일에 작성되는 서버 관리 매뉴얼입니다.

---

## 1️⃣ 서버 중지

1. 실행 중인 Java 프로세스 확인

   ```bash
   ps aux | grep java
   ```
2. 원하는 PID(예: `27554`)로 종료

   ```bash
   kill 27554
   ```
3. 그래도 안 죽으면 강제 종료

   ```bash
   kill -9 27554
   ```

---

## 2️⃣ 빌드

프로젝트 최상위 디렉토리에서 실행:

```bash
./gradlew clean bootJar
```

* 결과물: `build/libs/security-demo-0.0.1-SNAPSHOT.jar`

---

## 3️⃣ 서버 시작

백그라운드 실행 (로그는 `app.log`에 남김):

```bash
nohup java -jar build/libs/security-demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

* 실행 후 마지막 줄에 뜨는 PID를 확인하세요.

---

## 4️⃣ 상태 확인

1. 프로세스 확인

   ```bash
   ps aux | grep security-demo
   ```
2. 로그 실시간 보기

   ```bash
   tail -f app.log
   ```

---

## 5️⃣ 재시작

1. 서버 중지 (1️⃣ 참고)

   ```bash
   ps aux | grep security-demo
   kill <PID>
   ```
2. 서버 시작 (3️⃣ 참고)

   ```bash
   nohup java -jar build/libs/security-demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
   ```

---

## ✨ 팁

* **Alias 활용**
  매번 PID 찾기 번거로우면, `~/.bashrc` 등에 다음 Alias를 추가하세요:

  ```bash
  alias stopapp="ps aux | grep security-demo | awk '{print \$2}' | xargs kill"
  ```

  이후 `stopapp` 명령만으로 서버를 중지할 수 있습니다.

* **systemd 서비스 등록**
  장기 운영 시 `systemd`에 등록하면:

  ```bash
  sudo systemctl start  security-demo
  sudo systemctl stop   security-demo
  sudo systemctl restart security-demo
  ```

  처럼 간편하게 관리할 수 있습니다.
