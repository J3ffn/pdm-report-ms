FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Usuário não-root para segurança do container
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copia o JAR gerado no build
COPY target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
