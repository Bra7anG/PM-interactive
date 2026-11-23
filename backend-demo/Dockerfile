# Etapa 1: construir el .jar
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts ./
COPY src/ src/
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# Etapa 2: ejecutar el .jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
