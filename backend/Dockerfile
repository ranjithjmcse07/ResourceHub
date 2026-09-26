# Multi-stage Dockerfile for ToolShare Spring Boot Backend

# Stage 1: Build JAR with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production JRE runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for cloud security
RUN addgroup -S toolshare && adduser -S toolshare -G toolshare
USER toolshare

# Copy compiled jar
COPY --from=builder /app/target/toolshare-*.jar app.jar

# Dynamic PORT for Render/Railway/Heroku/AWS
EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dserver.port=${PORT}", "-jar", "app.jar"]
