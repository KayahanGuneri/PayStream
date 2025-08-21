FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR=build/libs/*.jar
COPY ${JAR} app.jar
EXPOSE 9001
ENTRYPOINT ["java","-jar","/app/app.jar"]


# --- build stage ---

# Uses Gradle with JDK 21 to build the ledger-service boot jar inside the monorepo
FROM gradle:8.10.0-jdk21 AS build
WORKDIR /home/gradle/src
COPY . .
# Build only the ledger-service module to keep the image smaller and faster
RUN gradle :core:ledger-service:bootJar --no-daemon

# --- runtime stage ---
# Lightweight JRE for running the Spring Boot application
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the built jar from the previous stage
COPY --from=build /home/gradle/src/core/ledger-service/build/libs/*.jar app.jar
EXPOSE 9100
ENTRYPOINT ["java","-jar","/app/app.jar"]


FROM gradle:8.10.0-jdk21 AS build
WORKDIR /home/gradle/src
COPY . .
RUN gradle :core:ledger-service:bootJar --no-daemon

# --- runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/src/core/ledger-service/build/libs/*.jar app.jar
EXPOSE 9100
ENTRYPOINT ["java","-jar","/app/app.jar"]

