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
public class ShiftJpaController {

    @Resource
    private UserTransaction utx = null;
    @PersistenceUnit(unitName = "enventPlanerPU")
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Shift shift) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (shift.getStaffMembers() == null) {
            shift.setStaffMembers(new ArrayList<StaffMember>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();

            Location location = shift.getLocation();
            if (location != null) {
                location = em.getReference(location.getClass(), location.getId());
                shift.setLocation(location);
            }

            List<StaffMember> attachedStaffMemberCollection = new ArrayList<StaffMember>();
            for (StaffMember staffMemberToAttach : shift.getStaffMembers()) {
                staffMemberToAttach = em.getReference(staffMemberToAttach.getClass(), staffMemberToAttach.getId());
                attachedStaffMemberCollection.add(staffMemberToAttach);
            }
            shift.setStaffMembers(attachedStaffMemberCollection);
            em.persist(shift);

            if (location != null) {
                location.getShifts().add(shift);
                location = em.merge(location);
            }

            for (StaffMember staffMember : shift.getStaffMembers()) {
                staffMember.getShifts().add(shift);
                staffMember = em.merge(staffMember);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException(ResourceBundle.getBundle("/Bundle").getString("TransactionRollBackError"), re);
            }
            if (findShift(shift.getId()) != null) {
                throw new PreexistingEntityException("Shift " + shift + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Shift shift) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Shift persistentShift = em.find(Shift.class, shift.getId());
//            Location locationOld = persistentShift.getLocation();
//            Location locationNew = shift.getLocation();
            Collection<StaffMember> staffMemberCollectionOld = persistentShift.getStaffMembers();

            Collection<StaffMember> staffMemberCollectionNew = shift.getStaffMembers();
            List<StaffMember> attachedStaffMemberCollectionNew = new ArrayList<StaffMember>();
            for (StaffMember staffMemberCollectionNewStaffMemberToAttach : staffMemberCollectionNew) {
                staffMemberCollectionNewStaffMemberToAttach = em.getReference(staffMemberCollectionNewStaffMemberToAttach.getClass(), staffMemberCollectionNewStaffMemberToAttach.getId());
                attachedStaffMemberCollectionNew.add(staffMemberCollectionNewStaffMemberToAttach);
            }
            staffMemberCollectionNew = attachedStaffMemberCollectionNew;
            shift.setStaffMembers(staffMemberCollectionNew);
            shift = em.merge(shift);
            for (StaffMember staffMemberCollectionNewStaffMember : staffMemberCollectionNew) {
                if (!staffMemberCollectionOld.contains(staffMemberCollectionNewStaffMember)) {
                    staffMemberCollectionNewStaffMember.getShifts().add(shift);
                    staffMemberCollectionNewStaffMember = em.merge(staffMemberCollectionNewStaffMember);
                }
            }
            for (StaffMember staffMemberCollectionOldStaffMember : staffMemberCollectionOld) {
                if (!staffMemberCollectionNew.contains(staffMemberCollectionOldStaffMember)) {
                    staffMemberCollectionOldStaffMember.getShifts().remove(shift);
                    staffMemberCollectionOldStaffMember = em.merge(staffMemberCollectionOldStaffMember);
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
                Long id = shift.getId();
                if (findShift(id) == null) {
                    throw new NonexistentEntityException(ResourceBundle.getBundle("/Bundle").getString("ShiftMissing"));
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Shift shift) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Shift persistentShift = null;
            try {
                persistentShift = em.getReference(Shift.class, shift.getId());
                persistentShift.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException(ResourceBundle.getBundle("/Bundle").getString("MissingShift"), enfe);
            }

            Location location = persistentShift.getLocation();
            if (location.getShifts().contains(persistentShift)) {
                location.getShifts().remove(persistentShift);
                em.persist(location);
            }

            for (StaffMember staffMemberToRemoveStaffMember : persistentShift.getStaffMembers()) {
                if (staffMemberToRemoveStaffMember.getShifts().contains(persistentShift)) {
                    staffMemberToRemoveStaffMember.getShifts().remove(persistentShift);
                    em.merge(staffMemberToRemoveStaffMember);
                }
            }
            em.remove(persistentShift);
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

    public List<Shift> findShiftEntities() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Shift as o");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Shift findShift(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Shift.class, id);
        } finally {
            em.close();
        }
    }

    public int getShiftCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from Shift as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
