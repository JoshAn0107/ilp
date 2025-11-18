FROM --platform=linux/amd64 openjdk:21

# Expose the port that the app is going to use.
EXPOSE 8080
#Set the working directory for the following commands.
WORKDIR /app
# Copy the JAR file from the repo into the image.
COPY ./target/ilp_submission_1-0.0.1-SNAPSHOT.jar app.jar

# Command that starts the app when the container starts.

ENTRYPOINT ["java", "-jar", "app.jar"]