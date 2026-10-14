# syntax=docker/dockerfile:1

# ---- Build stage: compile and package the Spring Boot fat jar ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Resolve dependencies first so this layer is cached until pom.xml changes.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Build the application (tests are run in CI, not during image build).
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage: minimal JRE running the packaged jar ----
FROM eclipse-temurin:21-jre

WORKDIR /app

# Run as a non-root user.
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/*.jar app.jar
RUN chown app:app app.jar
USER app

# Allow JVM tuning via JAVA_OPTS at runtime (e.g. -Xmx256m).
ENV JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
