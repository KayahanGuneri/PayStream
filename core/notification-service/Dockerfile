FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR=build/libs/*.jar
COPY ${JAR} app.jar
EXPOSE 9005
ENTRYPOINT ["java","-jar","/app/app.jar"]
