# FROM openjdk:17-jdk-alpine
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp 
## when define `ARG` like this, you need to provide corresponding `ARG` when build the image (e.g. --build-arg JAR_FILE=?)
ARG JAR_FILE
## OR you can directly define the arg and its value here
# ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} /tdd-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/tdd-0.0.1-SNAPSHOT.jar"]

# NOTE:
# Keep in mind that you will need to provide the JAR file for your Spring or Spring Boot MVC application 
# as the JAR_FILE build-time variable when building the Docker image. For example:

# E.g(create executable jar file):
# 1. mvn clean package
# 2. java -jar target/<app_name>-1.0.0.jar

# docker build -t <image-name> -f Dockerfile --build-arg JAR_FILE=app.jar .
# docker run -p 8080:8080 <container-name>