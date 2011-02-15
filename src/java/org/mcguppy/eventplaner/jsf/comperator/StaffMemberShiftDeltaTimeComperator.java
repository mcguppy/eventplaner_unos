
package org.mcguppy.eventplaner.jsf.comperator;

import java.util.Comparator;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
public class StaffMemberShiftDeltaTimeComperator implements Comparator<StaffMember> {

    @Override
    public int compare(StaffMember member1, StaffMember member2) {
        return member1.getMinShiftDeltaTime().compareTo(member2.getMinShiftDeltaTime());
    }


}
