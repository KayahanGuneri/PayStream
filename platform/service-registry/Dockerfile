# ---------- build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
# wrapper'ı çalıştırılabilir yap (Linux içindeyiz)
RUN chmod +x ./gradlew || true
# Monorepo: sadece bu modülü derle
RUN ./gradlew :platform:service-registry:clean :platform:service-registry:bootJar --no-daemon -x test

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/platform/service-registry/build/libs/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app/app.jar"]
