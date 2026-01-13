# 1. Aşama: Uygulamayı Derle (Maven ve Java 17 kullanarak)
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# 2. Aşama: Uygulamayı Çalıştır
FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/UniVerse-0.0.1-SNAPSHOT.jar universe.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","universe.jar"]