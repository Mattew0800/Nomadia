# ── Etapa 1: build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiamos el wrapper y el pom primero (cache de dependencias)
COPY api/mvnw api/mvnw.cmd api/pom.xml ./
COPY api/.mvn .mvn

# Descargamos dependencias sin compilar el código fuente
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copiamos el código fuente y compilamos
COPY api/src ./src
RUN ./mvnw package -DskipTests -q

# ── Etapa 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/travelplanner-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
