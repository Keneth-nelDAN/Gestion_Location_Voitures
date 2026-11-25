
# Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
# Click nbfs://nbhost/SystemFileSystem/Templates/Other/Dockerfile to edit this template

FROM quay.io/wildfly/wildfly:latest

USER root
RUN microdnf install -y curl unzip && microdnf clean all

RUN mkdir -p /tmp/pg && curl -fsSL -o /tmp/pg/postgresql.jar \
    https://jdbc.postgresql.org/download/postgresql-42.7.4.jar

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --connect=false <<'EOF'
embed-server --std-out=echo
module add --name=org.postgresql --resources=/tmp/pg/postgresql.jar --dependencies=jakarta.api,jakarta.transaction.api
/subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-datasource-class-name=org.postgresql.ds.PGSimpleDataSource)
data-source add --name=LocaDriveDS \
    --jndi-name=java:jboss/datasources/LocaDriveDS \
    --driver-name=postgresql \
    --connection-url="jdbc:postgresql://ep-frosty-feather-bet5jpua-pooler.c-9.us-east-2.aws.neon.tech:5432/LocaDrive?sslmode=require" \
    --user-name=neondb_owner \
    --password=npg_xCVmEL93zkJw
stop-embedded-server
EOF

RUN rm -rf /tmp/pg

COPY target/JSF15GestionLocationVoitures-2.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/ROOT.war

EXPOSE 8080
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]