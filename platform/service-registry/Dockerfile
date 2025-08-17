FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR=build/libs/*.jar
COPY ${JAR} app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app/app.jar"]
