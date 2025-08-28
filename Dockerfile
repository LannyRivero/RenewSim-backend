# Etapa de compilación
FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /app
COPY . .
RUN mvn clean package -Dmaven.test.skip=true

# Etapa de ejecución (ahora con Java 21 también)
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]




