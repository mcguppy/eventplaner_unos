package org.mcguppy.eventplaner.domain.metadata;

import java.util.Date;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
public class StaffMemberMetaData {

    private StaffMember staffMember;
    private Long minShiftDeltaTime;

    public StaffMemberMetaData(StaffMember staffMember) {
        this.staffMember = staffMember;
    }

    public Long getMinShiftDeltaTime() {
        return minShiftDeltaTime;
    }

    public void setMinShiftDeltaTime(Long minShiftDeltaTime) {
        this.minShiftDeltaTime = minShiftDeltaTime;
    }

    public StaffMember getStaffMember() {
        return staffMember;
    }

    public void setStaffMember(StaffMember staffMember) {
        this.staffMember = staffMember;
    }

    public String getMinShiftDeltaTimeHoursMinutesSecondsString() {
        String format = String.format("%%0%dd", 2);
        Long elapsedTime =  minShiftDeltaTime / 1000;
        String seconds = String.format(format, elapsedTime % 60);
        String minutes = String.format(format, (elapsedTime % 3600) / 60);
        String hours = String.format(format, elapsedTime / 3600);
        String time = hours + ":" + minutes + ":" + seconds;
        return time;
    }
}
