FROM maven:3.9.6 as build

WORKDIR /app

COPY src ./src
COPY pom.xml .

RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine3.15-jdk

WORKDIR /app

COPY --from=build /app .

CMD ["java", "-jar", "target/g1-0.0.1-SNAPSHOT.jar"]

EXPOSE 9999