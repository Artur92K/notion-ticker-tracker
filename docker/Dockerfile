# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies first (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Set timezone to Warsaw
ENV TZ=Europe/Warsaw
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 21801
EXPOSE 21801

# Set Java timezone and run the application
CMD ["java", "-Duser.timezone=Europe/Warsaw", "-jar", "app.jar"]
