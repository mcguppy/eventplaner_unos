package org.mcguppy.eventplaner.reporting.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.mcguppy.eventplaner.jpa.controllers.StaffMemberJpaController;
import org.mcguppy.eventplaner.jpa.entities.StaffMember;

/**
 *
 * @author stefan meichtry
 */
@Named
@RequestScoped
public class ShirtController {

    public ShirtController() {
        facesContext = FacesContext.getCurrentInstance();
        jpaController = (StaffMemberJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "staffMemberJpa");
    }
    private StaffMemberJpaController jpaController = null;
    private FacesContext facesContext = null;

    public String createShirtOverview() {
        return "reportingShirt";
    }

    public LinkedHashMap<String, Integer> getData() {
        LinkedHashMap<String, Integer> data = new LinkedHashMap<>();
        for (StaffMember.Shirt shirt : StaffMember.Shirt.values()) {
            data.put(shirt.name(), 0);
        }
        for (StaffMember staffMember : jpaController.findStaffMemberEntities()) {
            if (staffMember.getShirt() != null) {
                String shirtName = staffMember.getShirt().name();
                Integer count = data.get(shirtName);
                if (count == null) {
                    data.put(shirtName, 1);
                } else {
                    data.put(shirtName, count + 1);
                }
            }
        }
        return data;
    }

    public List<StaffMember> getStaffMembersWithoutShirt() {
        List<StaffMember> staffMembersWithoutShirt = new ArrayList<>();
        for (StaffMember staffMember : jpaController.findStaffMemberEntities()) {
            if (staffMember.getShirt() == null) {
                staffMembersWithoutShirt.add(staffMember);
            }

        }
        Collections.sort(staffMembersWithoutShirt);
        return staffMembersWithoutShirt;
    }

}
