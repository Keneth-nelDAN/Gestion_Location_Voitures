package com.mycompany.jsf15gestionlocationvoitures;

import com.mycompany.jsf15gestionlocationvoitures.util.JsfUtil;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.UserTransaction;

@Named("clientEspaceController")
@SessionScoped
public class ClientEspaceController implements Serializable {

    @Resource
    private UserTransaction utx = null;
    @PersistenceUnit(unitName = "my_persistence_unit")
    private EntityManagerFactory emf = null;

    private Location current;
    private Integer clientId;

    public ClientEspaceController() {
    }

    public Location getSelected() {
        if (current == null) {
            current = new Location();
        }
        return current;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public List<Vehicule> getVehiculesDisponibles() {
        List<Vehicule> disponibles = new ArrayList<Vehicule>();
        for (Vehicule vehicule : new VehiculeJpaController(utx, emf).findVehiculeEntities()) {
            if (isDisponible(vehicule)) {
                disponibles.add(vehicule);
            }
        }
        return disponibles;
    }

    public List<Client> getClients() {
        return new ClientJpaController(utx, emf).findClientEntities();
    }

    public String prepareDemande() {
        String vehiculeId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("vehiculeId");
        if (vehiculeId == null || vehiculeId.isEmpty()) {
            JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("ClientRequestVehicleUnavailable"));
            return null;
        }
        Vehicule vehicule;
        try {
            vehicule = new VehiculeJpaController(utx, emf).findVehicule(Integer.valueOf(vehiculeId));
        } catch (NumberFormatException e) {
            JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("ClientRequestVehicleUnavailable"));
            return null;
        }
        if (!isDisponible(vehicule)) {
            JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("ClientRequestVehicleUnavailable"));
            return null;
        }
        current = new Location();
        current.setIdVehicule(vehicule);
        current.setDateCreation(new Date());
        current.setTarifJournalierApplique(vehicule.getTarifJournalier());
        current.setStatutLocation("En attente");
        return "/espace-client/Demande";
    }

    public String createDemande() {
        ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");
        if (clientId == null) {
            JsfUtil.addErrorMessage(bundle.getString("ClientRequestClientRequired"));
            return null;
        }
        Client client = new ClientJpaController(utx, emf).findClient(clientId);
        if (client == null) {
            JsfUtil.addErrorMessage(bundle.getString("ClientRequestClientRequired"));
            return null;
        }
        if (current == null || current.getIdVehicule() == null) {
            JsfUtil.addErrorMessage(bundle.getString("ClientRequestVehicleUnavailable"));
            return null;
        }
        Vehicule vehicule = new VehiculeJpaController(utx, emf).findVehicule(current.getIdVehicule().getIdVehicule());
        if (!isDisponible(vehicule)) {
            JsfUtil.addErrorMessage(bundle.getString("ClientRequestVehicleUnavailable"));
            return null;
        }
        Utilisateur validateur = findValidateur();
        if (validateur == null) {
            JsfUtil.addErrorMessage(bundle.getString("ClientRequestNoValidator"));
            return null;
        }
        current.setIdClient(client);
        current.setIdUtilisateur(validateur);
        current.setIdVehicule(vehicule);
        current.setTarifJournalierApplique(vehicule.getTarifJournalier());
        current.setStatutLocation("En attente");
        if (current.getDateCreation() == null) {
            current.setDateCreation(new Date());
        }
        calculerMontant();
        try {
            new LocationJpaController(utx, emf).create(current);
            JsfUtil.addSuccessMessage(bundle.getString("ClientRequestCreated"));
            current = new Location();
            return "/espace-client/Vehicules";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, bundle.getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public boolean isDisponible(Vehicule vehicule) {
        if (vehicule == null) {
            return false;
        }
        String statut = normalizeStatut(vehicule.getStatutDisponible());
        if (statut.isEmpty() || statut.equals("disponible") || statut.equals("oui") || statut.equals("libre") || statut.equals("true")) {
            return true;
        }
        return statut.contains("disponible") && !statut.contains("indisponible") && !statut.contains("non");
    }

    private void calculerMontant() {
        if (current.getDateDebutPrevue() == null || current.getDateFinPrevue() == null || current.getTarifJournalierApplique() == null) {
            return;
        }
        long jours = (current.getDateFinPrevue().getTime() - current.getDateDebutPrevue().getTime()) / 86400000L;
        if (jours < 1) {
            jours = 1;
        }
        current.setMontantEstime((int) (jours * current.getTarifJournalierApplique()));
    }

    private Utilisateur findValidateur() {
        for (Utilisateur utilisateur : new UtilisateurJpaController(utx, emf).findUtilisateurEntities()) {
            String role = normalizeStatut(utilisateur.getRole());
            if (role.isEmpty() || role.equals("utilisateur") || role.equals("admin") || role.equals("administrateur")) {
                return utilisateur;
            }
        }
        return null;
    }

    private String normalizeStatut(String statut) {
        if (statut == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(statut, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase(Locale.ROOT).trim();
    }
}
