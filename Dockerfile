FROM tomcat:10-jdk17-openjdk-slim

# Copiar el compilado generado por NetBeans a la carpeta de despliegue de Tomcat
COPY dist/API_HealthBlood.war /usr/local/tomcat/webapps/API_HealthBlood.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
