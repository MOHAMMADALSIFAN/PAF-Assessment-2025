
FROM maven:3.9.9-eclipse-temurin-23 AS builder

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

FROM maven:3.9.9-eclipse-temurin-23

WORKDIR /app
COPY --from=builder /app/target/movies-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/src/main/resources/data

COPY --from=builder /app/src/main/resources/data/* /app/src/main/resources/data/

ENV PORT=8080
EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
# run
# docker build -t cihansifan/anyname:v0.0.1 .

# container
# docker run -p 8085:8080 cihansifan/anyname:v0.0.1

