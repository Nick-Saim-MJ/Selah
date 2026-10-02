# SelahFinance API · imagen para la nube (Railway, Render, Fly, VPS…).
# Construir:  docker build -t selah-api backend
# Ejecutar:   docker run -p 8080:8080 -e JWT_SECRET=... -e DB_URL=jdbc:postgresql://host:5432/db -e DB_USER=... -e DB_PASSWORD=... selah-api

# ---- 1) Compilación
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Las pruebas (incluida ArchUnit) se corren antes, en local/CI; aquí solo se empaqueta.
RUN mvn -B -q -DskipTests package && mv target/selah-finance-api-*.jar app.jar

# ---- 2) Ejecución (JRE mínimo, usuario sin privilegios)
FROM eclipse-temurin:17-jre
RUN useradd --system --no-create-home selah
WORKDIR /app
COPY --from=build /app/app.jar app.jar
USER selah

# Pensado para ~512 MB de RAM: heap acotado, GC serial y compilador ligero.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
ENV SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=5

EXPOSE 8080
# El puerto lo toma de la variable PORT (Railway la define sola; por defecto 8080).
ENTRYPOINT ["java", "-jar", "app.jar"]
