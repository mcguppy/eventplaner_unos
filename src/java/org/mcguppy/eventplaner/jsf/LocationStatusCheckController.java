package org.mcguppy.eventplaner.jsf;

import java.util.ArrayList;
import java.util.Collections;
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
    public List<Location> getLocationItemsWithNoShifts() {
        List<Location> locationItemsNoShifts = new ArrayList<Location>();

        for (Location location : locationItems) {
            if (location.getShifts().size() < 1) {
                locationItemsNoShifts.add(location);
            }
        }
        Collections.sort(locationItemsNoShifts);
        return locationItemsNoShifts;
    }
}
