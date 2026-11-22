package com.mycompany.jsf15gestionlocationvoitures;

import java.io.IOException;
import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

@Named("securityController")
@RequestScoped
public class SecurityController implements Serializable {

    public boolean isStaff() {
        return isUserInRole("ADMIN") || isUserInRole("UTILISATEUR");
    }

    public boolean isClient() {
        return isUserInRole("CLIENT");
    }

    public String getRemoteUser() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String user = externalContext.getRemoteUser();
        return user == null || user.isEmpty() ? "Invité" : user;
    }

    public String redirectAfterLogin() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String cible;
        if (isStaff()) {
            cible = "/faces/index.xhtml";
        } else if (isClient()) {
            cible = "/faces/espace-client/Vehicules.xhtml";
        } else {
            cible = "/faces/login.xhtml";
        }
        try {
            externalContext.redirect(externalContext.getRequestContextPath() + cible);
        } catch (IOException e) {
            return null;
        }
        FacesContext.getCurrentInstance().responseComplete();
        return null;
    }

    public String logout() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Object request = externalContext.getRequest();
        try {
            if (request instanceof HttpServletRequest) {
                ((HttpServletRequest) request).logout();
            }
        } catch (ServletException e) {
            return null;
        }
        externalContext.invalidateSession();
        try {
            externalContext.redirect(externalContext.getRequestContextPath() + "/faces/login.xhtml");
        } catch (IOException e) {
            return null;
        }
        FacesContext.getCurrentInstance().responseComplete();
        return null;
    }

    private boolean isUserInRole(String role) {
        return FacesContext.getCurrentInstance().getExternalContext().isUserInRole(role);
    }
}
