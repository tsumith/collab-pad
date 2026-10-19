# Stage 1: Build the JAR with Maven
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lean Alpine JRE runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Render Free Tier Memory Optimization:
# -XX:+UseContainerSupport enables the JVM to detect container cgroup RAM limits
# -XX:MaxRAMPercentage=75.0 limits heap to ~384MB on Render's 512MB RAM instance
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Copy compiled jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Render dynamically injects $PORT (defaults to 8081 if unset)
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
