# --- Stage 1: build ---
FROM gradle:8.10.0-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle :core:ledger-service:clean :core:ledger-service:bootJar -x test --no-daemon

# --- Stage 2: runtime ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/core/ledger-service/build/libs/*.jar /app/app.jar
EXPOSE 9100
ENTRYPOINT ["java","-jar","/app/app.jar"]
