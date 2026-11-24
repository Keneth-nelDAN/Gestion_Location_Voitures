package com.mycompany.jsf15gestionlocationvoitures;

import com.mycompany.jsf15gestionlocationvoitures.util.JsfUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.UserTransaction;

@Named("loginController")
@SessionScoped
public class LoginController implements Serializable {

    public static final String SESSION_USER = "locadrive.utilisateur";
    public static final String SESSION_ROLE = "locadrive.role";

    @Resource
    private UserTransaction utx;
    @PersistenceUnit(unitName = "my_persistence_unit")
    private EntityManagerFactory emf;

    private String email;
    private String motDePasse;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public boolean isLogged() {
        return getEmailConnecte() != null;
    }

    public String getEmailConnecte() {
        return (String) sessionMap().get(SESSION_USER);
    }

    public String getRoleConnecte() {
        return (String) sessionMap().get(SESSION_ROLE);
    }

    public String login() {
        ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");
        if (email == null || email.trim().isEmpty() || motDePasse == null || motDePasse.isEmpty()) {
            JsfUtil.addErrorMessage(bundle.getString("LoginError"));
            return null;
        }

        Utilisateur utilisateur = null;
        for (Utilisateur candidat : new UtilisateurJpaController(utx, emf).findUtilisateurEntities()) {
            if (email.trim().equalsIgnoreCase(candidat.getEmail())) {
                utilisateur = candidat;
                break;
            }
        }

        if (utilisateur == null || !Objects.equals(utilisateur.getMotDePasse(), motDePasse)) {
            JsfUtil.addErrorMessage(bundle.getString("LoginError"));
            return null;
        }

        Map<String, Object> session = sessionMap();
        session.put(SESSION_USER, utilisateur.getEmail());
        session.put(SESSION_ROLE, normaliserRole(utilisateur.getRole()));
        return redirect("/faces/index.xhtml");
    }

    public String redirectIfLogged() {
        if (!isLogged()) {
            return null;
        }
        String role = getRoleConnecte();
        return redirect("CLIENT".equals(role) ? "/faces/espace-client/Vehicules.xhtml" : "/faces/index.xhtml");
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return redirect("/faces/login.xhtml");
    }

    public static String normaliserRole(String role) {
        if (role == null) {
            return "UTILISATEUR";
        }
        String valeur = role.trim().toUpperCase();
        if (valeur.contains("ADMIN")) {
            return "ADMIN";
        }
        if (valeur.contains("CLIENT")) {
            return "CLIENT";
        }
        return "UTILISATEUR";
    }

    private Map<String, Object> sessionMap() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
    }

    private String redirect(String chemin) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        try {
            externalContext.redirect(externalContext.getRequestContextPath() + chemin);
        } catch (IOException e) {
            return null;
        }
        FacesContext.getCurrentInstance().responseComplete();
        return null;
    }
}
