FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar app.jar

# prod 프로파일 활성화
CMD ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
