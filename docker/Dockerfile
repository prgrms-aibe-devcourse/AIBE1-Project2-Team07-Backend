# 빌드 전용 이미지
FROM gradle:8.5.0-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test

# 실행용 이미지
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# 환경 변수 설정 (entrypoint에서 사용할 수 있도록)
ENV SPRING_PROFILES_ACTIVE=prod

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]