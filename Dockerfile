# Используем официальный образ с Gradle и JDK 17 для сборки
FROM gradle:8.4.0-jdk17 AS builder

WORKDIR /app

# Копируем исходники
COPY . .

# Очищаем кеш Gradle, чтобы избежать конфликтов с версиями Kotlin
RUN rm -rf /home/gradle/.gradle

USER gradle

# Чистим проект и собираем fat jar за один RUN — чтобы не делать двойную сборку
RUN ./gradlew clean shadowJar --no-daemon

# Финальный образ с легковесным JDK 17
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Копируем собранный jar из билд-стейджа
COPY --from=builder /app/build/libs/*.jar /app/bot.jar

# Открываем порт 8080 (если нужен)
EXPOSE 8080

# Запускаем jar
CMD ["java", "-jar", "/app/bot.jar"]