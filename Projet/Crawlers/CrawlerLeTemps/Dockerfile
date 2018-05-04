FROM maven:3.3-jdk-8-onbuild 

FROM java:8
COPY --from=0 /usr/src/app/target/letemps_scrapper-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/letemps.jar
CMD ["java","-jar","/opt/letemps.jar"]
