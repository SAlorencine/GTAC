FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests
CMD ["sh", "-c", "java -jar target/*.jar"]FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests
CMD ["sh", "-c", "java -jar target/*.jar"]
