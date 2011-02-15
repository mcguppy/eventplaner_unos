package org.mcguppy.eventplaner.jsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.faces.context.FacesContext;
import org.mcguppy.eventplaner.jsf.comperator.StaffMemberShiftDeltaTimeComperator;
import org.mcguppy.eventplaner.jpa.controllers.StaffMemberJpaController;
import org.mcguppy.eventplaner.jpa.entities.Shift;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
public class StaffMemberStatusCheckController {

    private StaffMemberJpaController staffMemberJpaController = null;
    private List<StaffMember> staffMemberItems = new ArrayList<StaffMember>();

    public StaffMemberStatusCheckController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        staffMemberJpaController = (StaffMemberJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "staffMemberJpa");
        staffMemberItems = staffMemberJpaController.findStaffMemberEntities();
    }

    public List<StaffMember> getStaffMemberItemsWithNoShifts() {
        List<StaffMember> staffMemberItemsNoShifts = new ArrayList<StaffMember>();

        for (StaffMember staffMember : staffMemberItems) {
            if (staffMember.getShifts().size() < 1) {
                staffMemberItemsNoShifts.add(staffMember);
            }
        }
        Collections.sort(staffMemberItemsNoShifts);
        return staffMemberItemsNoShifts;
    }

    public List<StaffMember> getStaffMemberItemsWithMostShifts() {
        List<StaffMember> staffMemberItemsMostShifts = new ArrayList<StaffMember>();
        int maxNumberOfShifts = 1;

        for (StaffMember staffMember : staffMemberItems) {
            if (staffMember.getShifts().size() > maxNumberOfShifts) {
                staffMemberItemsMostShifts.clear();
                staffMemberItemsMostShifts.add(staffMember);
                maxNumberOfShifts = staffMember.getShifts().size();
            } else {
                if (staffMember.getShifts().size() == maxNumberOfShifts) {
                    staffMemberItemsMostShifts.add(staffMember);
                }
            }

        }
        Collections.sort(staffMemberItemsMostShifts);
        return staffMemberItemsMostShifts;
    }

    public List<StaffMember> getStaffMemberItemsWithShortestTimeBetweenShifts() {
        int numberOfItems = 100;
        calculateMinShiftDeltaTime();
        List<StaffMember>staffMemberItemsWithShortestTimeBetweenShifts = staffMemberItems;
        Collections.sort(staffMemberItemsWithShortestTimeBetweenShifts, new StaffMemberShiftDeltaTimeComperator());
        Iterator<StaffMember> iter = staffMemberItemsWithShortestTimeBetweenShifts.iterator();
        while (iter.hasNext()) {
            if (iter.next().getMinShiftDeltaTime() == Long.MAX_VALUE) {
                iter.remove();
            }
        }
        if (numberOfItems >= staffMemberItemsWithShortestTimeBetweenShifts.size()) {
            return staffMemberItemsWithShortestTimeBetweenShifts;
        }
        return staffMemberItemsWithShortestTimeBetweenShifts.subList(0, numberOfItems);
    }

    private void calculateMinShiftDeltaTime() {

        for (StaffMember staffMember : staffMemberItems) {

            List<Shift> shifts = (List<Shift>) staffMember.getShifts();
            if (shifts.isEmpty() || shifts.size() == 1) {
                staffMember.setMinShiftDeltaTime(Long.MAX_VALUE);
                continue;
            }
            Collections.sort(shifts);
            Long minShiftDeltaTime = Long.MAX_VALUE;
            Shift lastShift = null;
            Long shiftDeltaTime;
            for (Shift actualShift : shifts) {
                if (lastShift == null) {
                    lastShift = actualShift;
                    continue;
                }
                shiftDeltaTime = actualShift.getStartTime().getTime() - lastShift.getEndTime().getTime();
                if (shiftDeltaTime < minShiftDeltaTime) {
                    minShiftDeltaTime = shiftDeltaTime;
                }
                lastShift = actualShift;
            }
            staffMember.setMinShiftDeltaTime(minShiftDeltaTime);
        }

    }
}
