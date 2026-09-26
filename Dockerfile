# Stage 1: Build the Spring Boot application using Maven Wrapper
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Cache Maven dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Production runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root application user for cloud security best practice
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Cloud providers (Render, Railway, Fly.io, etc.) automatically inject $PORT
ENV PORT=8085
EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8085} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-mysql} -jar app.jar"]
