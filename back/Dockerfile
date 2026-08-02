# Build multietapa: la imagen final no lleva ni Maven ni el codigo fuente,
# solo el JRE y el jar. Pasa de ~450 MB a ~200 MB.

# ------------------------------------------------------------------
# Etapa 1: compilacion
# ------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Las dependencias se resuelven antes de copiar el codigo: mientras el pom no
# cambie, Docker reutiliza esta capa y el build no vuelve a descargar internet.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -DskipTests package

# ------------------------------------------------------------------
# Etapa 2: ejecucion
# ------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios: si algo compromete el proceso, no es root.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=postgres

EXPOSE 8080

# Forma shell (no exec) para que $JAVA_OPTS se expanda.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
