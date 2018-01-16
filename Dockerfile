FROM openjdk:8-jdk-alpine

MAINTAINER jm5619

RUN mkdir /app

WORKDIR /app

ADD ./target/ir-recommendation-1.0.0-SNAPSHOT.jar /app

EXPOSE 8087

CMD ["java", "-jar", "ir-recommendation-1.0.0-SNAPSHOT.jar"]
