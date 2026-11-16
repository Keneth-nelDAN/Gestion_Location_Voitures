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
public class UtilisateurJpaController implements Serializable {

    public UtilisateurJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Utilisateur utilisateur) throws RollbackFailureException, Exception {
        if (utilisateur.getLocationList() == null) {
            utilisateur.setLocationList(new ArrayList<Location>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<Location> attachedLocationList = new ArrayList<Location>();
            for (Location locationListLocationToAttach : utilisateur.getLocationList()) {
                locationListLocationToAttach = em.getReference(locationListLocationToAttach.getClass(), locationListLocationToAttach.getIdLocation());
                attachedLocationList.add(locationListLocationToAttach);
            }
            utilisateur.setLocationList(attachedLocationList);
            em.persist(utilisateur);
            for (Location locationListLocation : utilisateur.getLocationList()) {
                Utilisateur oldIdUtilisateurOfLocationListLocation = locationListLocation.getIdUtilisateur();
                locationListLocation.setIdUtilisateur(utilisateur);
                locationListLocation = em.merge(locationListLocation);
                if (oldIdUtilisateurOfLocationListLocation != null) {
                    oldIdUtilisateurOfLocationListLocation.getLocationList().remove(locationListLocation);
                    oldIdUtilisateurOfLocationListLocation = em.merge(oldIdUtilisateurOfLocationListLocation);
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

    public void edit(Utilisateur utilisateur) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Utilisateur persistentUtilisateur = em.find(Utilisateur.class, utilisateur.getIdUtilisateur());
            List<Location> locationListOld = persistentUtilisateur.getLocationList();
            List<Location> locationListNew = utilisateur.getLocationList();
            List<String> illegalOrphanMessages = null;
            for (Location locationListOldLocation : locationListOld) {
                if (!locationListNew.contains(locationListOldLocation)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Location " + locationListOldLocation + " since its idUtilisateur field is not nullable.");
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
            utilisateur.setLocationList(locationListNew);
            utilisateur = em.merge(utilisateur);
            for (Location locationListNewLocation : locationListNew) {
                if (!locationListOld.contains(locationListNewLocation)) {
                    Utilisateur oldIdUtilisateurOfLocationListNewLocation = locationListNewLocation.getIdUtilisateur();
                    locationListNewLocation.setIdUtilisateur(utilisateur);
                    locationListNewLocation = em.merge(locationListNewLocation);
                    if (oldIdUtilisateurOfLocationListNewLocation != null && !oldIdUtilisateurOfLocationListNewLocation.equals(utilisateur)) {
                        oldIdUtilisateurOfLocationListNewLocation.getLocationList().remove(locationListNewLocation);
                        oldIdUtilisateurOfLocationListNewLocation = em.merge(oldIdUtilisateurOfLocationListNewLocation);
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
                Integer id = utilisateur.getIdUtilisateur();
                if (findUtilisateur(id) == null) {
                    throw new NonexistentEntityException("The utilisateur with id " + id + " no longer exists.");
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
            Utilisateur utilisateur;
            try {
                utilisateur = em.getReference(Utilisateur.class, id);
                utilisateur.getIdUtilisateur();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The utilisateur with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Location> locationListOrphanCheck = utilisateur.getLocationList();
            for (Location locationListOrphanCheckLocation : locationListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Utilisateur (" + utilisateur + ") cannot be destroyed since the Location " + locationListOrphanCheckLocation + " in its locationList field has a non-nullable idUtilisateur field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(utilisateur);
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

    public List<Utilisateur> findUtilisateurEntities() {
        return findUtilisateurEntities(true, -1, -1);
    }

    public List<Utilisateur> findUtilisateurEntities(int maxResults, int firstResult) {
        return findUtilisateurEntities(false, maxResults, firstResult);
    }

    private List<Utilisateur> findUtilisateurEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Utilisateur.class));
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

    public Utilisateur findUtilisateur(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Utilisateur.class, id);
        } finally {
            em.close();
        }
    }

    public int getUtilisateurCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Utilisateur> rt = cq.from(Utilisateur.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
