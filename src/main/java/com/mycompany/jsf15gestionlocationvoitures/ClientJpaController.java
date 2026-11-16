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
public class ClientJpaController implements Serializable {

    public ClientJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Client client) throws RollbackFailureException, Exception {
        if (client.getLocationList() == null) {
            client.setLocationList(new ArrayList<Location>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<Location> attachedLocationList = new ArrayList<Location>();
            for (Location locationListLocationToAttach : client.getLocationList()) {
                locationListLocationToAttach = em.getReference(locationListLocationToAttach.getClass(), locationListLocationToAttach.getIdLocation());
                attachedLocationList.add(locationListLocationToAttach);
            }
            client.setLocationList(attachedLocationList);
            em.persist(client);
            for (Location locationListLocation : client.getLocationList()) {
                Client oldIdClientOfLocationListLocation = locationListLocation.getIdClient();
                locationListLocation.setIdClient(client);
                locationListLocation = em.merge(locationListLocation);
                if (oldIdClientOfLocationListLocation != null) {
                    oldIdClientOfLocationListLocation.getLocationList().remove(locationListLocation);
                    oldIdClientOfLocationListLocation = em.merge(oldIdClientOfLocationListLocation);
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

    public void edit(Client client) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Client persistentClient = em.find(Client.class, client.getIdClient());
            List<Location> locationListOld = persistentClient.getLocationList();
            List<Location> locationListNew = client.getLocationList();
            List<String> illegalOrphanMessages = null;
            for (Location locationListOldLocation : locationListOld) {
                if (!locationListNew.contains(locationListOldLocation)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Location " + locationListOldLocation + " since its idClient field is not nullable.");
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
            client.setLocationList(locationListNew);
            client = em.merge(client);
            for (Location locationListNewLocation : locationListNew) {
                if (!locationListOld.contains(locationListNewLocation)) {
                    Client oldIdClientOfLocationListNewLocation = locationListNewLocation.getIdClient();
                    locationListNewLocation.setIdClient(client);
                    locationListNewLocation = em.merge(locationListNewLocation);
                    if (oldIdClientOfLocationListNewLocation != null && !oldIdClientOfLocationListNewLocation.equals(client)) {
                        oldIdClientOfLocationListNewLocation.getLocationList().remove(locationListNewLocation);
                        oldIdClientOfLocationListNewLocation = em.merge(oldIdClientOfLocationListNewLocation);
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
                Integer id = client.getIdClient();
                if (findClient(id) == null) {
                    throw new NonexistentEntityException("The client with id " + id + " no longer exists.");
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
            Client client;
            try {
                client = em.getReference(Client.class, id);
                client.getIdClient();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The client with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Location> locationListOrphanCheck = client.getLocationList();
            for (Location locationListOrphanCheckLocation : locationListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Client (" + client + ") cannot be destroyed since the Location " + locationListOrphanCheckLocation + " in its locationList field has a non-nullable idClient field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(client);
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

    public List<Client> findClientEntities() {
        return findClientEntities(true, -1, -1);
    }

    public List<Client> findClientEntities(int maxResults, int firstResult) {
        return findClientEntities(false, maxResults, firstResult);
    }

    private List<Client> findClientEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Client.class));
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

    public Client findClient(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Client.class, id);
        } finally {
            em.close();
        }
    }

    public int getClientCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Client> rt = cq.from(Client.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
