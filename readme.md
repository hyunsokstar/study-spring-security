# EC2 서버 업데이트 및 재시작 가이드

## 1. 최신 코드 받기
```bash
sudo git pull
```

## 2. 기존 PostgreSQL 컨테이너 정리
```bash
# 기존 컨테이너 중지 및 삭제
docker stop security_demo_db
docker rm security_demo_db

# 기존 볼륨 삭제 (데이터 초기화)
docker volume rm study-spring-security_pgdata
```

## 3. 새 docker-compose.yml 생성
```bash
cat > docker-compose.yml << 'EOF'
services:
  postgres:
    image: pgvector/pgvector:pg16
    container_name: security_demo_db
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: security_db
      POSTGRES_USER: pilot
      POSTGRES_PASSWORD: pilot1234
    volumes:
      - pgdata:/var/lib/postgresql/data
    networks:
      - backend

volumes:
  pgdata:

networks:
  backend:
EOF
```

## 4. 새 PostgreSQL 컨테이너 실행
```bash
docker-compose up -d
```

## 5. 컨테이너 실행 확인
```bash
docker ps
docker logs security_demo_db
```

## 6. 기존 Spring Boot 앱 종료
```bash
# 실행 중인 Java 프로세스 확인
ps aux | grep java

# 포트 8080 사용 중인 프로세스 확인
lsof -i :8080

# 기존 앱 종료
sudo fuser -k 8080/tcp
```

## 7. 새 코드 빌드
```bash
./gradlew build
```

## 8. nohup으로 백그라운드 실행
```bash
nohup java -jar build/libs/security-demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

## 9. 실행 확인
```bash
# 프로세스 확인
ps aux | grep java

# 포트 확인
lsof -i :8080

# 로그 확인
tail -f app.log

# 로그 실시간 모니터링 중단: Ctrl + C
```

## 10. 데이터베이스 초기화 (필요시)
```bash
# backup.sql이 있다면
docker exec -i security_demo_db psql -U pilot -d security_db < backup.sql

# 또는 init.sql이 있다면
docker exec -i security_demo_db psql -U pilot -d security_db < init.sql
```

## 참고사항
- nohup으로 실행하면 SSH 연결이 끊어져도 서버가 계속 실행됩니다
- 로그는 `app.log` 파일에 저장됩니다
- PostgreSQL은 pgvector 확장이 포함된 버전으로 업그레이드됩니다
- 데이터베이스명: `security_db`, 사용자: `pilot`, 비밀번호: `pilot1234`

---

# DDD 기반 채팅 백엔드 스캐폴드 추가 안내

본 프로젝트에 DDD 스타일의 채팅(bounded context: chat) 기본 골격을 추가했습니다. 최소 구성으로 도메인/애플리케이션/인프라/프리젠테이션 레이어를 분리했습니다.

폴더 구조:
- src/main/java/com/example/security/security_demo/chat
  - domain: ChatRoom, Message 엔티티와 포트(ChatRoomRepository, MessageRepository)
  - application: ChatCommandService(쓰기), ChatQueryService(읽기)
  - infrastructure/jpa: Spring Data JPA 어댑터 (포트 구현)
  - presentation: ChatController + 요청 DTO

기본 엔드포인트:
- POST /api/chat/rooms
  - 요청: { "name": "방이름" }
  - 응답: 생성된 ChatRoom
- GET /api/chat/rooms
  - 응답: ChatRoom 리스트
- GET /api/chat/rooms/{roomId}
  - 응답: 단일 ChatRoom
- POST /api/chat/rooms/{roomId}/messages
  - 요청: { "senderUserId": "user-123", "content": "안녕하세요" }
  - 응답: 생성된 Message
- GET /api/chat/rooms/{roomId}/messages
  - 응답: 해당 방의 Message 리스트(오래된 순)

주의사항 및 다음 단계 제안:
1) 인증 연동: senderUserId를 SecurityContext의 인증 사용자로 대체하는 것이 좋습니다.
2) 실시간 통신: 이후 STOMP(WebSocket) 또는 SSE를 도입하여 메시지 푸시 전송 구현 권장.
3) 도메인 규칙 강화: 방 참여자, 초대/권한, 메시지 편집/삭제 정책 등 도메인 로직 추가.
4) 이벤트/아웃박스: 메시지 생성 이벤트 발행, 알림 등 비동기 처리를 위한 이벤트 설계.
5) 성능: 메시지 페이지네이션, 인덱스, 아카이브 전략 고려.

현재는 JPA ddl-auto=update로 테이블이 자동 생성되며(PostgreSQL), 필요 시 마이그레이션 도구(Flyway/Liquibase) 도입을 권장합니다.