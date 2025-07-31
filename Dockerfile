FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./gradlew build --no-daemon

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
