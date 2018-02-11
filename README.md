# Payconiq-stock-api
Stock api assignment

## Metrics 
Default user pass is admin/admin
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

## Code coverage
can be reported by below command under **${project.basedir}/target/site/cobertura/index.html**. 
```bash
mvn cobertura:cobertura
```
