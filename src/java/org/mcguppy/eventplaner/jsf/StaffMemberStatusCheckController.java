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
import org.mcguppy.eventplaner.jsf.comperator.StaffMemberShiftSizeComperator;

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
            if (staffMember.getShiftsSize() == 0) {
                staffMemberItemsNoShifts.add(staffMember);
            }
        }
        Collections.sort(staffMemberItemsNoShifts);
        return staffMemberItemsNoShifts;
    }

    public List<StaffMember> getStaffMemberItemsWithMostShifts() {
        int numberOfItems = 50;
        List<StaffMember> staffMemberItemsMostShifts = new ArrayList<StaffMember>();

        for (StaffMember staffMember : staffMemberItems) {
            if (staffMember.getShiftsSize() > 0) {
                staffMemberItemsMostShifts.add(staffMember);
            }
        }

        Collections.sort(staffMemberItemsMostShifts, new StaffMemberShiftSizeComperator());
        if (numberOfItems >= staffMemberItemsMostShifts.size()) {
            return staffMemberItemsMostShifts;
        }
        return staffMemberItemsMostShifts.subList(0, numberOfItems);
    }

    public List<StaffMember> getStaffMemberItemsWithShortestTimeBetweenShifts() {
        int numberOfItems = 150;
        calculateMinShiftDeltaTime();
        Collections.sort(staffMemberItems, new StaffMemberShiftDeltaTimeComperator());
        List<StaffMember> staffMemberItemsWithShortestTimeBetweenShifts = new ArrayList<StaffMember>();
        int counter = 0;
        for(StaffMember staffMember : staffMemberItems) {
            counter++;
            if(staffMember.getMinShiftDeltaTime() == Long.MAX_VALUE){
                break;
            }
            staffMemberItemsWithShortestTimeBetweenShifts.add(staffMember);
            if(counter >= numberOfItems) {
                break;
            }
        }
        return staffMemberItemsWithShortestTimeBetweenShifts;
        
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
