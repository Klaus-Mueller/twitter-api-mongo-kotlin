# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Install Maven
RUN apt-get update && \
    apt-get install -y maven

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml file and the source code directory to the container
COPY pom.xml /app
COPY src /app/src

# Build the application
RUN mvn clean package

# Verify the JAR file location
RUN ls target

# Expose the port the application runs on
EXPOSE 4567

# Run the application
CMD ["java", "-jar", "target/twitter-api-mongo-kotlin-1.0-SNAPSHOT.jar"]
