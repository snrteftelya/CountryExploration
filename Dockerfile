FROM maven:3-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
# загрузка зависимостей
RUN mvn clean verify --fail-never -DskipTests
COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:21-alpine

WORKDIR /app

RUN apk --no-cache add curl

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]