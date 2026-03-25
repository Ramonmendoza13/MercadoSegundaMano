# Dockerfile

# ETAPA 1: Compilar la aplicación
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# ---------------------------------------------------------------

# ETAPA 2: Imagen final
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Carpeta para H2
RUN mkdir -p /app/data
# Carpeta para las imagenes subidas
RUN mkdir -p /app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]