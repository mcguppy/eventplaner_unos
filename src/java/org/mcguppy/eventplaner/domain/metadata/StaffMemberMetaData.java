
package org.mcguppy.eventplaner.domain.metadata;

import java.util.Date;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
public class StaffMemberMetaData {

    private StaffMember staffMember;
    private Date minShiftDeltaTime;

    public StaffMemberMetaData(StaffMember staffMember) {
        this.staffMember = staffMember;
    }

    public Date getMinShiftDeltaTime() {
        return minShiftDeltaTime;
    }

    public void setMinShiftDeltaTime(Date minShiftDeltaTime) {
        this.minShiftDeltaTime = minShiftDeltaTime;
    }

    public StaffMember getStaffMember() {
        return staffMember;
    }

    public void setStaffMember(StaffMember staffMember) {
        this.staffMember = staffMember;
    }


    

}
