package org.mcguppy.eventplaner.jsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.context.FacesContext;
import org.mcguppy.eventplaner.jpa.controllers.ShiftJpaController;
import org.mcguppy.eventplaner.jpa.entities.Shift;

/**
 *
 * @author stefan meichtry
 */
public class StatusCheckController {

    private ShiftJpaController jpaController = null;
    private List<Shift> shiftItems = new ArrayList<Shift>();

    public StatusCheckController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        jpaController = (ShiftJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "shiftJpa");
        shiftItems = jpaController.findShiftEntities();
    }

    public List<Shift> getShiftItemsWithLessMembers() {
        List<Shift> shiftItemsWithLessMembers = new ArrayList<Shift>();

        for (Shift shift : shiftItems) {
            if (shift.getStaffMembersSize() < shift.getNumberOfStaffMembers()) {
                shiftItemsWithLessMembers.add(shift);
            }
        }
        Collections.sort(shiftItemsWithLessMembers);
        return shiftItemsWithLessMembers;
    }

    public List<Shift> getShiftItemsWithToManyMembers() {
        List<Shift> shiftItemsWithToManyMembers = new ArrayList<Shift>();

        for (Shift shift : shiftItems) {
            if (shift.getStaffMembersSize() > shift.getNumberOfStaffMembers()) {
                shiftItemsWithToManyMembers.add(shift);
            }
        }
        Collections.sort(shiftItemsWithToManyMembers);
        return shiftItemsWithToManyMembers;
    }

    public List<Shift> getShiftItemsWithOutResponsible() {
        List<Shift> shiftItemsWithOutResposible = new ArrayList<Shift>();

        for (Shift shift : shiftItems) {
            if (shift.getResponsible() == null) {
                shiftItemsWithOutResposible.add(shift);
            }
        }
        Collections.sort(shiftItemsWithOutResposible);
        return shiftItemsWithOutResposible;
    }
}
