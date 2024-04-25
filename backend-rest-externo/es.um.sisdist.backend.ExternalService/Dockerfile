FROM tomcat:10.1-jdk11

EXPOSE 8180
RUN sed -i 's/port="8080"/port="8180"/' /usr/local/tomcat/conf/server.xml
COPY target/ExternalService-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ExternalService.war

