FROM quay.io/wildfly/wildfly:latest

USER root
RUN microdnf install -y curl && microdnf clean all

RUN mkdir -p /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main && \
    curl -fsSL -o /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/postgresql-42.7.4.jar \
    https://jdbc.postgresql.org/download/postgresql-42.7.4.jar

RUN printf '%s\n' \
    '<?xml version="1.0" encoding="UTF-8"?>' \
    '<module xmlns="urn:jboss:module:1.9" name="org.postgresql">' \
    '    <resources>' \
    '        <resource-root path="postgresql-42.7.4.jar"/>' \
    '    </resources>' \
    '    <dependencies>' \
    '        <module name="java.sql"/>' \
    '        <module name="java.naming"/>' \
    '        <module name="jakarta.transaction.api"/>' \
    '    </dependencies>' \
    '</module>' \
    > /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/module.xml

RUN set -e; \
    /opt/jboss/wildfly/bin/standalone.sh -b 127.0.0.1 -bmanagement 127.0.0.1 >/tmp/wildfly-build.log 2>&1 & \
    SERVER_PID=$!; \
    trap 'kill $SERVER_PID 2>/dev/null || true' EXIT; \
    for i in $(seq 1 30); do \
      if /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command=":read-attribute(name=server-state)" 2>/dev/null | grep -q "running"; then \
        break; \
      fi; \
      sleep 1; \
    done; \
    /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command="/subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-datasource-class-name=org.postgresql.ds.PGSimpleDataSource)"; \
    /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command='data-source add --name=LocaDriveDS --jndi-name=java:jboss/datasources/LocaDriveDS --driver-name=postgresql --connection-url="jdbc:postgresql://ep-frosty-feather-bet5jpua-pooler.c-9.us-east-2.aws.neon.tech:5432/LocaDrive?sslmode=require" --user-name=neondb_owner --password=npg_xCVmEL93zkJw'; \
    /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command=":shutdown"; \
    wait $SERVER_PID; \
    trap - EXIT; \
    rm -f /tmp/wildfly-build.log

COPY target/JSF15GestionLocationVoitures-2.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/ROOT.war

EXPOSE 8080
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]