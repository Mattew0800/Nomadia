# ── Etapa 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

# Directorio de trabajo
WORKDIR /app

# Copiamos archivos para cache de dependencias
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Copiamos el código fuente
COPY src ./src

# Hacemos ejecutable el wrapper y descargamos dependencias sin compilar el código
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Compilamos el proyecto y generamos el JAR
RUN ./mvnw package -DskipTests -q

# ── Etapa 2: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiamos el JAR compilado desde la etapa build
COPY --from=builder /app/target/travelplanner-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto (Render asigna dinámicamente la variable PORT)
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
