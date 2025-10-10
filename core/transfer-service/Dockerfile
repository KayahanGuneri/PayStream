# ---------- build stage ----------
FROM gradle:8.10.0-jdk21 AS builder
WORKDIR /workspace
COPY . .
RUN gradle :core:transfer-service:clean :core:transfer-service:bootJar --no-daemon -x test

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=builder /workspace/core/transfer-service/build/libs/*.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]
