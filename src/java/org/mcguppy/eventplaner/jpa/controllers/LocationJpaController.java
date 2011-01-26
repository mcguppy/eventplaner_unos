package org.mcguppy.eventplaner.jpa.controllers;

import java.util.List;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.UserTransaction;
import org.mcguppy.eventplaner.jpa.controllers.exceptions.PreexistingEntityException;
import org.mcguppy.eventplaner.jpa.controllers.exceptions.RollbackFailureException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import org.mcguppy.eventplaner.jpa.controllers.exceptions.NonexistentEntityException;
import org.mcguppy.eventplaner.jpa.entities.Location;
import org.mcguppy.eventplaner.jpa.entities.Shift;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author mbohm
 */
public class LocationJpaController {

    @Resource
    private UserTransaction utx = null;
    @PersistenceUnit(unitName = "enventPlanerPU")
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Location location) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (location.getShifts() == null) {
            location.setShifts(new ArrayList<Shift>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<Shift> attachedShiftCollection = new ArrayList<Shift>();
            for (Shift shiftToAttach : location.getShifts()) {
                shiftToAttach = em.getReference(shiftToAttach.getClass(), shiftToAttach.getId());
                attachedShiftCollection.add(shiftToAttach);
            }
            location.setShifts(attachedShiftCollection);
            em.persist(location);
            for (Shift shift : location.getShifts()) {
                shift.setLocation(location);
                shift = em.merge(shift);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException(ResourceBundle.getBundle("/Bundle").getString("TransactionRollBackError"), re);
            }
            if (findLocation(location.getId()) != null) {
                throw new PreexistingEntityException("Location " + location + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Location location) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Location persistentLocation = em.find(Location.class, location.getId());
            Collection<Shift> shiftCollectionOld = persistentLocation.getShifts();
            Collection<Shift> shiftCollectionNew = location.getShifts();
            List<Shift> attachedShiftCollectionNew = new ArrayList<Shift>();
            for (Shift shiftCollectionNewShiftToAttach : shiftCollectionNew) {
                shiftCollectionNewShiftToAttach = em.getReference(shiftCollectionNewShiftToAttach.getClass(), shiftCollectionNewShiftToAttach.getId());
                attachedShiftCollectionNew.add(shiftCollectionNewShiftToAttach);
            }
            shiftCollectionNew = attachedShiftCollectionNew;
            location.setShifts(shiftCollectionNew);
            location = em.merge(location);
            for (Shift shiftCollectionNewShift : shiftCollectionNew) {
                if (!shiftCollectionOld.contains(shiftCollectionNewShift)) {
                    shiftCollectionNewShift.setLocation(location);
                    shiftCollectionNewShift = em.merge(shiftCollectionNewShift);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException(ResourceBundle.getBundle("/Bundle").getString("TransactionRollBackError"), re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = location.getId();
                if (findLocation(id) == null) {
                    throw new NonexistentEntityException(ResourceBundle.getBundle("/Bundle").getString("LocationMissing"));
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Location location) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Location persistentLocation = null;
            try {
                persistentLocation = em.getReference(Location.class, location.getId());
                persistentLocation.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException(ResourceBundle.getBundle("/Bundle").getString("MissingLocation"), enfe);
            }

            for (Shift shiftToRemove : persistentLocation.getShifts()) {
                for (StaffMember staffMemberToRemoveShift : shiftToRemove.getStaffMembers()) {
                    if (staffMemberToRemoveShift.getShifts().contains(shiftToRemove)) {
                        staffMemberToRemoveShift.getShifts().remove(shiftToRemove);
                        em.merge(staffMemberToRemoveShift);
                    }
                }
                em.remove(shiftToRemove);

            }
            em.remove(persistentLocation);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException(ResourceBundle.getBundle("/Bundle").getString("TransactionRollBackError"), re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Location> findLocationEntities() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Location as o");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Location findLocation(Long id) {
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
            return ((Long) em.createQuery("select count(o) from Location as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
