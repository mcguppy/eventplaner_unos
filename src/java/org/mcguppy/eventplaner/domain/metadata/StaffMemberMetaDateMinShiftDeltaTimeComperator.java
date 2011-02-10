
package org.mcguppy.eventplaner.domain.metadata;

import java.util.Comparator;

/**
 *
 * @author stefan meichtry
 */
public class StaffMemberMetaDateMinShiftDeltaTimeComperator implements Comparator<StaffMemberMetaData> {

    @Override
    public int compare(StaffMemberMetaData member1, StaffMemberMetaData member2) {
        return member1.getMinShiftDeltaTime().compareTo(member2.getMinShiftDeltaTime());
    }


}
