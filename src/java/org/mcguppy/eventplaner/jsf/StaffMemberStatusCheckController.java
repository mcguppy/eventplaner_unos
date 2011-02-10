package org.mcguppy.eventplaner.jsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.context.FacesContext;
import org.mcguppy.eventplaner.domain.metadata.StaffMemberMetaData;
import org.mcguppy.eventplaner.domain.metadata.StaffMemberMetaDataController;
import org.mcguppy.eventplaner.jpa.controllers.StaffMemberJpaController;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
public class StaffMemberStatusCheckController {

    private StaffMemberJpaController staffMemberJpaController = null;
    private List<StaffMember> staffMemberItems = new ArrayList<StaffMember>();
    private StaffMemberMetaDataController staffMemberMetaDataController;


    public StaffMemberStatusCheckController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        staffMemberJpaController = (StaffMemberJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "staffMemberJpa");
        staffMemberItems = staffMemberJpaController.findStaffMemberEntities();
        staffMemberMetaDataController = new StaffMemberMetaDataController();
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
        int maxNumberOfShifts = 0;

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

    public List<StaffMemberMetaData> getStaffMemberMetaDataItemWithShortestTimeBetweenShifts() {
        return this.staffMemberMetaDataController.getStaffMemberMetaDataItemsWithShortestTimeBetweenShifts(staffMemberItems, 100);
    }
}
