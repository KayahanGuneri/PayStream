# ---- build stage ----
FROM gradle:8.9-jdk21 AS build
WORKDIR /src
COPY ../../ .
RUN gradle :core:payment-service:bootJar -x test

# ---- runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/core/payment-service/build/libs/*.jar app.jar
EXPOSE 9095
ENTRYPOINT ["java","-jar","/app/app.jar"]
