Objetivo
Desarrollar un servicio backend resiliente, seguro y escalable que gestione operaciones sobre una
entidad de "Empleado", siguiendo prácticas modernas de desarrollo, arquitectura de microservicios y
aplicando los principios de DevSecOps.

---
Requerimientos
1. Funcionalidades del API REST:
- GET /employees: Devuelve el listado de todos los empleados registrados.
- GET /employees/{id}: Recupera el detalle de un empleado por su ID.
- POST /employees: Permite insertar uno o varios empleados en una misma petición.
- PUT /employees/{id}: Actualiza todos o algunos de los campos de un empleado.
- DELETE /employees/{id}: Elimina un empleado por su ID.
- GET /employees/search?name={name}: Buscar empleados por nombre (búsqueda parcial).
2. Modelo de Datos “Empleado”:
- Primer nombre
- Segundo nombre
- Apellido paterno
- Apellido materno
- Edad
- Sexo
- Fecha de nacimiento (formato dd-MM-yyyy)
- Puesto
- Fecha de alta en sistema (timestamp automático)
- Estado activo/inactivo (boolean)
3. Requisitos Técnicos:
- Java 11 o Java 17
- Spring Boot 2.7.x o Quarkus
- Spring Data JPA / Hibernate
- Base de datos Oracle o MySQL (o H2 para pruebas)
- JSON request/response
- Excepciones customizadas
- Swagger / OpenAPI
- Pruebas unitarias con JUnit 5 y Mockito
- Configuración de logs via YAML o properties
4. Buenas Prácticas Esperadas:
- POO
- Programación funcional
- SOLID
- Arquitectura tipo microservicio
- Validaciones de datos
- Manejo correcto de HTTP Status
- Limpieza de código
- Uso de constantes
- Git Flow
5. Extras Opcionales:
- Dockerización del proyecto
- Seguridad con Spring Security
- Pipeline básico de CI/CD
- Health Check (actuator)
- Postman Collection
- Documentación de contrato API
- Registro de headers en logs

6. Tiempo de Entrega:
- Deberás tenerlo listo o hasta donde puedas cubrir la prueba, hasta presentarlo en la entrevista
  Técnica
7. Entregables:
- Código fuente (GitHub o repositorio PRIVADO O PERSONAL)
- README de instalación, ejecución y pruebas
- Evidencias de funcionamiento (capturas o grabaciones)
- Dockerfile y/o Jenkinsfile si aplica
---
Evaluación Principal:
- Cumplimiento de requerimientos funcionales
- Calidad y estructura del código
- Validaciones y manejo de errores
- Diseño de APIs y documentación
- Calidad de pruebas unitarias
- Prácticas de DevSecOps

Notas Adicionales
- No se evaluará la UI
- El código debe estar escrito en inglés