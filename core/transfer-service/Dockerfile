# --- build stage ---
FROM gradle:8.10.0-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle :core:transfer-service:clean :core:transfer-service:bootJar --no-daemon -x test

# --- runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/core/transfer-service/build/libs/*.jar app.jar

# ---------- build stage ----------
FROM gradle:8.10.0-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle :core:transfer-service:clean :core:transfer-service:bootJar -x test --no-daemon

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/core/transfer-service/build/libs/*.jar /app/app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]
