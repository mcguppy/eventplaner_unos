
package org.mcguppy.eventplaner.jsf.comperator;

import java.util.Comparator;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
public class StaffMemberShiftSizeComperator implements Comparator<StaffMember> {

    @Override
    public int compare(StaffMember member1, StaffMember member2) {
        Integer shiftSize1 = member1.getShiftsSize();
        Integer shiftSize2 = member2.getShiftsSize();
        return shiftSize2.compareTo(shiftSize1);
    }


}
