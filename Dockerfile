# Utiliser l'image officielle WildFly
FROM quay.io/wildfly/wildfly:latest

# Copier ton fichier WAR généré dans le répertoire de déploiement automatique de WildFly
# Remplace "NomDeTonProjet.war" par le vrai nom de ton fichier WAR
COPY target/JSF15GestionLocationVoitures-2.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/ROOT.war

# Exposer le port 8080 pour le web
EXPOSE 8080

# Lancer WildFly en mode public pour qu'il soit accessible sur le cloud
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
