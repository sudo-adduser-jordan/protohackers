# # Use GraalVM as the base image
# FROM ghcr.io/graalvm/graalvm-ce:latest

# # Install necessary tools
# RUN apt-get update && apt-get install -y \
#     unzip \
#     git \
#     && rm -rf /var/lib/apt/lists/*

# # Set working directory
# WORKDIR /app

# # Copy Gradle wrapper (if present) and project files
# COPY . .

# # Build the project (assuming Gradle wrapper is used)
# RUN bash ./gradlew build --no-daemon

# # Expose the application port (replace 8080 with your app's port)
# EXPOSE 8080

# # Run the application (replace with your actual start command)
# CMD ["java", "-jar", "build/libs/your-app.jar"]