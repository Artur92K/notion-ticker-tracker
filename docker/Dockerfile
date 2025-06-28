# Build stage
FROM maven:3.9-openjdk-21-slim AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean compile package -DskipTests

# Runtime stage
FROM openjdk:21-jre-slim

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 21801
EXPOSE 21801

# Run the application
CMD ["java", "-jar", "app.jar"]
