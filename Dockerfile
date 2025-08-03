# Используем официальное JDK-образ
FROM gradle:8.4.0-jdk17 as builder

WORKDIR /app
COPY . .

# Сборка fat jar
RUN ./gradlew clean shadowJar --no-daemon --stacktrace -Dorg.gradle.jvmargs="-Xmx256m"

# Финальный образ
FROM eclipse-temurin:22-jdk-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/bot.jar

CMD ["java", "-jar", "/app/bot.jar"]

EXPOSE 8080
