# Etapa 1: Build con Gradle
FROM gradle:8.4.0-jdk21 AS build
WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY build.gradle settings.gradle ./

# Copiar la carpeta gradle (wrapper) si existe
COPY gradle gradle

# Copiar el código fuente
COPY src src

# Ejecutar build y verificar si se generó el JAR
RUN gradle build --no-daemon && ls -la build/libs

# Etapa 2: Imagen liviana de runtime
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copiar el .jar desde el contenedor de build
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
