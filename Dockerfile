# FROM openjdk:17-jdk-alpine
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp 

## when define `ARG` like this, you need to provide corresponding `ARG` when build the image (e.g. --build-arg JAR_FILE=?)
ARG JAR_FILE
ARG DB_SOURCE_PW
ARG DB_SOURCE_URL
ARG DB_SOURCE_USERNAME

ENV SPRING_DATASOURCE_PASSWORD=${DB_SOURCE_PW}
ENV SPRING_DATASOURCE_URL=${DB_SOURCE_URL}
ENV SPRING_DATASOURCE_USERNAME=${DB_SOURCE_USERNAME}

COPY ${JAR_FILE} /tdd-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java","-jar","/tdd-0.0.1-SNAPSHOT.jar"]

# NOTE:
# Keep in mind that you will need to provide the JAR file for your Spring or Spring Boot MVC application 
# as the JAR_FILE build-time variable when building the Docker image. For example:

# E.g (create executable jar file):
# 1. mvn clean package (this will created a <app_name>.jar file located in ./target)
# 2. java -jar target/<app_name>-1.0.0.jar (optional)
