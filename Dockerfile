# 1. 베이스 이미지 선택 (Java 17 버전을 사용)
FROM openjdk:17-jdk-slim

# 2. JAR 파일이 저장될 위치를 /app으로 지정
WORKDIR /app

# 3. 빌드된 JAR 파일을 컨테이너의 /app 폴더로 복사
COPY build/libs/*.jar app.jar

# 4. 컨테이너가 시작될 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]

# 5. 애플리케이션이 사용하는 포트(8080)를 외부에 알림
EXPOSE 8080