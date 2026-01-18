FROM eclipse-temurin:11-jre

WORKDIR /app

COPY build/libs/*.war app.war

EXPOSE 8080

CMD ["java", "-jar", "app.war"]