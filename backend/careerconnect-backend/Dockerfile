# Use OpenJDK image
FROM openjdk:17-jdk-slim

# Set work directory
WORKDIR /app

# Copy and build the app
COPY . /app
RUN ./mvnw clean package -DskipTests

# Run the jar
CMD ["java", "-jar", "target/careerconnect-backend-0.0.1-SNAPSHOT.jar"]
