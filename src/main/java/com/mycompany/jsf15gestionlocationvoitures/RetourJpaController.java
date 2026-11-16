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
public class RetourJpaController implements Serializable {

    public RetourJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Retour retour) throws IllegalOrphanException, RollbackFailureException, Exception {
        List<String> illegalOrphanMessages = null;
        Location idLocationOrphanCheck = retour.getIdLocation();
        if (idLocationOrphanCheck != null) {
            Retour oldRetourOfIdLocation = idLocationOrphanCheck.getRetour();
            if (oldRetourOfIdLocation != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The Location " + idLocationOrphanCheck + " already has an item of type Retour whose idLocation column cannot be null. Please make another selection for the idLocation field.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Location idLocation = retour.getIdLocation();
            if (idLocation != null) {
                idLocation = em.getReference(idLocation.getClass(), idLocation.getIdLocation());
                retour.setIdLocation(idLocation);
            }
            em.persist(retour);
            if (idLocation != null) {
                idLocation.setRetour(retour);
                idLocation = em.merge(idLocation);
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

    public void edit(Retour retour) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Retour persistentRetour = em.find(Retour.class, retour.getIdRetour());
            Location idLocationOld = persistentRetour.getIdLocation();
            Location idLocationNew = retour.getIdLocation();
            List<String> illegalOrphanMessages = null;
            if (idLocationNew != null && !idLocationNew.equals(idLocationOld)) {
                Retour oldRetourOfIdLocation = idLocationNew.getRetour();
                if (oldRetourOfIdLocation != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The Location " + idLocationNew + " already has an item of type Retour whose idLocation column cannot be null. Please make another selection for the idLocation field.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (idLocationNew != null) {
                idLocationNew = em.getReference(idLocationNew.getClass(), idLocationNew.getIdLocation());
                retour.setIdLocation(idLocationNew);
            }
            retour = em.merge(retour);
            if (idLocationOld != null && !idLocationOld.equals(idLocationNew)) {
                idLocationOld.setRetour(null);
                idLocationOld = em.merge(idLocationOld);
            }
            if (idLocationNew != null && !idLocationNew.equals(idLocationOld)) {
                idLocationNew.setRetour(retour);
                idLocationNew = em.merge(idLocationNew);
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
                Integer id = retour.getIdRetour();
                if (findRetour(id) == null) {
                    throw new NonexistentEntityException("The retour with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Retour retour;
            try {
                retour = em.getReference(Retour.class, id);
                retour.getIdRetour();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The retour with id " + id + " no longer exists.", enfe);
            }
            Location idLocation = retour.getIdLocation();
            if (idLocation != null) {
                idLocation.setRetour(null);
                idLocation = em.merge(idLocation);
            }
            em.remove(retour);
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

    public List<Retour> findRetourEntities() {
        return findRetourEntities(true, -1, -1);
    }

    public List<Retour> findRetourEntities(int maxResults, int firstResult) {
        return findRetourEntities(false, maxResults, firstResult);
    }

    private List<Retour> findRetourEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Retour.class));
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

    public Retour findRetour(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Retour.class, id);
        } finally {
            em.close();
        }
    }

    public int getRetourCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Retour> rt = cq.from(Retour.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
