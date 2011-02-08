package org.mcguppy.eventplaner.domain.metadata;

import java.util.ArrayList;
import java.util.List;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
public class StaffMemberMetaDataController {

    public List<StaffMemberMetaData> getStaffMemberMetaDataItems(List<StaffMember> staffMemberItems) {
        List<StaffMemberMetaData> staffMemberMetaDataItems = new ArrayList<StaffMemberMetaData>();
        for (StaffMember staffMember : staffMemberItems) {
            StaffMemberMetaData staffMemberMetaData = new StaffMemberMetaData(staffMember);
            this.createMetaData(staffMemberMetaData);

        }
        return staffMemberMetaDataItems;
    }

    public void createMetaData(StaffMemberMetaData staffMemberMetaData) {
        this.calculateMinShiftDeltaTime(staffMemberMetaData);
    }

    public void calculateMinShiftDeltaTime(StaffMemberMetaData staffMemberMetaData) {
        if (staffMemberMetaData.getStaffMember().getShifts().isEmpty()) {
            staffMemberMetaData.setMinShiftDeltaTime(null);
            return;
        }
        if (staffMemberMetaData.getStaffMember().getShifts().size() == 1) {
            staffMemberMetaData.setMinShiftDeltaTime(null);
            return;
        }

    }
}
