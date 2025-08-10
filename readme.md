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