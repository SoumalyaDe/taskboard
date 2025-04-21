FROM amazoncorretto:21-alpine

LABEL maintainer="admin"

WORKDIR /app

COPY build/libs/taskboard-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

