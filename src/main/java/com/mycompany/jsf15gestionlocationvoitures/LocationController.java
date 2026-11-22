package com.mycompany.jsf15gestionlocationvoitures;

import com.mycompany.jsf15gestionlocationvoitures.util.JsfUtil;
import com.mycompany.jsf15gestionlocationvoitures.util.PaginationHelper;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import jakarta.annotation.Resource;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.SelectItem;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.UserTransaction;

@Named("locationController")
@SessionScoped
public class LocationController implements Serializable {

    @Resource
    private UserTransaction utx = null;
    @PersistenceUnit(unitName = "my_persistence_unit")
    private EntityManagerFactory emf = null;

    private Location current;
    private DataModel items = null;
    private LocationJpaController jpaController = null;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public LocationController() {
    }

    public Location getSelected() {
        if (current == null) {
            current = new Location();
            selectedItemIndex = -1;
        }
        return current;
    }

    public void setSelected(Location location) {
        current = location;
    }

    private LocationJpaController getJpaController() {
        if (jpaController == null) {
            jpaController = new LocationJpaController(utx, emf);
        }
        return jpaController;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getJpaController().getLocationCount();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().findLocationEntities(getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Location) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Location();
        current.setDateCreation(new Date());
        current.setStatutLocation("En attente");
        current.setIdUtilisateur(findValidateur());
        selectedItemIndex = -1;
        return "Create";
    }

    public String prepareCreateFromVehicule() {
        String vehiculeId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("vehiculeId");
        if (vehiculeId == null || vehiculeId.length() == 0) {
            JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("LocationVehicleUnavailable"));
            return null;
        }
        Vehicule vehicule = null;
        try {
            vehicule = new VehiculeJpaController(utx, emf).findVehicule(Integer.valueOf(vehiculeId));
        } catch (NumberFormatException e) {
            JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("LocationVehicleUnavailable"));
            return null;
        }
        if (!isDisponible(vehicule)) {
            JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("LocationVehicleUnavailable"));
            return null;
        }
        current = new Location();
        current.setIdVehicule(vehicule);
        current.setDateCreation(new Date());
        current.setStatutLocation("En cours");
        if (vehicule.getTarifJournalier() != null) {
            current.setTarifJournalierApplique(vehicule.getTarifJournalier());
        }
        current.setIdUtilisateur(findValidateur());
        selectedItemIndex = -1;
        return "/location/Create";
    }

    public String create() {
        ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");
        try {
            if (!isDisponible(current.getIdVehicule())) {
                JsfUtil.addErrorMessage(bundle.getString("LocationVehicleUnavailable"));
                return null;
            }
            if (!datesCoherentes()) {
                JsfUtil.addErrorMessage(bundle.getString("LocationDateOrder"));
                return null;
            }
            if (current.getTarifJournalierApplique() == null && current.getIdVehicule() != null
                    && current.getIdVehicule().getTarifJournalier() != null) {
                current.setTarifJournalierApplique(current.getIdVehicule().getTarifJournalier());
            }
            calculerMontant();
            getJpaController().create(current);
            new VehiculeJpaController(utx, emf).updateStatutDisponible(current.getIdVehicule().getIdVehicule(), "Indisponible");
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LocationCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String valider() {
        ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");
        try {
            current.setStatutLocation("En cours");
            getJpaController().edit(current);
            new VehiculeJpaController(utx, emf).updateStatutDisponible(current.getIdVehicule().getIdVehicule(), "Indisponible");
            JsfUtil.addSuccessMessage(bundle.getString("LocationValidated"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, bundle.getString("LocationValidationError"));
        }
        recreateModel();
        return "List";
    }

    public boolean isPending(Location location) {
        if (location == null) {
            return false;
        }
        String statut = normalizeStatut(location.getStatutLocation());
        return statut.contains("attente") || statut.contains("enattente");
    }

    public String prepareEdit() {
        current = (Location) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");
        try {
            if (!datesCoherentes()) {
                JsfUtil.addErrorMessage(bundle.getString("LocationDateOrder"));
                return null;
            }
            calculerMontant();
            getJpaController().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LocationUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Location) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().destroy(current.getIdLocation());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LocationDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().getLocationCount();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getJpaController().findLocationEntities(1, selectedItemIndex).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(getJpaController().findLocationEntities(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(getJpaController().findLocationEntities(), true);
    }

    private boolean datesCoherentes() {
        Date debut = current.getDateDebutPrevue();
        Date fin = current.getDateFinPrevue();
        return debut == null || fin == null || !fin.before(debut);
    }

    private void calculerMontant() {
        Date debut = current.getDateDebutPrevue();
        Date fin = current.getDateFinPrevue();
        Integer tarif = current.getTarifJournalierApplique();
        if (debut == null || fin == null || tarif == null) {
            current.setMontantEstime(null);
            return;
        }
        long jours = (fin.getTime() - debut.getTime()) / 86400000L;
        if (jours < 1) {
            jours = 1;
        }
        current.setMontantEstime((int) (jours * tarif));
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

    private boolean isDisponible(Vehicule vehicule) {
        if (vehicule == null) {
            return false;
        }
        String statut = normalizeStatut(vehicule.getStatutDisponible());
        if (statut.isEmpty() || statut.equals("disponible") || statut.equals("oui") || statut.equals("libre") || statut.equals("true")) {
            return true;
        }
        return statut.contains("disponible") && !statut.contains("indisponible") && !statut.contains("non");
    }

    private String normalizeStatut(String statut) {
        if (statut == null) {
            return "";
        }
        String value = Normalizer.normalize(statut, Normalizer.Form.NFD);
        return value.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase(Locale.ROOT).trim();
    }

    @FacesConverter(forClass = Location.class)
    public static class LocationControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            LocationController controller = (LocationController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "locationController");
            return controller.getJpaController().findLocation(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Location) {
                Location o = (Location) object;
                return getStringKey(o.getIdLocation());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Location.class.getName());
            }
        }

    }

}
