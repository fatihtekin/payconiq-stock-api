FROM openjdk:8-jre-alpine
COPY ./application.yml /payconiq-stock-api/
COPY ./target/payconiq-stock-api-0.1.0-SNAPSHOT-exec.jar /payconiq-stock-api/
WORKDIR /payconiq-stock-api
EXPOSE 8080
CMD ["java", "-jar", "-Dspring.config.location=file:///./application.yml","payconiq-stock-api-0.1.0-SNAPSHOT-exec.jar"]
