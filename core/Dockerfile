FROM openjdk:21-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw package -DskipTests

FROM openjdk:21-jdk

WORKDIR /app

RUN mkdir -p /app/question-images && \
    chmod 777 /app/question-images

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

VOLUME /app/question-images

ENTRYPOINT ["java", "-jar", "/app/app.jar"]