FROM openjdk:8-jre-alpine
COPY ./application.yml /payconiq-stock-api/
COPY ./target/payconiq-stock-api-*-exec.jar /payconiq-stock-api/
WORKDIR /payconiq-stock-api
EXPOSE 8080
CMD ["sh", "-c", "java -jar ${JAVA_OPTS} -Dspring.config.location=file:///./application.yml $(echo payconiq-stock-api-*-exec.jar)"]
