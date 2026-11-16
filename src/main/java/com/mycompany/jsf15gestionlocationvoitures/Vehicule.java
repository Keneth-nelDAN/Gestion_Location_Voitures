/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.jsf15gestionlocationvoitures;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author keneth
 */
@Entity
@Table(name = "vehicule")
@NamedQueries({
    @NamedQuery(name = "Vehicule.findAll", query = "SELECT v FROM Vehicule v"),
    @NamedQuery(name = "Vehicule.findByIdVehicule", query = "SELECT v FROM Vehicule v WHERE v.idVehicule = :idVehicule"),
    @NamedQuery(name = "Vehicule.findByImmatriculation", query = "SELECT v FROM Vehicule v WHERE v.immatriculation = :immatriculation"),
    @NamedQuery(name = "Vehicule.findByMarque", query = "SELECT v FROM Vehicule v WHERE v.marque = :marque"),
    @NamedQuery(name = "Vehicule.findByModele", query = "SELECT v FROM Vehicule v WHERE v.modele = :modele"),
    @NamedQuery(name = "Vehicule.findByCategorie", query = "SELECT v FROM Vehicule v WHERE v.categorie = :categorie"),
    @NamedQuery(name = "Vehicule.findByCouleur", query = "SELECT v FROM Vehicule v WHERE v.couleur = :couleur"),
    @NamedQuery(name = "Vehicule.findByKilometrage", query = "SELECT v FROM Vehicule v WHERE v.kilometrage = :kilometrage"),
    @NamedQuery(name = "Vehicule.findByTarifJournalier", query = "SELECT v FROM Vehicule v WHERE v.tarifJournalier = :tarifJournalier"),
    @NamedQuery(name = "Vehicule.findByEtatVehicule", query = "SELECT v FROM Vehicule v WHERE v.etatVehicule = :etatVehicule"),
    @NamedQuery(name = "Vehicule.findByStatutDisponible", query = "SELECT v FROM Vehicule v WHERE v.statutDisponible = :statutDisponible")})
public class Vehicule implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_vehicule")
    private Integer idVehicule;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "immatriculation")
    private String immatriculation;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "marque")
    private String marque;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "modele")
    private String modele;
    @Column(name = "categorie")
    private Integer categorie;
    @Size(max = 50)
    @Column(name = "couleur")
    private String couleur;
    @Column(name = "kilometrage")
    private Integer kilometrage;
    @Column(name = "tarif_journalier")
    private Integer tarifJournalier;
    @Size(max = 100)
    @Column(name = "etat_vehicule")
    private String etatVehicule;
    @Size(max = 50)
    @Column(name = "statut_disponible")
    private String statutDisponible;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idVehicule")
    private List<Location> locationList;

    public Vehicule() {
    }

    public Vehicule(Integer idVehicule) {
        this.idVehicule = idVehicule;
    }

    public Vehicule(Integer idVehicule, String immatriculation, String marque, String modele) {
        this.idVehicule = idVehicule;
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
    }

    public Integer getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(Integer idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public Integer getCategorie() {
        return categorie;
    }

    public void setCategorie(Integer categorie) {
        this.categorie = categorie;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public Integer getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(Integer kilometrage) {
        this.kilometrage = kilometrage;
    }

    public Integer getTarifJournalier() {
        return tarifJournalier;
    }

    public void setTarifJournalier(Integer tarifJournalier) {
        this.tarifJournalier = tarifJournalier;
    }

    public String getEtatVehicule() {
        return etatVehicule;
    }

    public void setEtatVehicule(String etatVehicule) {
        this.etatVehicule = etatVehicule;
    }

    public String getStatutDisponible() {
        return statutDisponible;
    }

    public void setStatutDisponible(String statutDisponible) {
        this.statutDisponible = statutDisponible;
    }

    public List<Location> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<Location> locationList) {
        this.locationList = locationList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idVehicule != null ? idVehicule.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Vehicule)) {
            return false;
        }
        Vehicule other = (Vehicule) object;
        if ((this.idVehicule == null && other.idVehicule != null) || (this.idVehicule != null && !this.idVehicule.equals(other.idVehicule))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.jsf15gestionlocationvoitures.Vehicule[ idVehicule=" + idVehicule + " ]";
    }
    
}
