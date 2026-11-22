/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.jsf15gestionlocationvoitures;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "retour")
@NamedQueries({
    @NamedQuery(name = "Retour.findAll", query = "SELECT r FROM Retour r"),
    @NamedQuery(name = "Retour.findByIdRetour", query = "SELECT r FROM Retour r WHERE r.idRetour = :idRetour"),
    @NamedQuery(name = "Retour.findByDateRetourEffective", query = "SELECT r FROM Retour r WHERE r.dateRetourEffective = :dateRetourEffective"),
    @NamedQuery(name = "Retour.findByKilometrageRetour", query = "SELECT r FROM Retour r WHERE r.kilometrageRetour = :kilometrageRetour"),
    @NamedQuery(name = "Retour.findByEtatAuRetour", query = "SELECT r FROM Retour r WHERE r.etatAuRetour = :etatAuRetour"),
    @NamedQuery(name = "Retour.findByObservations", query = "SELECT r FROM Retour r WHERE r.observations = :observations")})
public class Retour implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_retour")
    private Integer idRetour;
    @Column(name = "date_retour_effective")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateRetourEffective;
    @Column(name = "kilometrage_retour")
    private Integer kilometrageRetour;
    @Size(max = 2147483647)
    @Column(name = "etat_au_retour")
    private String etatAuRetour;
    @Size(max = 2147483647)
    @Column(name = "observations")
    private String observations;
    @JoinColumn(name = "id_location", referencedColumnName = "id_location")
    @OneToOne(optional = false)
    private Location idLocation;

    public Retour() {
    }

    public Retour(Integer idRetour) {
        this.idRetour = idRetour;
    }

    public Integer getIdRetour() {
        return idRetour;
    }

    public void setIdRetour(Integer idRetour) {
        this.idRetour = idRetour;
    }

    public Date getDateRetourEffective() {
        return dateRetourEffective;
    }

    public void setDateRetourEffective(Date dateRetourEffective) {
        this.dateRetourEffective = dateRetourEffective;
    }

    public Integer getKilometrageRetour() {
        return kilometrageRetour;
    }

    public void setKilometrageRetour(Integer kilometrageRetour) {
        this.kilometrageRetour = kilometrageRetour;
    }

    public String getEtatAuRetour() {
        return etatAuRetour;
    }

    public void setEtatAuRetour(String etatAuRetour) {
        this.etatAuRetour = etatAuRetour;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public Location getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(Location idLocation) {
        this.idLocation = idLocation;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idRetour != null ? idRetour.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Retour)) {
            return false;
        }
        Retour other = (Retour) object;
        if ((this.idRetour == null && other.idRetour != null) || (this.idRetour != null && !this.idRetour.equals(other.idRetour))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Retour #" + idRetour;
    }
    
}
