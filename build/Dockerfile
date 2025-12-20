FROM maven:3.9.12-eclipse-temurin-21 AS m2-cache

# copy all poms and project root files. See .dockerignore
COPY . /usr/src/mvn
RUN chown -R ubuntu /usr/src/mvn
WORKDIR /usr/src/mvn
USER ubuntu
# downloads dependencies ('mvn dependency:go-offline' is not working with tycho)
RUN mvn --batch-mode --threads 4 validate --settings maven/config/settings.xml

FROM maven:3.9.12-eclipse-temurin-21

COPY --from=m2-cache /home/ubuntu/.m2/ /home/ubuntu/.m2/
