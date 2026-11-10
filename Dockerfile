FROM tomcat:10-jdk17-openjdk-slim

# Limpiar aplicaciones por defecto de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar el WAR como ROOT.war para que responda directamente en la raíz /
COPY dist/API_HealthBlood.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]