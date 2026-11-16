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
public class LocationJpaController implements Serializable {

    public LocationJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Location location) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Retour retour = location.getRetour();
            if (retour != null) {
                retour = em.getReference(retour.getClass(), retour.getIdRetour());
                location.setRetour(retour);
            }
            Client idClient = location.getIdClient();
            if (idClient != null) {
                idClient = em.getReference(idClient.getClass(), idClient.getIdClient());
                location.setIdClient(idClient);
            }
            Utilisateur idUtilisateur = location.getIdUtilisateur();
            if (idUtilisateur != null) {
                idUtilisateur = em.getReference(idUtilisateur.getClass(), idUtilisateur.getIdUtilisateur());
                location.setIdUtilisateur(idUtilisateur);
            }
            Vehicule idVehicule = location.getIdVehicule();
            if (idVehicule != null) {
                idVehicule = em.getReference(idVehicule.getClass(), idVehicule.getIdVehicule());
                location.setIdVehicule(idVehicule);
            }
            em.persist(location);
            if (retour != null) {
                Location oldIdLocationOfRetour = retour.getIdLocation();
                if (oldIdLocationOfRetour != null) {
                    oldIdLocationOfRetour.setRetour(null);
                    oldIdLocationOfRetour = em.merge(oldIdLocationOfRetour);
                }
                retour.setIdLocation(location);
                retour = em.merge(retour);
            }
            if (idClient != null) {
                idClient.getLocationList().add(location);
                idClient = em.merge(idClient);
            }
            if (idUtilisateur != null) {
                idUtilisateur.getLocationList().add(location);
                idUtilisateur = em.merge(idUtilisateur);
            }
            if (idVehicule != null) {
                idVehicule.getLocationList().add(location);
                idVehicule = em.merge(idVehicule);
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

    public void edit(Location location) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Location persistentLocation = em.find(Location.class, location.getIdLocation());
            Retour retourOld = persistentLocation.getRetour();
            Retour retourNew = location.getRetour();
            Client idClientOld = persistentLocation.getIdClient();
            Client idClientNew = location.getIdClient();
            Utilisateur idUtilisateurOld = persistentLocation.getIdUtilisateur();
            Utilisateur idUtilisateurNew = location.getIdUtilisateur();
            Vehicule idVehiculeOld = persistentLocation.getIdVehicule();
            Vehicule idVehiculeNew = location.getIdVehicule();
            List<String> illegalOrphanMessages = null;
            if (retourOld != null && !retourOld.equals(retourNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain Retour " + retourOld + " since its idLocation field is not nullable.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (retourNew != null) {
                retourNew = em.getReference(retourNew.getClass(), retourNew.getIdRetour());
                location.setRetour(retourNew);
            }
            if (idClientNew != null) {
                idClientNew = em.getReference(idClientNew.getClass(), idClientNew.getIdClient());
                location.setIdClient(idClientNew);
            }
            if (idUtilisateurNew != null) {
                idUtilisateurNew = em.getReference(idUtilisateurNew.getClass(), idUtilisateurNew.getIdUtilisateur());
                location.setIdUtilisateur(idUtilisateurNew);
            }
            if (idVehiculeNew != null) {
                idVehiculeNew = em.getReference(idVehiculeNew.getClass(), idVehiculeNew.getIdVehicule());
                location.setIdVehicule(idVehiculeNew);
            }
            location = em.merge(location);
            if (retourNew != null && !retourNew.equals(retourOld)) {
                Location oldIdLocationOfRetour = retourNew.getIdLocation();
                if (oldIdLocationOfRetour != null) {
                    oldIdLocationOfRetour.setRetour(null);
                    oldIdLocationOfRetour = em.merge(oldIdLocationOfRetour);
                }
                retourNew.setIdLocation(location);
                retourNew = em.merge(retourNew);
            }
            if (idClientOld != null && !idClientOld.equals(idClientNew)) {
                idClientOld.getLocationList().remove(location);
                idClientOld = em.merge(idClientOld);
            }
            if (idClientNew != null && !idClientNew.equals(idClientOld)) {
                idClientNew.getLocationList().add(location);
                idClientNew = em.merge(idClientNew);
            }
            if (idUtilisateurOld != null && !idUtilisateurOld.equals(idUtilisateurNew)) {
                idUtilisateurOld.getLocationList().remove(location);
                idUtilisateurOld = em.merge(idUtilisateurOld);
            }
            if (idUtilisateurNew != null && !idUtilisateurNew.equals(idUtilisateurOld)) {
                idUtilisateurNew.getLocationList().add(location);
                idUtilisateurNew = em.merge(idUtilisateurNew);
            }
            if (idVehiculeOld != null && !idVehiculeOld.equals(idVehiculeNew)) {
                idVehiculeOld.getLocationList().remove(location);
                idVehiculeOld = em.merge(idVehiculeOld);
            }
            if (idVehiculeNew != null && !idVehiculeNew.equals(idVehiculeOld)) {
                idVehiculeNew.getLocationList().add(location);
                idVehiculeNew = em.merge(idVehiculeNew);
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
                Integer id = location.getIdLocation();
                if (findLocation(id) == null) {
                    throw new NonexistentEntityException("The location with id " + id + " no longer exists.");
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
            Location location;
            try {
                location = em.getReference(Location.class, id);
                location.getIdLocation();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The location with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Retour retourOrphanCheck = location.getRetour();
            if (retourOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Location (" + location + ") cannot be destroyed since the Retour " + retourOrphanCheck + " in its retour field has a non-nullable idLocation field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Client idClient = location.getIdClient();
            if (idClient != null) {
                idClient.getLocationList().remove(location);
                idClient = em.merge(idClient);
            }
            Utilisateur idUtilisateur = location.getIdUtilisateur();
            if (idUtilisateur != null) {
                idUtilisateur.getLocationList().remove(location);
                idUtilisateur = em.merge(idUtilisateur);
            }
            Vehicule idVehicule = location.getIdVehicule();
            if (idVehicule != null) {
                idVehicule.getLocationList().remove(location);
                idVehicule = em.merge(idVehicule);
            }
            em.remove(location);
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

    public List<Location> findLocationEntities() {
        return findLocationEntities(true, -1, -1);
    }

    public List<Location> findLocationEntities(int maxResults, int firstResult) {
        return findLocationEntities(false, maxResults, firstResult);
    }

    private List<Location> findLocationEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Location.class));
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

    public Location findLocation(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Location.class, id);
        } finally {
            em.close();
        }
    }

    public int getLocationCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Location> rt = cq.from(Location.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
