
package org.mcguppy.eventplaner.jsf;

import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import org.mcguppy.eventplaner.jpa.controllers.StaffMemberJpaController;
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

}
