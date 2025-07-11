# Используем официальное JDK-образ
FROM gradle:8.4.0-jdk17 as builder

WORKDIR /app
COPY . .

# Сборка fat jar
RUN ./gradlew shadowJar

# Финальный образ
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/bot.jar

CMD ["java", "-jar", "/app/bot.jar"]

EXPOSE 8080