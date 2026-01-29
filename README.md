
## 1. Prerequisite
- Install JDK 21+ (if not already installed)
- Install Maven 3.9+ (if not already installed)
- Install IntelliJ (if not already installed)
- Install Docker (if not already installed)

## 2. Technical Stacks
- Java 21
- Maven 3.9.10+
- Spring Boot 4.0.2
- Spring Data Validation
- Spring Data JPA
- Postgres/MySQL (optional)
- Lombok
- DevTools
- Docker
- Docker compose


## 3. Build & Run Application
– Run the application using mvnw in the backend-service folder.
```
$ ./mvnw spring-boot:run
```

– Run application by docker
```
$ mvn clean install -P dev
$ docker build -t backend-service:latest .
$ docker run -it -p 8080:8080 --name backend-service backend-service:latest
```

## 4. Test
– Check your health with cURL
```
curl --location 'http://localhost:8080/actuator/health'
```
-- Response --
```
{
    "status": "UP"
}
```