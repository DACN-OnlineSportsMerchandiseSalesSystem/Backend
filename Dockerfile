# --- STAGE 1: Build the application ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy the pom.xml and download project dependencies to cache them
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and compile the application jar (skipping unit tests)
COPY src ./src
RUN mvn clean package -DskipTests

# --- STAGE 2: Run the application ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cài đặt thêm các thư viện C++ và glibc tương thích ngược (compatibility libraries)
# giúp mô hình AI thật (ONNX Runtime / Embedding Model) chạy trơn tru trên Docker Alpine.
RUN apk add --no-cache libc6-compat gcompat libstdc++

# Copy the built jar from the builder stage and rename it to app.jar
COPY --from=builder /app/target/backend-dacn-1.0.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
