# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml /app
COPY src /app/src
RUN mvn dependency:go-offline
RUN mvn clean package -DskipTests

# Package stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /usr/local/lib

# Tạo user không root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/DATN_STF_BE-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar
COPY static /app/static

ARG APP_PORT=8080
ENV APP_PORT=${APP_PORT}
EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "--enable-preview", "-jar", "/usr/local/lib/app.jar"]
