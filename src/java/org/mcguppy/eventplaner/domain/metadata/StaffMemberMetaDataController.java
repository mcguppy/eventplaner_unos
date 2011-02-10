package org.mcguppy.eventplaner.domain.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.mcguppy.eventplaner.jpa.entities.Shift;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
public class StaffMemberMetaDataController {

    public List<StaffMemberMetaData> getStaffMemberMetaDataItemsWithShortestTimeBetweenShifts(List<StaffMember> staffMemberList, int numberOfItems) {
        List<StaffMemberMetaData> list = getStaffMemberMetaDataItems(staffMemberList);
        Collections.sort(list, new StaffMemberMetaDateMinShiftDeltaTimeComperator());
        if (numberOfItems >= list.size()) {
            return list;
        }
        return list.subList(0, numberOfItems);
    }

    public List<StaffMemberMetaData> getStaffMemberMetaDataItems(List<StaffMember> staffMemberItems) {
        List<StaffMemberMetaData> staffMemberMetaDataItems = new ArrayList<StaffMemberMetaData>();
        for (StaffMember staffMember : staffMemberItems) {
            StaffMemberMetaData staffMemberMetaData = new StaffMemberMetaData(staffMember);
            this.createMetaData(staffMemberMetaData);
            staffMemberMetaDataItems.add(staffMemberMetaData);
        }
        return staffMemberMetaDataItems;
    }

    private void createMetaData(StaffMemberMetaData staffMemberMetaData) {
        this.calculateMinShiftDeltaTime(staffMemberMetaData);
    }

    private void calculateMinShiftDeltaTime(StaffMemberMetaData staffMemberMetaData) {
        List<Shift> shifts = (List<Shift>) staffMemberMetaData.getStaffMember().getShifts();
        if (shifts.isEmpty() || shifts.size() == 1) {
            staffMemberMetaData.setMinShiftDeltaTime(Long.MAX_VALUE);
            return;
        }
        Collections.sort(shifts);
        Long minShiftDeltaTime = Long.MAX_VALUE;
        Iterator<Shift> iter = shifts.iterator();
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
        staffMemberMetaData.setMinShiftDeltaTime(minShiftDeltaTime);
    }
}
