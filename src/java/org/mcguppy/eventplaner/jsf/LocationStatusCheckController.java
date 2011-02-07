package org.mcguppy.eventplaner.jsf;

import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import org.mcguppy.eventplaner.jpa.controllers.LocationJpaController;
import org.mcguppy.eventplaner.jpa.entities.Location;

/**
 *
 * @author stefan meichtry
 */
public class LocationStatusCheckController {

    private LocationJpaController locationJpaController = null;
    private List<Location> locationItems = new ArrayList<Location>();

    public LocationStatusCheckController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        locationJpaController = (LocationJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "locationJpa");
        locationItems = locationJpaController.findLocationEntities();
    }
}
