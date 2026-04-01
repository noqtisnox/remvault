# Stage 1: Build the JAR using JDK 21
FROM gradle:8.5-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew :server:shadowJar -x test --no-daemon

# Stage 2: Run the JAR in a standard Ubuntu-based JRE 21 environment
FROM eclipse-temurin:21-jre
EXPOSE 8080
WORKDIR /app
# Copy the compiled "fat jar" from the build stage
COPY --from=build /home/gradle/src/server/build/libs/*-all.jar /app/ktor-server.jar

ENTRYPOINT ["java", "-jar", "/app/ktor-server.jar"]