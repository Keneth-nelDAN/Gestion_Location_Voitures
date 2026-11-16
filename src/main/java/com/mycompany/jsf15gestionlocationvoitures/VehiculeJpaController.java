/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.jsf15gestionlocationvoitures;

import com.mycompany.jsf15gestionlocationvoitures.exceptions.IllegalOrphanException;
import com.mycompany.jsf15gestionlocationvoitures.exceptions.NonexistentEntityException;
import com.mycompany.jsf15gestionlocationvoitures.exceptions.RollbackFailureException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author keneth
 */
public class VehiculeJpaController implements Serializable {

    public VehiculeJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Vehicule vehicule) throws RollbackFailureException, Exception {
        if (vehicule.getLocationList() == null) {
            vehicule.setLocationList(new ArrayList<Location>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<Location> attachedLocationList = new ArrayList<Location>();
            for (Location locationListLocationToAttach : vehicule.getLocationList()) {
                locationListLocationToAttach = em.getReference(locationListLocationToAttach.getClass(), locationListLocationToAttach.getIdLocation());
                attachedLocationList.add(locationListLocationToAttach);
            }
            vehicule.setLocationList(attachedLocationList);
            em.persist(vehicule);
            for (Location locationListLocation : vehicule.getLocationList()) {
                Vehicule oldIdVehiculeOfLocationListLocation = locationListLocation.getIdVehicule();
                locationListLocation.setIdVehicule(vehicule);
                locationListLocation = em.merge(locationListLocation);
                if (oldIdVehiculeOfLocationListLocation != null) {
                    oldIdVehiculeOfLocationListLocation.getLocationList().remove(locationListLocation);
                    oldIdVehiculeOfLocationListLocation = em.merge(oldIdVehiculeOfLocationListLocation);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Vehicule vehicule) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Vehicule persistentVehicule = em.find(Vehicule.class, vehicule.getIdVehicule());
            List<Location> locationListOld = persistentVehicule.getLocationList();
            List<Location> locationListNew = vehicule.getLocationList();
            List<String> illegalOrphanMessages = null;
            for (Location locationListOldLocation : locationListOld) {
                if (!locationListNew.contains(locationListOldLocation)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Location " + locationListOldLocation + " since its idVehicule field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Location> attachedLocationListNew = new ArrayList<Location>();
            for (Location locationListNewLocationToAttach : locationListNew) {
                locationListNewLocationToAttach = em.getReference(locationListNewLocationToAttach.getClass(), locationListNewLocationToAttach.getIdLocation());
                attachedLocationListNew.add(locationListNewLocationToAttach);
            }
            locationListNew = attachedLocationListNew;
            vehicule.setLocationList(locationListNew);
            vehicule = em.merge(vehicule);
            for (Location locationListNewLocation : locationListNew) {
                if (!locationListOld.contains(locationListNewLocation)) {
                    Vehicule oldIdVehiculeOfLocationListNewLocation = locationListNewLocation.getIdVehicule();
                    locationListNewLocation.setIdVehicule(vehicule);
                    locationListNewLocation = em.merge(locationListNewLocation);
                    if (oldIdVehiculeOfLocationListNewLocation != null && !oldIdVehiculeOfLocationListNewLocation.equals(vehicule)) {
                        oldIdVehiculeOfLocationListNewLocation.getLocationList().remove(locationListNewLocation);
                        oldIdVehiculeOfLocationListNewLocation = em.merge(oldIdVehiculeOfLocationListNewLocation);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = vehicule.getIdVehicule();
                if (findVehicule(id) == null) {
                    throw new NonexistentEntityException("The vehicule with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Vehicule vehicule;
            try {
                vehicule = em.getReference(Vehicule.class, id);
                vehicule.getIdVehicule();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The vehicule with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Location> locationListOrphanCheck = vehicule.getLocationList();
            for (Location locationListOrphanCheckLocation : locationListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Vehicule (" + vehicule + ") cannot be destroyed since the Location " + locationListOrphanCheckLocation + " in its locationList field has a non-nullable idVehicule field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(vehicule);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Vehicule> findVehiculeEntities() {
        return findVehiculeEntities(true, -1, -1);
    }

    public List<Vehicule> findVehiculeEntities(int maxResults, int firstResult) {
        return findVehiculeEntities(false, maxResults, firstResult);
    }

    private List<Vehicule> findVehiculeEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Vehicule.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Vehicule findVehicule(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Vehicule.class, id);
        } finally {
            em.close();
        }
    }

    public int getVehiculeCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Vehicule> rt = cq.from(Vehicule.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
