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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author keneth
 */
@Entity
@Table(name = "location")
@NamedQueries({
    @NamedQuery(name = "Location.findAll", query = "SELECT l FROM Location l"),
    @NamedQuery(name = "Location.findByIdLocation", query = "SELECT l FROM Location l WHERE l.idLocation = :idLocation"),
    @NamedQuery(name = "Location.findByDateCreation", query = "SELECT l FROM Location l WHERE l.dateCreation = :dateCreation"),
    @NamedQuery(name = "Location.findByDateDebutPrevue", query = "SELECT l FROM Location l WHERE l.dateDebutPrevue = :dateDebutPrevue"),
    @NamedQuery(name = "Location.findByDateFinPrevue", query = "SELECT l FROM Location l WHERE l.dateFinPrevue = :dateFinPrevue"),
    @NamedQuery(name = "Location.findByDateDebutEffective", query = "SELECT l FROM Location l WHERE l.dateDebutEffective = :dateDebutEffective"),
    @NamedQuery(name = "Location.findByTarifJournalierApplique", query = "SELECT l FROM Location l WHERE l.tarifJournalierApplique = :tarifJournalierApplique"),
    @NamedQuery(name = "Location.findByMontantEstime", query = "SELECT l FROM Location l WHERE l.montantEstime = :montantEstime"),
    @NamedQuery(name = "Location.findByStatutLocation", query = "SELECT l FROM Location l WHERE l.statutLocation = :statutLocation")})
public class Location implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_location")
    private Integer idLocation;
    @Column(name = "date_creation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;
    @Column(name = "date_debut_prevue")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDebutPrevue;
    @Column(name = "date_fin_prevue")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFinPrevue;
    @Column(name = "date_debut_effective")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDebutEffective;
    @Column(name = "tarif_journalier_applique")
    private Integer tarifJournalierApplique;
    @Column(name = "montant_estime")
    private Integer montantEstime;
    @Size(max = 50)
    @Column(name = "statut_location")
    private String statutLocation;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "idLocation")
    private Retour retour;
    @JoinColumn(name = "id_client", referencedColumnName = "id_client")
    @ManyToOne(optional = false)
    private Client idClient;
    @JoinColumn(name = "id_utilisateur", referencedColumnName = "id_utilisateur")
    @ManyToOne(optional = false)
    private Utilisateur idUtilisateur;
    @JoinColumn(name = "id_vehicule", referencedColumnName = "id_vehicule")
    @ManyToOne(optional = false)
    private Vehicule idVehicule;

    public Location() {
    }

    public Location(Integer idLocation) {
        this.idLocation = idLocation;
    }

    public Integer getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(Integer idLocation) {
        this.idLocation = idLocation;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateDebutPrevue() {
        return dateDebutPrevue;
    }

    public void setDateDebutPrevue(Date dateDebutPrevue) {
        this.dateDebutPrevue = dateDebutPrevue;
    }

    public Date getDateFinPrevue() {
        return dateFinPrevue;
    }

    public void setDateFinPrevue(Date dateFinPrevue) {
        this.dateFinPrevue = dateFinPrevue;
    }

    public Date getDateDebutEffective() {
        return dateDebutEffective;
    }

    public void setDateDebutEffective(Date dateDebutEffective) {
        this.dateDebutEffective = dateDebutEffective;
    }

    public Integer getTarifJournalierApplique() {
        return tarifJournalierApplique;
    }

    public void setTarifJournalierApplique(Integer tarifJournalierApplique) {
        this.tarifJournalierApplique = tarifJournalierApplique;
    }

    public Integer getMontantEstime() {
        return montantEstime;
    }

    public void setMontantEstime(Integer montantEstime) {
        this.montantEstime = montantEstime;
    }

    public String getStatutLocation() {
        return statutLocation;
    }

    public void setStatutLocation(String statutLocation) {
        this.statutLocation = statutLocation;
    }

    public Retour getRetour() {
        return retour;
    }

    public void setRetour(Retour retour) {
        this.retour = retour;
    }

    public Client getIdClient() {
        return idClient;
    }

    public void setIdClient(Client idClient) {
        this.idClient = idClient;
    }

    public Utilisateur getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Utilisateur idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public Vehicule getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(Vehicule idVehicule) {
        this.idVehicule = idVehicule;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idLocation != null ? idLocation.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Location)) {
            return false;
        }
        Location other = (Location) object;
        if ((this.idLocation == null && other.idLocation != null) || (this.idLocation != null && !this.idLocation.equals(other.idLocation))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String label = "Location #" + idLocation;
        if (statutLocation != null) {
            label += " - " + statutLocation;
        }
        return label;
    }
    
}
