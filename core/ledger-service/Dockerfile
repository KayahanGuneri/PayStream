FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR=build/libs/*.jar
COPY ${JAR} app.jar
EXPOSE 9001
ENTRYPOINT ["java","-jar","/app/app.jar"]


# --- build stage ---
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
