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
import org.mcguppy.eventplaner.jpa.entities.Shift;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author mbohm
 */
public class StaffMemberJpaController {

    @Resource
    private UserTransaction utx = null;
    @PersistenceUnit(unitName = "enventPlanerPU")
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(StaffMember staffMember) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (staffMember.getShifts() == null) {
            staffMember.setShifts(new ArrayList<Shift>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<Shift> attachedShiftCollection = new ArrayList<Shift>();
            for (Shift shiftToAttach : staffMember.getShifts()) {
                shiftToAttach = em.getReference(shiftToAttach.getClass(), shiftToAttach.getId());
                attachedShiftCollection.add(shiftToAttach);
            }
            staffMember.setShifts(attachedShiftCollection);
            em.persist(staffMember);
            for (Shift shift : staffMember.getShifts()) {
                shift.getStaffMembers().add(staffMember);
                shift = em.merge(shift);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException(ResourceBundle.getBundle("/Bundle").getString("TransactionRollBackError"), re);
            }
            if (findStaffMember(staffMember.getId()) != null) {
                throw new PreexistingEntityException("StaffMember " + staffMember + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(StaffMember staffMember) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            StaffMember persistentStaffMember = em.find(StaffMember.class, staffMember.getId());
            Collection<Shift> shiftCollectionOld = persistentStaffMember.getShifts();
            Collection<Shift> shiftCollectionNew = staffMember.getShifts();
            List<Shift> attachedShiftCollectionNew = new ArrayList<Shift>();
            for (Shift shiftCollectionNewShiftToAttach : shiftCollectionNew) {
                shiftCollectionNewShiftToAttach = em.getReference(shiftCollectionNewShiftToAttach.getClass(), shiftCollectionNewShiftToAttach.getId());
                attachedShiftCollectionNew.add(shiftCollectionNewShiftToAttach);
            }
            shiftCollectionNew = attachedShiftCollectionNew;
            staffMember.setShifts(shiftCollectionNew);
            staffMember = em.merge(staffMember);
            for (Shift shiftCollectionNewShift : shiftCollectionNew) {
                if (!shiftCollectionOld.contains(shiftCollectionNewShift)) {
                    shiftCollectionNewShift.getStaffMembers().add(staffMember);
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
                Long id = staffMember.getId();
                if (findStaffMember(id) == null) {
                    throw new NonexistentEntityException(ResourceBundle.getBundle("/Bundle").getString("StaffMemberMissing"));
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(StaffMember staffMember) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            StaffMember persistentStaffMember = null;
            try {
                persistentStaffMember = em.getReference(StaffMember.class, staffMember.getId());
                persistentStaffMember.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException(ResourceBundle.getBundle("/Bundle").getString("MissingStaffMember"), enfe);
            }

            for (Shift shiftToRemoveStaffMember : persistentStaffMember.getShifts()) {
                if (shiftToRemoveStaffMember.getStaffMembers().contains(persistentStaffMember)) {
                    shiftToRemoveStaffMember.getStaffMembers().remove(persistentStaffMember);
                    em.merge(shiftToRemoveStaffMember);
                }
            }
            em.remove(persistentStaffMember);
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

    public List<StaffMember> findStaffMemberEntities() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from StaffMember as o");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public StaffMember findStaffMember(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(StaffMember.class, id);
        } finally {
            em.close();
        }
    }

    public int getStaffMemberCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from StaffMember as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
