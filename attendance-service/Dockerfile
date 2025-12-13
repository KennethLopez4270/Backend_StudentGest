# =====================================================
# Dockerfile para attendance-service
# Puerto: 8082
# =====================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiar Maven wrapper y archivos de configuración
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución al Maven wrapper
RUN chmod +x mvnw

# Descargar dependencias (capa cacheada)
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src src

# Construir el proyecto
RUN ./mvnw clean package -DskipTests

# =====================================================
# Imagen de producción
# =====================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el JAR desde la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto
EXPOSE 8082

# Variables de entorno para la base de datos
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/studentgest
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=ABCabc123

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
