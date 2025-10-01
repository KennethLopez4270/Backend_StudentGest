@echo off
echo Iniciando microservicios en diferentes ventanas...

start cmd /k "cd attendance-service && mvn spring-boot:run"
start cmd /k "cd foro-service && mvn spring-boot:run"
start cmd /k "cd homework-service && mvn spring-boot:run"
start cmd /k "cd notificaciones-service && mvn spring-boot:run"
start cmd /k "cd student-service && mvn spring-boot:run"
start cmd /k "cd user-service && mvn spring-boot:run"
start cmd /k "cd student-gateway && mvn spring-boot:run"

echo Todos los microservicios fueron iniciados.
pause
