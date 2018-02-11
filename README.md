# Payconiq-stock-api
Stock api assignment
Default user pass is `admin/admin` as in `application.yml`

## Metrics 
```properties
http://localhost:8080/metrics
```

## Swagger

```properties
http://localhost:8080/swagger-ui.html
```

## Config
Security can be disabled by setting management.security.enabled to "false" in application.yml file
```properties
-Dspring.config.location=file:///./application.yml
```

## Docker
```bash
docker build -f Dockerfile.app -t stock-api .
docker run -p 8080:8080 stock-api
```

## Build
```bash
mvn clean package
```

##Run from spring boot plugin from root folder of the project 
```bash
mvn spring-boot:run -Dspring.config.location=file:///./application.yml
```

## Code coverage
can be reported by below command under /target/site/jacoco/index.html. 
```bash
mvn clean package site
```

React setup part is inspired from [https://github.com/spring-guides/tut-react-and-spring-data-rest](https://github.com/spring-guides/tut-react-and-spring-data-rest)

Things that could be improved
Pagination for /api/stock
Better security options other than hardcoded in application.yml file
CI/CD depending on github or gitlab
Better look and feel and css for user interface