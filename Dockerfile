FROM quay.io/wildfly/wildfly:latest

USER root
RUN microdnf install -y curl python3 && microdnf clean all

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
    /opt/jboss/wildfly/bin/jboss-cli.sh --connect --command=":shutdown"; \
    wait $SERVER_PID; \
    trap - EXIT; \
    rm -f /tmp/wildfly-build.log

RUN cat <<'EOF' > /tmp/patch_ds.py && python3 /tmp/patch_ds.py && rm -f /tmp/patch_ds.py
path = "/opt/jboss/wildfly/standalone/configuration/standalone.xml"
content = open(path, "r", encoding="utf-8").read()
ds_xml = """
                <datasource jndi-name="java:jboss/datasources/LocaDriveDS" pool-name="LocaDriveDS" enabled="true" use-java-context="true">
                    <connection-url>jdbc:postgresql://ep-frosty-feather-bet5jpua-pooler.c-9.us-east-2.aws.neon.tech:5432/LocaDrive?sslmode=require</connection-url>
                    <driver>postgresql</driver>
                    <security>
                        <user-name>neondb_owner</user-name>
                        <password>npg_xCVmEL93zkJw</password>
                    </security>
                    <validation>
                        <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.novendor.JDBCValidConnectionChecker"/>
                        <validate-on-match>true</validate-on-match>
                        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter"/>
                    </validation>
                </datasource>"""
target = "</datasource>"
idx = content.find(target)
assert idx != -1, "Balise </datasource> introuvable"
insert_pos = idx + len(target)
new_content = content[:insert_pos] + ds_xml + content[insert_pos:]
open(path, "w", encoding="utf-8").write(new_content)
EOF

COPY target/JSF15GestionLocationVoitures-2.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/ROOT.war

ENV JAVA_OPTS="-Xms128m -Xmx768m"

EXPOSE 8080
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]