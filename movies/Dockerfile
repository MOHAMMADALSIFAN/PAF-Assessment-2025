# First Stage - Build
FROM maven:3.9.9-eclipse-temurin-23 AS builder
ARG COMPILE_DIR=/compiledir
WORKDIR ${COMPILE_DIR}

# Copy necessary files for Maven
COPY mvnw .
COPY pom.xml .
COPY .mvn .mvn
COPY src src

# Copy additional resources
COPY src/main/resources/data/director_movies_report.jasper /app/director_movies_report.jasper
COPY src/main/resources/data/movies_post_2010.zip /app/movies_post_2010.zip

# Set executable permission for mvnw
RUN chmod +x mvnw

# Run Maven to build the project
RUN ./mvnw clean package -DskipTests

# Second Stage - Runtime
FROM eclipse-temurin:23-jdk
ARG WORK_DIR=/app
WORKDIR ${WORK_DIR}

# Copy the application jar from the build stage
COPY --from=builder /compiledir/target/movies-0.0.1-SNAPSHOT.jar events.jar

# Also copy necessary data files from the builder stage
COPY --from=builder /app/director_movies_report.jasper ${WORK_DIR}/director_movies_report.jasper
COPY --from=builder /app/movies_post_2010.zip ${WORK_DIR}/movies_post_2010.zip

# Set environment variables
ENV SERVER_PORT=3000

# Expose the application port
EXPOSE ${SERVER_PORT}

# Command to run the application
ENTRYPOINT ["java", "-jar", "events.jar"]

# run
# docker build -t cihansifan/anyname:v0.0.1 .

# container
# docker run -p 8085:8080 cihansifan/anyname:v0.0.1

