package com.mycompany.jsf15gestionlocationvoitures;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;

@Named("securityController")
@RequestScoped
public class SecurityController implements Serializable {

    @Inject
    private LoginController loginController;

    public boolean isStaff() {
        return "ADMIN".equals(loginController.getRoleConnecte())
                || "UTILISATEUR".equals(loginController.getRoleConnecte());
    }

    public boolean isClient() {
        return "CLIENT".equals(loginController.getRoleConnecte());
    }

    public String getRemoteUser() {
        String utilisateur = loginController.getEmailConnecte();
        return utilisateur == null || utilisateur.isEmpty() ? "Invité" : utilisateur;
    }

    public String redirectAfterLogin() {
        String cible;
        if (isStaff()) {
            cible = "/faces/index.xhtml";
        } else if (isClient()) {
            cible = "/faces/espace-client/Vehicules.xhtml";
        } else {
            cible = "/faces/login.xhtml";
        }
        return redirect(cible);
    }

    public String logout() {
        return loginController.logout();
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
