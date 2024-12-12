# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml /app
COPY src /app/src
RUN mvn clean package -DskipTests

# Package stage
FROM eclipse-temurin:17-jre
COPY --from=build /app/target/DATN_STF_BE-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar
EXPOSE 8000
ENTRYPOINT ["java", "--enable-preview", "-jar", "/usr/local/lib/app.jar"]
